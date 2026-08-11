/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.yv_smp.logic.music.svc

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.UUID
import java.util.function.Consumer

/**
 * Pure-reflection bridge to Simple Voice Chat's API.
 *
 * ## Why this exists
 *
 * `yv-smp` is fetched at runtime as a dynamically-loaded feature module (see
 * `core`'s `DynamicModuleLoader`/`DynamicModuleDriver`). Every class in this
 * module is defined by Paper's "library tier" classloader - a plain
 * `URLClassLoader` built once in `PaperClasspathBuilder.buildClassLoader`,
 * combining the embedded runtime bundle and every dynamically-resolved
 * feature module (see `DynamicModuleLoader.classloader()`).
 *
 * That library tier classloader has its parent set to the *root* classloader
 * (`PaperClasspathBuilder.class.getClassLoader()`), NOT to Paper's per-plugin
 * dependency group. Cross-plugin visibility (`dependencies.server.voicechat`
 * in `core`'s `paper-plugin.yml`) is only wired into `core`'s own top-level
 * `PaperPluginClassLoader` (see `PaperPluginClassLoader.loadClass`, which
 * checks: own jar -> library tier -> dependency group, in that order). The
 * library tier itself never gets that third fallback.
 *
 * Concretely: any class living in this module that statically references a
 * `de.maxhenkel.voicechat.api.*` type (even just as a compileOnly-resolved
 * parameter/field/supertype) will throw `NoClassDefFoundError` the moment
 * that type needs to be linked/resolved - **regardless of whether Simple
 * Voice Chat is installed, enabled, and loaded before this plugin**. This is
 * a hard classloader isolation issue, not a missing/misconfigured
 * dependency.
 *
 * The only correct fix is for nothing in this module to ever reference
 * voicechat-api types directly. Instead, every class/method is resolved
 * through Simple Voice Chat's *own* classloader (obtained from the live
 * `voicechat` `Plugin` instance itself) via reflection here, so we always
 * operate on the exact same `Class`/instances the real plugin created. This
 * also matters semantically, not just for linking: Bukkit's
 * `ServicesManager` keys registered services by `Class` identity, so using a
 * `Class` object resolved through any *other* classloader (even one that
 * loads byte-for-byte identical class files) would simply fail to find the
 * service that voicechat registered.
 */
object VoicechatBridge {
    private const val API_PACKAGE = "de.maxhenkel.voicechat.api"

    @Volatile
    private var initializedLoader: ClassLoader? = null

    private lateinit var bukkitVoicechatServiceClass: Class<*>
    private lateinit var voicechatPluginClass: Class<*>
    private lateinit var voicechatApiClass: Class<*>
    private lateinit var voicechatServerApiClass: Class<*>
    private lateinit var voicechatConnectionClass: Class<*>
    private lateinit var audioChannelClass: Class<*>
    private lateinit var staticAudioChannelClass: Class<*>
    private lateinit var eventRegistrationClass: Class<*>
    private lateinit var voicechatServerStartedEventClass: Class<*>
    private lateinit var volumeCategoryClass: Class<*>
    private lateinit var volumeCategoryBuilderClass: Class<*>

    private lateinit var registerPluginMethod: Method
    private lateinit var volumeCategoryBuilderMethod: Method
    private lateinit var getConnectionOfMethod: Method
    private lateinit var createStaticAudioChannelMethod: Method
    private lateinit var registerVolumeCategoryMethod: Method
    private lateinit var addTargetMethod: Method
    private lateinit var setCategoryMethod: Method
    private lateinit var sendMethod: Method
    private lateinit var registerEventMethod: Method
    private lateinit var getVoicechatFromEventMethod: Method
    private lateinit var builderSetIdMethod: Method
    private lateinit var builderSetNameMethod: Method
    private lateinit var builderSetDescriptionMethod: Method
    private lateinit var builderSetIconMethod: Method
    private lateinit var builderBuildMethod: Method

    /**
     * A handler for the (few) [de.maxhenkel.voicechat.api.VoicechatPlugin]
     * callbacks we care about, expressed without referencing any
     * voicechat-api type. [createPluginProxy] builds an actual
     * `VoicechatPlugin` instance (via [Proxy], loaded through voicechat's own
     * classloader) that delegates to this.
     */
    interface PluginHandler {
        fun pluginId(): String
        fun onInitialize(api: Any)
        fun onServerStarted(event: Any)
    }

    val isInitialized: Boolean
        get() = initializedLoader != null

