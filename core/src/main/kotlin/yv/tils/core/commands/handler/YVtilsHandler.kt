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

package yv.tils.core.commands.handler

import org.bukkit.command.CommandSender
import yv.tils.common.language.LangStrings
import yv.tils.common.updateChecker.PluginVersion
import yv.tils.common.updateChecker.VersionState
import yv.tils.configv2.language.LanguageHandler
import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module

class YVtilsHandler {
    fun pluginInformation(sender: CommandSender) {
        val version = Core.yvtilsVersion
        val modules = Module.getModules(sort = true)
        val moduleNames = if (modules.isEmpty()) "-" else modules.joinToString(", ") { "${it.name} v${it.version}" }

        val updateStatus = when (PluginVersion.versionState) {
            VersionState.UP_TO_DATE -> LanguageHandler.getRawMessage(
                LangStrings.YVTILS_INFO_UPDATE_UP_TO_DATE.key,
                sender
            )

            VersionState.UNKNOWN -> LanguageHandler.getRawMessage(
                LangStrings.YVTILS_INFO_UPDATE_UNKNOWN.key,
                sender
            )

            else -> LanguageHandler.getRawMessage(
                LangStrings.YVTILS_INFO_UPDATE_AVAILABLE.key,
                sender,
                mapOf(
                    "currentVersion" to version,
                    "newVersion" to (PluginVersion.cloudVersion ?: "?"),
                )
            )
        }

        sender.sendMessage(
            LanguageHandler.getMessage(
                LangStrings.YVTILS_INFO.key,
                sender,
                mapOf(
                    "version" to version,
                    "moduleCount" to modules.size,
                    "modules" to moduleNames,
                    "updateStatus" to updateStatus,
                )
            )
        )
    }
}