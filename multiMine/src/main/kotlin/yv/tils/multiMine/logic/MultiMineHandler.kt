package yv.tils.multiMine.logic

import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import yv.tils.config.language.LanguageHandler
import yv.tils.multiMine.configs.MultiMineConfig
import yv.tils.multiMine.data.Permissions
import yv.tils.multiMine.utils.BlockUtils
import yv.tils.multiMine.utils.BlockUtils.Companion.blocks
import yv.tils.multiMine.utils.BlockUtils.Companion.brokenMap
import yv.tils.multiMine.utils.CooldownUtils
import yv.tils.multiMine.utils.ToolUtils
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger

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

        brokenMap[player.uniqueId] = 0

        CooldownUtils().setCooldown(player.uniqueId)
        BlockUtils().registerBlocks(loc, player, item)
    }

    fun cleanup(player: Player) {
        val uuid = player.uniqueId
        brokenMap[uuid] = 0
        BlockUtils.playerBlockTypeMap.remove(uuid)
        BlockUtils.runningProcessesMap.remove(uuid)
        BlockUtils.processFinishedMap.remove(uuid)
    }

    fun toggle(sender: Player) {
        val uuid = sender.uniqueId
        val value = MultiMineConfig().getPlayerSetting(uuid)

        Logger.debug("Toggling multiMine for player $uuid: \nOld value: $value \nNew value: ${!value}")

        MultiMineConfig().updatePlayerSetting(uuid, !value)

        sender.sendMessage(
            if (!value) {
                LanguageHandler.getMessage(
                    "command.multiMine.activate",
                    sender,
                    mapOf("prefix" to Data.prefix)
                )
            } else {
                LanguageHandler.getMessage(
                    "command.multiMine.deactivate",
                    sender,
                    mapOf("prefix" to Data.prefix)
                )
            }
        )
    }
}
