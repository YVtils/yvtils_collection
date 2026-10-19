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

package yv.tils.status

import yv.tils.gui.core.InvUIBootstrap
import yv.tils.status.commands.StatusCommand
import yv.tils.status.configs.ConfigFile
import yv.tils.status.configs.ManageGUI
import yv.tils.status.configs.SaveFile
import yv.tils.status.language.RegisterStrings
import yv.tils.status.listeners.PlayerJoin
import yv.tils.status.listeners.PlayerQuit
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module
import java.util.function.Function
import net.kyori.adventure.text.Component
import yv.tils.utils.message.MessageUtils
import yv.tils.utils.modules.ConnectionMessages

class StatusYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "status",
            Module.readVersion(StatusYVtils::class.java, "status"),
            "Status module for YVtils",
            "YVtils",
            "https://docs.yvtils.net/status/",
            configGuiOpener = { player -> ManageGUI().openGUI(player) },
        )
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
    }

    override fun enablePlugin() {
        InvUIBootstrap.ensure()

        Module.addModule(MODULE)

        registerCommands()
        registerListeners()
        registerCoroutines()

        loadConfigs()
        ConnectionMessages.displayName = Function { player ->
            val status = SaveFile.saves[player.uniqueId]?.content.orEmpty()
            if (status.isBlank()) Component.text(player.name)
            else MessageUtils.replacer(
                ConfigFile.config["display"] as String,
                mapOf("status" to status, "playerName" to player.name)
            )
        }
    }

    override fun onLateEnablePlugin() {

    }

    override fun disablePlugin() {
        ConnectionMessages.displayName = Function {
            Component.text(it.name)
        }
    }

    private fun registerCommands() {
        StatusCommand()
    }

    private fun registerListeners() {
        val plugin = Core.instance
        val pm = plugin.server.pluginManager

        pm.registerEvents(PlayerJoin(), plugin)
        pm.registerEvents(PlayerQuit(), plugin)
    }

    private fun registerCoroutines() {

    }

    private fun registerPermissions() {
        Core.instance.server.pluginManager

    }

    private fun loadConfigs() {
        ConfigFile().loadConfig()
        SaveFile().loadConfig()
    }
}
