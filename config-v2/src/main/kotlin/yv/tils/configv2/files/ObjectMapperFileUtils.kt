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

package yv.tils.configv2.files

import io.leangen.geantyref.GenericTypeReflector
import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.dataClassFieldDiscoverer
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.util.NamingSchemes
import yv.tils.utils.logger.Logger
import java.math.BigInteger
import java.sql.Date
import java.sql.Timestamp

/**
 * ObjectMapper-based facade over [ConfigurateFileUtils] for persisting a Kotlin `data class`
 * (optionally containing nested data classes, enums, and enum-keyed maps/collections
 * thereof) as a single unit, instead of the hand-written `node.node("key")` read/write pairs
 * every `SaveFile`/`*StatesFile`/`ConfigFile` class in this project writes by hand (one per
 * field, kept in sync manually between the data class, the load function, and the save
 * function).
 *
 * With this, adding a new persisted field only ever requires touching the data class's
 * declaration (with a sensible default) - [load]/[loadList] automatically fall back to that
 * default for any field missing from an older on-disk file, and [save]/[saveList]
 * automatically pick up the new field the next time they're called. There's nothing else to
 * keep in sync.
 *
 * Supports both `.json` ([ConfigFormat.JSON], the default - matches every existing
 * `SaveFile`) and `.yml` ([ConfigFormat.YAML] - for GUI-editable `config.yml`s once paired
 * with `gui`'s annotation-driven config GUI).
 */
