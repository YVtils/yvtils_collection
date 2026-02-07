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

package yv.tils.multiMine.logic

import com.destroystokyo.paper.profile.PlayerProfile
import dev.jorel.commandapi.executors.CommandArguments
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import yv.tils.common.language.LangStrings
import yv.tils.config.language.LanguageHandler
import yv.tils.multiMine.configs.MultiMineConfig
import yv.tils.multiMine.data.Permissions
import yv.tils.multiMine.utils.BlockUtils
import yv.tils.multiMine.utils.BlockUtils.Companion.blocks
import yv.tils.multiMine.utils.CooldownUtils
import yv.tils.multiMine.utils.ToolUtils
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class MultiMineHandler {
    fun trigger(e: BlockBreakEvent) {
        val loc = e.block.location
        val player = e.player
        val uuid = player.uniqueId
        val item = player.inventory.itemInMainHand
        val block = e.block

        if (!player.hasPermission(Permissions.USE_MULTIMINE.permission.name)) return
        if (!MultiMineConfig().getPlayerSetting(uuid)) return

        cleanup(player)

        if (!BlockUtils().checkBlock(e.block.type, blocks, player)) return
        if (!ToolUtils().checkTool(block, item)) return
        if (CooldownUtils().checkCooldown(e.player.uniqueId)) return
        if (player.isSneaking) return
        if (player.gameMode != GameMode.SURVIVAL) return

        BlockUtils.brokenMap[player.uniqueId]?.store(0)

        CooldownUtils().setCooldown(player.uniqueId)
        BlockUtils().registerBlocks(loc, player, item)
    }

    fun cleanup(player: Player) {
        val uuid = player.uniqueId
        BlockUtils.brokenMap.remove(uuid)
        BlockUtils.playerBlockTypeMap.remove(uuid)
        BlockUtils.runningProcessesMap.remove(uuid)
        BlockUtils.processFinishedMap.remove(uuid)
    }

    fun toggle(sender: CommandSender, args: CommandArguments) {
        if (args["target"] == null) {
            if (sender is Player) {
                val uuid = sender.uniqueId
                val value = MultiMineConfig().getPlayerSetting(uuid)
                Logger.debug("Toggling multiMine for player $uuid: \nOld value: $value \nNew value: ${!value}")
                MultiMineConfig().updatePlayerSetting(uuid, !value)

                val langMessageState = if (!value) "command.multiMine.activate" else "command.multiMine.deactivate"

                sender.sendMessage(
                    LanguageHandler.getMessage(
                        "$langMessageState.self",
                        sender,
                        mapOf("prefix" to Data.prefix)
                    )
                )
            } else {
                sender.sendMessage(
                    LanguageHandler.getMessage(
                        LangStrings.COMMAND_MISSING_PLAYER.key,
                    )
                )
            }
        } else {
            @Suppress("UNCHECKED_CAST")
            val targets = args["target"] as List<PlayerProfile>

            for (target in targets) {
                val uuid = target.id ?: continue  // TODO: Think about implementing a message for this case
                val value = MultiMineConfig().getPlayerSetting(uuid)

                Logger.debug("Toggling multiMine for player $uuid by ${sender.name}: \nOld value: $value \nNew value: ${!value}")

                MultiMineConfig().updatePlayerSetting(uuid, !value)

                val langMessageState = if (!value) "command.multiMine.activate" else "command.multiMine.deactivate"

                // Check if target is online to send them a message
                val onlinePlayer = Data.instance.server.getPlayer(uuid)
                onlinePlayer?.sendMessage(
                    LanguageHandler.getMessage(
                        "$langMessageState.self",
                        sender
                    )
                )

                sender.sendMessage(
                    LanguageHandler.getMessage(
                        "$langMessageState.other",
                        sender,
                        mapOf(
                            "player" to (target.name ?: "Unknown")
                        )
                    )
                )
            }
        }
    }
}
