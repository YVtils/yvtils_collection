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

import de.maxhenkel.voicechat.api.VoicechatApi
import de.maxhenkel.voicechat.api.VoicechatPlugin
import de.maxhenkel.voicechat.api.events.EventRegistration
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent
import yv.tils.utils.logger.Logger

class SimpleVoiceChat : VoicechatPlugin {
    companion object {
        var svcAPI: VoicechatApi? = null
        const val AUDIO_CATEGORY = "yvtils"  // Volume category for VoiceMod UI
    }

    override fun getPluginId(): String {
        return "yvtils"
    }

    override fun initialize(api: VoicechatApi) {
        svcAPI = api
        Logger.info("SVC API initialized successfully.")

        // Initialize MusicHandler now that the API is available
        SVCManager.initializeMusicHandler()
    }

    override fun registerEvents(registration: EventRegistration) {
        registration.registerEvent(VoicechatServerStartedEvent::class.java, this::onServerStarted)
    }

    /**
     * Called when the voice chat server starts.
     * Registers the audio volume category so it appears in VoiceMod UI.
     */
    private fun onServerStarted(event: VoicechatServerStartedEvent) {
        val icon = createIcon()

        val volumeCategory = event.voicechat.volumeCategoryBuilder()
            .setId(AUDIO_CATEGORY)
            .setName("YVtils")
            .setDescription("Control volume for custom music played by YVtils plugins")
            .setIcon(icon)
            .build()

        event.voicechat.registerVolumeCategory(volumeCategory)

        Logger.info("Registered VoiceMod volume category: $AUDIO_CATEGORY")
    }

    private fun createIcon(): Array<IntArray> {
        val icon = Array(16) { IntArray(16) { 0x00000000 } }

        val mainColor = 0xFF5F795B.toInt()
        val secondColor = 0xFFD6E0C6.toInt()
        val thirdColor = 0xFF88D07C.toInt()

        // Row 0 empty

        // Row 1
        // 0 - 12 empty
        // 14 - 15 empty
        icon[1][13] = mainColor

        // Row 2
        // 0 - 11 empty
        // 14 - 15 empty
        icon[2][12] = mainColor
        icon[2][13] = secondColor

        // Row 3
        // 0 - 5 empty
        // 8 - 10 empty
        // 14 - 15 empty
        icon[3][6] = mainColor
        icon[3][7] = mainColor
        icon[3][11] = mainColor
        icon[3][12] = secondColor
        icon[3][13] = secondColor

        // Row 4
        // 0 - 1 empty
        // 9 empty
        // 14 - 15 empty
        icon[4][2] = secondColor
        icon[4][3] = secondColor
        icon[4][4] = secondColor
        icon[4][5] = mainColor
        icon[4][6] = secondColor
        icon[4][7] = mainColor
        icon[4][8] = mainColor
        icon[4][10] = mainColor
        icon[4][11] = secondColor
        icon[4][12] = secondColor
        icon[4][13] = secondColor

        // Row 5
        // 0 empty
        // 2 - 4 empty
        // 13 - 15 empty
        icon[5][1] = secondColor
        icon[5][5] = mainColor
        icon[5][6] = mainColor
        icon[5][7] = thirdColor
        icon[5][8] = secondColor
        icon[5][9] = mainColor
        icon[5][10] = mainColor
        icon[5][11] = secondColor
        icon[5][12] = secondColor

        // Row 6
        // 0 - 5 empty
        // 13 - 15 empty
        icon[6][6] = thirdColor
        icon[6][7] = thirdColor
        icon[6][8] = mainColor
        icon[6][9] = mainColor
        icon[6][10] = secondColor
        icon[6][11] = secondColor
        icon[6][12] = secondColor

        // Row 7
        // 0 - 5 empty
        // 12 - 15 empty
        icon[7][6] = thirdColor
        icon[7][7] = thirdColor
        icon[7][8] = secondColor
        icon[7][9] = mainColor
        icon[7][10] = secondColor
        icon[7][11] = secondColor

        // Row 8
        // 0 - 5 empty
        // 12 - 15 empty
        icon[8][6] = thirdColor
        icon[8][7] = thirdColor
        icon[8][8] = secondColor
        icon[8][9] = mainColor
        icon[8][10] = mainColor
        icon[8][11] = secondColor

        // Row 9
        // 0 - 6 empty
        // 12 - 15 empty
        icon[9][7] = thirdColor
        icon[9][8] = thirdColor
        icon[9][9] = secondColor
        icon[9][10] = mainColor
        icon[9][11] = mainColor

        // Row 10
        // 0 - 7 empty
        // 12 - 15 empty
        icon[10][8] = thirdColor
        icon[10][9] = thirdColor
        icon[10][10] = secondColor
        icon[10][11] = mainColor

        // Row 11
        // 0 - 9 empty
        // 12 - 15 empty
        icon[11][10] = mainColor
        icon[11][11] = mainColor

        // Row 12
        // 0 - 9 empty
        // 12 - 15 empty
        icon[12][10] = mainColor
        icon[12][11] = mainColor

        // Row 13
        // 0 - 10 empty
        // 12 - 15 empty
        icon[13][11] = mainColor

        // Row 14
        // 0 - 15 empty

        // Row 15
        // 0 - 15 empty

        return icon
    }
}