class ObjectMapperFileUtils {
    companion object {
        /**
         * Mirrors `GsonConfigurationLoader`'s own (private) native-types set - see [optionsFor]
         * for why this needs to be kept in sync with it.
         */
        @PublishedApi
        internal val jsonNativeTypes: Set<Class<*>> = setOf(
            Double::class.java,
            Float::class.java,
            Long::class.java,
            Integer::class.java,
            Boolean::class.java,
            String::class.java,
        )

        /**
         * Mirrors `YamlConfigurationLoader`'s own (private) native-types set - see [optionsFor]
         * for why this needs to be kept in sync with it. Notably *not* identical to
         * [jsonNativeTypes] (e.g. YAML has no native `Float`, but does have `BigInteger`).
         */
        @PublishedApi
        internal val yamlNativeTypes: Set<Class<*>> = setOf(
            Boolean::class.java,
            Integer::class.java,
            Long::class.java,
            BigInteger::class.java,
            Double::class.java,
            ByteArray::class.java,
            String::class.java,
            Date::class.java,
            Date::class.java,
            Timestamp::class.java,
        )

        /**
         * The [ObjectMapper.Factory] used by every function here.
         *
         * - [dataClassFieldDiscoverer] lets the mapper read/write a Kotlin `data class`'s
         *   primary-constructor properties directly - no `@ConfigSerializable` annotation
         *   or JavaBean getters/setters required.
         * - [NamingSchemes.PASSTHROUGH] keeps field names exactly as declared (`pvpState`
         *   stays `pvpState`) instead of Configurate's default `lower-case-dashed` scheme
         *   (`pvpState` -> `pvp-state`), which would silently break every already-existing
         *   hand-written save/state file in this project on the next load.
         */
        @PublishedApi
        internal val factory: ObjectMapper.Factory = ObjectMapper.factoryBuilder()
            .addDiscoverer(dataClassFieldDiscoverer())
            .defaultNamingScheme(NamingSchemes.PASSTHROUGH)
            .build()

        /**
         * The standard Configurate defaults (String/Number/Boolean/List/Map/Enum/...) *plus*
         * [factory] itself, registered as a fallback for any type that's a Kotlin `data
         * class` and isn't otherwise handled.
         *
         * This is what makes *nested* data classes work (e.g. a `List<Warning>` field inside
         * a `WarnSave` data class): without this, [factory] only ever gets consulted for the
         * top-level type explicitly passed to [load]/[save]/[loadList]/[saveList] - any
         * nested custom type would otherwise fail with "No applicable type serializer",
         * since the node's own (default) options have no way to resolve it.
         *
         * The native-types restriction passed in additionally restricts which types
         * [ConfigurationOptions] considers "native" (storable as-is, no serializer needed) to
         * exactly the set the target format's own loader uses. Without this,
         * [BasicConfigurationNode]'s own (unrestricted) default would consider *any* object
         * "native" - e.g. an enum field would get written to the node as the raw enum
         * instance instead of being converted to its name via
         * [org.spongepowered.configurate.serialize.EnumValueSerializer], only to then fail
         * once that value is copied into an actual format-backed node (whose native types
         * are restricted), since a raw enum object isn't one of the types Gson/SnakeYAML
         * accept directly.
         */
        @PublishedApi
        internal val jsonOptions: ConfigurationOptions = buildOptions(jsonNativeTypes)

        @PublishedApi
        internal val yamlOptions: ConfigurationOptions = buildOptions(yamlNativeTypes)

        private fun buildOptions(nativeTypes: Set<Class<*>>): ConfigurationOptions =
            ConfigurationOptions.defaults()
                .nativeTypes(nativeTypes)
                .serializers(
                    TypeSerializerCollection.defaults().childBuilder()
                        .register({ type ->
                            runCatching { GenericTypeReflector.erase(type).kotlin.isData }.getOrDefault(
                                false
                            )
                        }, factory.asTypeSerializer())
                        .build()
                )

        @PublishedApi
        internal fun optionsFor(format: ConfigFormat): ConfigurationOptions = when (format) {
            ConfigFormat.JSON -> jsonOptions
            ConfigFormat.YAML -> yamlOptions
        }

        /**
         * Loads [path] and maps it into an instance of [T] (a Kotlin `data class`).
         *
         * Returns [default] as-is - without touching disk any further - if the file doesn't
         * exist yet, or logs an error and returns [default] if it exists but fails to parse.
         * Callers that need to bootstrap a fresh file on first run should explicitly call
         * [save] with the returned value afterward (see `StatesFile.loadConfig()` for the
         * established pattern) - `load` itself never writes anything.
         *
         * Any field declared on [T] but missing from the on-disk file (e.g. a field added to
         * the data class after the file was first created) transparently falls back to that
         * field's own Kotlin default value instead of failing.
         */
        inline fun <reified T : Any> load(
            path: String,
            default: T,
            format: ConfigFormat = ConfigFormat.JSON,
            overwriteParentDir: Boolean = false,
        ): T {
            val file = runCatching { ConfigurateFileUtils.load(path, format, overwriteParentDir) }.getOrNull()
                ?: return default

            // Bridge into a node backed by the format-appropriate options (which know about
            // nested data classes) before handing it to the mapper - `file.node` itself only
            // has the plain Configurate defaults attached, since it was loaded via the shared
            // (path resolution only) ConfigurateFileUtils loader.
            val bridge = BasicConfigurationNode.root(optionsFor(format))
            bridge.from(file.node)

            return runCatching { factory.get<T>().load(bridge) }
                .onFailure { Logger.error("Failed to load $path as ${T::class.simpleName}: ${it.message}") }
                .getOrDefault(default)
        }

        /**
         * Persists [value] to [path], fully replacing whatever's already on disk.
         *
         * Safe to call any time [value] represents the complete, current in-memory state
         * (e.g. right after [load] returned it, or after mutating a field on it) - since [T]
         * is always the full object rather than a partial patch, there's no separate "safe
         * bootstrap merge" vs. "real update overwrite" distinction to keep track of, unlike
         * the hand-written `registerStrings()`/`upgradeStrings()` split used elsewhere.
         */
        inline fun <reified T : Any> save(
            path: String,
            value: T,
            format: ConfigFormat = ConfigFormat.JSON,
            overwriteParentDir: Boolean = false,
        ) {
            val bridge = BasicConfigurationNode.root(optionsFor(format))
            factory.get<T>().save(value, bridge)

            val file = ConfigurateFileUtils.create(path, emptyMap(), format, overwriteParentDir)
            file.node.from(bridge)
            ConfigurateFileUtils.save(file)
        }

        /**
         * Loads [path] and maps the list found at [key] (e.g. `{"saves": [...]}` ->
         * `key = "saves"`, the shape almost every `SaveFile` class in this project uses) into
         * a list of [T] instances (a Kotlin `data class`, which may itself contain nested
         * data classes, enums, and enum-keyed maps - all handled transparently).
         *
         * Returns an empty list if the file doesn't exist, [key] is missing, or [key] isn't a
         * list. Entries that individually fail to map (e.g. a corrupt/incompatible record)
         * are skipped and logged rather than failing the whole load.
         */
        inline fun <reified T : Any> loadList(
            path: String,
            key: String = "saves",
            format: ConfigFormat = ConfigFormat.JSON,
            overwriteParentDir: Boolean = false,
        ): MutableList<T> {
            val file = runCatching { ConfigurateFileUtils.load(path, format, overwriteParentDir) }.getOrNull()
                ?: return mutableListOf()

            val listNode = file.node.node(key)
            if (listNode.virtual() || !listNode.isList()) return mutableListOf()

            val bridge = BasicConfigurationNode.root(optionsFor(format))
            bridge.from(listNode)

            val mapper = factory.get<T>()
            return bridge.childrenList().mapNotNull { child ->
                runCatching { mapper.load(child) }
                    .onFailure { Logger.error("Failed to load an entry from $path: ${it.message}") }
                    .getOrNull()
            }.toMutableList()
        }

        /**
         * Persists [values] to [path] under [key] (e.g. `{"saves": [...]}`), fully replacing
         * whatever's already on disk - same full-replace semantics as [save], safe to call any
         * time [values] represents the complete, current in-memory list.
         */
        inline fun <reified T : Any> saveList(
            path: String,
            values: List<T>,
            key: String = "saves",
            format: ConfigFormat = ConfigFormat.JSON,
            overwriteParentDir: Boolean = false,
        ) {
            val bridge = BasicConfigurationNode.root(optionsFor(format))
            val bridgeList = bridge.node(key)
            val mapper = factory.get<T>()
            values.forEach { mapper.save(it, bridgeList.appendListNode()) }

            val file = ConfigurateFileUtils.create(path, emptyMap(), format, overwriteParentDir)
            file.node.from(bridge)
            ConfigurateFileUtils.save(file)
        }

        /**
         * Flattens [value] into a `"a.b.c" -> value` map for every leaf (non-nested-object)
         * field, the same shape [ConfigurateFileUtils.flattenToMap] produces for a loaded
         * YAML/JSON file - lets an existing `ConfigFile.getValueAsString("a.b.c")`-style API
         * keep working unchanged on top of a nested `data class`, without every call site
         * needing to be rewritten against the data class's real (nested) shape.
         */
        inline fun <reified T : Any> flatten(value: T, format: ConfigFormat = ConfigFormat.YAML): Map<String, Any> {
            val bridge = BasicConfigurationNode.root(optionsFor(format))
            factory.get<T>().save(value, bridge)
            return ConfigurateFileUtils.flattenToMap(bridge)
        }
    }
}
