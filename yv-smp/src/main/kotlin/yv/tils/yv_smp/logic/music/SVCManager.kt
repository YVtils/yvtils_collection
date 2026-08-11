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

package yv.tils.yv_smp.logic.music

import yv.tils.utils.logger.Logger
import yv.tils.utils.modules.Core
import yv.tils.yv_smp.logic.music.svc.VoicechatBridge
import java.util.*

/**
 * Manager for Simple Voice Chat integration.
 * Handles plugin registration and player connection checks.
 *
 * NOTE: this deliberately never references any `de.maxhenkel.voicechat.api.*`
 * type directly - see [VoicechatBridge] for why doing so would throw
 * `NoClassDefFoundError` at runtime regardless of whether Simple Voice Chat
 * is installed (this module is fetched dynamically and lives in a
 * classloader tier that cannot see other plugins' classes).
 */
object SVCManager {
    /**
     * Register the YVtils VoiceChat plugin with Simple Voice Chat.
     * Should be called once during plugin initialization.
     */
    fun registerSVCPlugin() {
        val server = Core.instance.server

        val svcPlugin = server.pluginManager.getPlugin("voicechat")
        if (svcPlugin == null || !svcPlugin.isEnabled) {
            Logger.warn("Simple Voice Chat plugin not found! Voice chat features will be disabled.")
            MusicHandler.initialize(null)
            return
        }

        try {
            VoicechatBridge.initialize(svcPlugin.javaClass.classLoader)

            val vcService = server.servicesManager.load(VoicechatBridge.bukkitVoicechatServiceClass())
            if (vcService == null) {
                Logger.warn("Simple Voice Chat service not available! Voice chat features will be disabled.")
                MusicHandler.initialize(null)
                return
            }

            val voicechatPlugin = VoicechatBridge.createPluginProxy(SimpleVoiceChat())
            VoicechatBridge.registerPlugin(vcService, voicechatPlugin)

            Logger.info("Successfully registered YVtils with Simple Voice Chat")
        } catch (e: ReflectiveOperationException) {
            Logger.error(
                "Failed to bridge into the Simple Voice Chat API (mismatched/incompatible version?). Voice chat features will be disabled.",
                e
            )
            MusicHandler.initialize(null)
        } catch (e: NoClassDefFoundError) {
            Logger.error(
                "Simple Voice Chat API classes could not be loaded (mismatched/incompatible version?). Voice chat features will be disabled.",
                e
            )
            MusicHandler.initialize(null)
        }
    }

    /**
     * Initialize MusicHandler with the VoiceChat API.
     * This is called from SimpleVoiceChat after the API is initialized.
     */
    fun initializeMusicHandler() {
        MusicHandler.initialize(SimpleVoiceChat.svcAPI)
    }

    /**
     * Check if a player has Simple Voice Chat installed and connected.
     *
     * @param uuid The player's UUID
     * @param api The VoiceChat server API instance (kept as `Any` - see [VoicechatBridge])
     * @return true if the player has SVC installed and is connected
     */
    fun hasPlayerSVC(uuid: UUID, api: Any?): Boolean {
        if (api == null) return false
        val connection = VoicechatBridge.getConnectionOf(api, uuid)
        return connection != null
    }
}