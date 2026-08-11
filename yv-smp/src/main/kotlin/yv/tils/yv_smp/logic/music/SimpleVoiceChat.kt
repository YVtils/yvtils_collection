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
import yv.tils.yv_smp.logic.music.svc.VoicechatBridge

/**
 * Bridges YVtils into Simple Voice Chat.
 *
 * IMPORTANT: this class deliberately does NOT implement
 * `de.maxhenkel.voicechat.api.VoicechatPlugin` (nor reference any other
 * voicechat-api type) - see [VoicechatBridge] for why any such static
 * reference would throw `NoClassDefFoundError` at runtime regardless of
 * whether Simple Voice Chat is installed. [VoicechatBridge.createPluginProxy]
 * builds the actual `VoicechatPlugin` instance via a dynamic [java.lang.reflect.Proxy]
 * (loaded through voicechat's own classloader) that delegates to this class.
 */
class SimpleVoiceChat : VoicechatBridge.PluginHandler {
    companion object {
        /** The voicechat `VoicechatApi`/`VoicechatServerApi` instance, kept as `Any` - see class doc. */
        var svcAPI: Any? = null
        const val AUDIO_CATEGORY = "yvtils"  // Volume category for VoiceMod UI
    }

    override fun pluginId(): String = "yvtils"

    override fun onInitialize(api: Any) {
        svcAPI = api
        Logger.info("SVC API initialized successfully.")

        // Initialize MusicHandler now that the API is available
        SVCManager.initializeMusicHandler()
    }

    /**
     * Called when the voice chat server starts.
     * Registers the audio volume category so it appears in VoiceMod UI.
     */
    override fun onServerStarted(event: Any) {
        val icon = createIcon()
        val api = VoicechatBridge.getVoicechatFromEvent(event)

        var builder = VoicechatBridge.volumeCategoryBuilder(api)
        builder = VoicechatBridge.builderSetId(builder, AUDIO_CATEGORY)
        builder = VoicechatBridge.builderSetName(builder, "YVtils")
        builder = VoicechatBridge.builderSetDescription(
            builder,
            "Control volume for custom music played by YVtils plugins"
        )
        builder = VoicechatBridge.builderSetIcon(builder, icon)
        val volumeCategory = VoicechatBridge.builderBuild(builder)

        VoicechatBridge.registerVolumeCategory(api, volumeCategory)

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