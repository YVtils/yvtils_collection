package yv.tils.multiMine.logic

import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import yv.tils.config.language.LanguageHandler
import yv.tils.multiMine.configs.ConfigFile
import yv.tils.multiMine.configs.MultiMineConfig
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import java.util.*

// TODO: Add fast Leave decay
// TODO: Try making the break process better for the performance

class MultiMineHandler {
    companion object {
        val animationTime = ConfigFile.config["animationTime"] as Int
        val cooldownTime = ConfigFile.config["cooldownTime"] as Int
        val breakLimit = ConfigFile.config["breakLimit"] as Int
        val blocks = ConfigFile.blockList

        val cooldownMap: MutableMap<UUID, Int> = mutableMapOf()
        val brokenMap: MutableMap<UUID, Int> = mutableMapOf()
    }

    fun trigger(e: BlockBreakEvent) {
        val loc = e.block.location
        val player = e.player
        val uuid = player.uniqueId
        val item = player.inventory.itemInMainHand
        val block = e.block

        if (!player.hasPermission("yvtils.use.multiMine")) return
        if (!MultiMineConfig().getPlayerSetting(uuid)) return
        if (!checkBlock(e.block.type, blocks)) return
        if (!checkTool(block, item)) return
        if (checkCooldown(e.player.uniqueId)) return
        if (player.isSneaking) return
        if (player.gameMode != GameMode.SURVIVAL) return

        brokenMap[player.uniqueId] = 0

        setCooldown(player.uniqueId)
        registerBlocks(loc, player, item)
    }

    private var itemBroke = false

    private fun registerBlocks(loc: Location, player: Player, item: ItemStack) {
        if (brokenMap[player.uniqueId]!! >= breakLimit) {
            return
        }

        for (x in -1..1) {
            for (y in -1..1) {
                for (z in -1..1) {
                    if (x == 0 && y == 0 && z == 0) continue
                    val newLoc = Location(loc.world, loc.x + x, loc.y + y, loc.z + z)
                    val newBlock = newLoc.block

                    Bukkit.getScheduler().runTaskLater(Data.instance, Runnable { // TODO: Test if switchable to coroutine
                        if (!breakBlock(newBlock, player, item)) {
                            return@Runnable
                        } else {
                            registerBlocks(newLoc, player, item)
                        }
                    }, animationTime * 1L)
                }
            }
        }
    }

    /**
     * Breaks the blocks
     * @param block The block to break
     * @param player The player who is breaking the block
     * @param item The item the player is using to break the block
     * @return true if the block was broken
     */
    private fun breakBlock(block: Block, player: Player, item: ItemStack): Boolean {
        if (checkBlock(block.type, blocks) && checkTool(block, item)) {
            if (brokenMap[player.uniqueId]!! != 0) {
                try {
                    if (itemBroke) return false

                    if (damageItem(player, 1, item)) {
                        return false
                    }
                } catch (_: NullPointerException) {
                    return false
                }
            }

            brokenMap[player.uniqueId] = brokenMap[player.uniqueId]!! + 1

            block.breakNaturally(item, true, true)
            return true
        }
        return false
    }

    /**
     * Damages the item
     * @param player The player who is breaking the block
     * @param damage The amount of damage to deal to the item
     * @param item The item to damage
     * @return true if the item broke
     */
    private fun damageItem(player: Player, damage: Int, item: ItemStack): Boolean {
        val damageable: Damageable = item.itemMeta as Damageable

        if (damageable.damage + damage >= item.type.maxDurability) {
            itemBroke = true
            player.inventory.removeItem(item)
            player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, 1f, 1f)
            return true
        } else {
            item.damage(damage, player)
            return false
        }
    }

    private fun setCooldown(player: UUID) {
        cooldownMap[player] = cooldownTime
    }

    private fun checkCooldown(player: UUID): Boolean {
        return cooldownMap[player] != null && cooldownMap[player] != 0
    }

    private fun checkBlock(material: Material, blocks: List<Material>): Boolean {
        return blocks.contains(material)
    }

    private fun checkTool(block: Block, tool: ItemStack): Boolean {
        if (tool.type == Material.AIR) return false
        if (tool.type.maxDurability.toInt() == 0) return false

        return block.getDrops(tool).isNotEmpty()
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

    fun cooldownHandler() {
        Logger.debug("Handling multiMine cooldowns...")
        for (entry in cooldownMap) {
            if (entry.value == 0) continue
            cooldownMap[entry.key] = entry.value - 1
        }
    }
}
