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

import de.maxhenkel.voicechat.api.BukkitVoicechatService
import de.maxhenkel.voicechat.api.VoicechatServerApi
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import java.util.UUID

/**
 * Manager for Simple Voice Chat integration.
 * Handles plugin registration and player connection checks.
 */
object SVCManager {
    /**
     * Register the YVtils VoiceChat plugin with Simple Voice Chat.
     * Should be called once during plugin initialization.
     */
    fun registerSVCPlugin() {
        val server = Data.instance.server

        val vcService = server.servicesManager.load(BukkitVoicechatService::class.java)
        if (vcService != null) {
            val voicechatPlugin = SimpleVoiceChat()
            vcService.registerPlugin(voicechatPlugin)

            Logger.info("Successfully registered YVtils with Simple Voice Chat")
        } else {
            Logger.warn("Simple Voice Chat plugin not found! Voice chat features will be disabled.")
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
     * @param api The VoiceChat server API instance
     * @return true if the player has SVC installed and is connected
     */
    fun hasPlayerSVC(uuid: UUID, api: VoicechatServerApi?): Boolean {
        if (api == null) return false
        val connection = api.getConnectionOf(uuid)
        return connection != null
    }
}