    /**
     * Resolves every class/method reference needed below through [loader] -
     * which must be the "voicechat" plugin's own classloader (e.g.
     * `Bukkit.getPluginManager().getPlugin("voicechat")!!.javaClass.classLoader`).
     *
     * Safe to call more than once with the same loader (e.g. across a
     * `/reload`); re-resolves if called with a different loader.
     */
    @Synchronized
    fun initialize(loader: ClassLoader) {
        if (initializedLoader === loader) return

        fun clazz(simpleName: String) = Class.forName("$API_PACKAGE.$simpleName", true, loader)

        bukkitVoicechatServiceClass = clazz("BukkitVoicechatService")
        voicechatPluginClass = clazz("VoicechatPlugin")
        voicechatApiClass = clazz("VoicechatApi")
        voicechatServerApiClass = clazz("VoicechatServerApi")
        voicechatConnectionClass = clazz("VoicechatConnection")
        audioChannelClass = clazz("audiochannel.AudioChannel")
        staticAudioChannelClass = clazz("audiochannel.StaticAudioChannel")
        eventRegistrationClass = clazz("events.EventRegistration")
        voicechatServerStartedEventClass = clazz("events.VoicechatServerStartedEvent")
        volumeCategoryClass = clazz("VolumeCategory")
        volumeCategoryBuilderClass = Class.forName("$API_PACKAGE.VolumeCategory\$Builder", true, loader)

        registerPluginMethod = bukkitVoicechatServiceClass.getMethod("registerPlugin", voicechatPluginClass)
        volumeCategoryBuilderMethod = voicechatApiClass.getMethod("volumeCategoryBuilder")
        getConnectionOfMethod = voicechatServerApiClass.getMethod("getConnectionOf", UUID::class.java)
        createStaticAudioChannelMethod =
            voicechatServerApiClass.getMethod("createStaticAudioChannel", UUID::class.java)
        registerVolumeCategoryMethod =
            voicechatServerApiClass.getMethod("registerVolumeCategory", volumeCategoryClass)
        addTargetMethod = staticAudioChannelClass.getMethod("addTarget", voicechatConnectionClass)
        setCategoryMethod = audioChannelClass.getMethod("setCategory", String::class.java)
        sendMethod = audioChannelClass.getMethod("send", ByteArray::class.java)
        registerEventMethod =
            eventRegistrationClass.getMethod("registerEvent", Class::class.java, Consumer::class.java)
        getVoicechatFromEventMethod = voicechatServerStartedEventClass.getMethod("getVoicechat")
        builderSetIdMethod = volumeCategoryBuilderClass.getMethod("setId", String::class.java)
        builderSetNameMethod = volumeCategoryBuilderClass.getMethod("setName", String::class.java)
        builderSetDescriptionMethod = volumeCategoryBuilderClass.getMethod("setDescription", String::class.java)
        builderSetIconMethod = volumeCategoryBuilderClass.getMethod("setIcon", Array<IntArray>::class.java)
        builderBuildMethod = volumeCategoryBuilderClass.getMethod("build")

        initializedLoader = loader
    }

    /** The `BukkitVoicechatService` class, resolved via voicechat's own classloader. */
    fun bukkitVoicechatServiceClass(): Class<*> = bukkitVoicechatServiceClass

    /**
     * Builds a real `VoicechatPlugin` instance (via a JDK dynamic [Proxy]) that
     * delegates `getPluginId`/`initialize`/`registerEvents` to [handler].
     */
    fun createPluginProxy(handler: PluginHandler): Any {
        val invocationHandler = InvocationHandler { proxy, method, args ->
            when (method.name) {
                "getPluginId" -> handler.pluginId()
                "initialize" -> {
                    handler.onInitialize(args!![0]!!)
                    null
                }

                "registerEvents" -> {
                    registerServerStartedEvent(args!![0]!!, handler)
                    null
                }

                "equals" -> proxy === args?.getOrNull(0)
                "hashCode" -> System.identityHashCode(proxy)
                "toString" -> "YVtilsVoicechatPlugin"
                else -> null
            }
        }

        return Proxy.newProxyInstance(
            voicechatPluginClass.classLoader,
            arrayOf(voicechatPluginClass),
            invocationHandler
        )
    }

    private fun registerServerStartedEvent(registration: Any, handler: PluginHandler) {
        val consumer = Consumer<Any> { event -> handler.onServerStarted(event) }
        registerEventMethod.invoke(registration, voicechatServerStartedEventClass, consumer)
    }

    fun registerPlugin(service: Any, plugin: Any) {
        registerPluginMethod.invoke(service, plugin)
    }

    fun getVoicechatFromEvent(event: Any): Any = getVoicechatFromEventMethod.invoke(event)!!

    fun volumeCategoryBuilder(api: Any): Any = volumeCategoryBuilderMethod.invoke(api)!!

    fun builderSetId(builder: Any, id: String): Any = builderSetIdMethod.invoke(builder, id)!!

    fun builderSetName(builder: Any, name: String): Any = builderSetNameMethod.invoke(builder, name)!!

    fun builderSetDescription(builder: Any, description: String?): Any =
        builderSetDescriptionMethod.invoke(builder, description)!!

    fun builderSetIcon(builder: Any, icon: Array<IntArray>): Any = builderSetIconMethod.invoke(builder, icon)!!

    fun builderBuild(builder: Any): Any = builderBuildMethod.invoke(builder)!!

    fun registerVolumeCategory(serverApi: Any, category: Any) {
        registerVolumeCategoryMethod.invoke(serverApi, category)
    }

    /** @return the player's [de.maxhenkel.voicechat.api.VoicechatConnection], or `null` if not connected. */
    fun getConnectionOf(serverApi: Any, uuid: UUID): Any? = getConnectionOfMethod.invoke(serverApi, uuid)

    /** @return a new `StaticAudioChannel`, or `null` if the player isn't connected/available. */
    fun createStaticAudioChannel(serverApi: Any, id: UUID): Any? =
        createStaticAudioChannelMethod.invoke(serverApi, id)

    fun addTarget(channel: Any, connection: Any) {
        addTargetMethod.invoke(channel, connection)
    }

    fun setCategory(channel: Any, category: String?) {
        setCategoryMethod.invoke(channel, category)
    }

    fun send(channel: Any, data: ByteArray) {
        sendMethod.invoke(channel, data)
    }
}
