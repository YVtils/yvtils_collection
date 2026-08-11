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

package yv.tils.yv_smp

import yv.tils.common.permissions.PermissionManager
import yv.tils.configv2.language.LanguageProvider
import yv.tils.gui.core.InvUIBootstrap
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module
import yv.tils.yv_smp.commands.MusicCommand
import yv.tils.yv_smp.commands.YVSmpCommand
import yv.tils.yv_smp.configs.ConfigFile
import yv.tils.yv_smp.language.LangStrings
import yv.tils.yv_smp.listeners.PlayerQuitListener
import yv.tils.yv_smp.logic.music.MusicHandler
import yv.tils.yv_smp.logic.music.SVCManager
import yv.tils.yv_smp.logic.music.VoiceChatAudioPlayer
import yv.tils.yv_smp.permissions.PermissionsData

class YV_SMPYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "yv_smp",
            Module.readVersion(YV_SMPYVtils::class.java, "yv-smp"),
            "YV SMP module for YVtils",
            "YVtils",
        )
    }

    override fun onLoad() {
        LanguageProvider.registerEnumStrings<LangStrings>()
    }

    override fun enablePlugin() {
        Module.addModule(MODULE)

        // Must run in enablePlugin() (not onLoad()) - see InvUIBootstrap's KDoc for why.
        InvUIBootstrap.ensure()

        registerCommands()
        registerListeners()
        registerCoroutines()
        registerPermissions()

        loadConfigs()
    }

    override fun onLateEnablePlugin() {
        SVCManager.registerSVCPlugin()
    }

    override fun disablePlugin() {
        // Stop all active audio players with a short fade-out
        MusicHandler.stopAll(fadeoutTime = 500L)

        // Shutdown the audio executor service
        VoiceChatAudioPlayer.shutdown()
    }

    private fun registerCommands() {
        YVSmpCommand()
        MusicCommand()
    }

    private fun registerListeners() {
        val plugin = Core.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(PlayerQuitListener(), plugin)
    }

    private fun registerCoroutines() {

    }

    private fun registerPermissions() {
        PermissionManager.registerPermissions(PermissionsData().getPermissionList(true))
    }

    private fun loadConfigs() {
        ConfigFile().loadConfig()
    }
}
