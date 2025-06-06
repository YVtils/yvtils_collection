package yv.tils.multiMine.logic

import data.Data
import language.LanguageHandler
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.BundleMeta
import server.VersionUtils
import yv.tils.multiMine.configs.ConfigFile
import yv.tils.multiMine.configs.MultiMineConfig

class BlockManage {
    fun addBlock(sender: CommandSender, block: Any?) {
        val block = block as ItemStack?
        var material = block?.type
        if (sender !is Player) {
            if (material == null) {
                sender.sendMessage(LanguageHandler.getMessage(
                    "command.multiMine.noBlock.asParam",
                    sender,
                    mapOf("prefix" to Data.prefix)
                ))
                return
            }

            val b = modifyBlockList("+", material, sender)
            if (b) {
                sender.sendMessage(LanguageHandler.getMessage(
                    "command.multiMine.block.added",
                    sender,
                    mapOf("prefix" to Data.prefix, "block" to material.name)
                ))
            }
        } else {
            if (material == null) {
                if (sender.inventory.itemInMainHand.type == Material.AIR) {
                    sender.sendMessage(LanguageHandler.getMessage(
                        "command.multiMine.noBlock.inHand",
                        sender,
                        mapOf("prefix" to Data.prefix)
                    ))
                    return
                } else {
                    material = sender.inventory.itemInMainHand.type
                }
            }

            val b = modifyBlockList("+", material, sender)
            if (b) {
                sender.sendMessage(LanguageHandler.getMessage(
                    "command.multiMine.block.added",
                    sender,
                    mapOf("prefix" to Data.prefix, "block" to material.name)
                ))
            }
        }
    }

    fun removeBlock(sender: CommandSender, block: Any?) {
        val block = block as ItemStack?
        var material = block?.type
        if (sender !is Player) {
            if (material == null) {
                sender.sendMessage(LanguageHandler.getMessage(
                    "command.multiMine.noBlock.asParam",
                    sender,
                    mapOf("prefix" to Data.prefix)
                ))
                return
            }

            val b = modifyBlockList("-", material, sender)
            if (b) {
                sender.sendMessage(LanguageHandler.getMessage(
                    "command.multiMine.block.removed",
                    sender,
                    mapOf("prefix" to Data.prefix, "block" to material.name)
                ))
            }
        } else {
            if (material == null) {
                if (sender.inventory.itemInMainHand.type == Material.AIR) {
                    sender.sendMessage(LanguageHandler.getMessage(
                        "command.multiMine.noBlock.inHand",
                        sender,
                        mapOf("prefix" to Data.prefix)
                    ))
                    return
                } else {
                    material = sender.inventory.itemInMainHand.type
                }
            }

            val b = modifyBlockList("-", material, sender)

            if (b) {
                sender.sendMessage(LanguageHandler.getMessage(
                    "command.multiMine.block.removed",
                    sender,
                    mapOf("prefix" to Data.prefix, "block" to material.name)
                ))
            }
        }
    }

    private fun checkForContainer(item: Material): Boolean {
        val containerList = mutableListOf<Material>()
        containerList.addAll(shulkerList())
        containerList.addAll(bundleList())

        return containerList.contains(item)
    }

    private fun shulkerList(): MutableList<Material> {
        val shulkerList = mutableListOf<Material>()
        shulkerList.add(Material.SHULKER_BOX)
        shulkerList.add(Material.BLACK_SHULKER_BOX)
        shulkerList.add(Material.BLUE_SHULKER_BOX)
        shulkerList.add(Material.BROWN_SHULKER_BOX)
        shulkerList.add(Material.CYAN_SHULKER_BOX)
        shulkerList.add(Material.GRAY_SHULKER_BOX)
        shulkerList.add(Material.GREEN_SHULKER_BOX)
        shulkerList.add(Material.LIGHT_BLUE_SHULKER_BOX)
        shulkerList.add(Material.LIGHT_GRAY_SHULKER_BOX)
        shulkerList.add(Material.LIME_SHULKER_BOX)
        shulkerList.add(Material.MAGENTA_SHULKER_BOX)
        shulkerList.add(Material.ORANGE_SHULKER_BOX)
        shulkerList.add(Material.PINK_SHULKER_BOX)
        shulkerList.add(Material.PURPLE_SHULKER_BOX)
        shulkerList.add(Material.RED_SHULKER_BOX)
        shulkerList.add(Material.WHITE_SHULKER_BOX)
        shulkerList.add(Material.YELLOW_SHULKER_BOX)
        return shulkerList
    }

    private fun bundleList(): MutableList<Material> {
        val bundleList = mutableListOf<Material>()
        bundleList.add(Material.BUNDLE)

        when (VersionUtils.serverVersion) {
            "1.21.4" -> {
                bundleList.add(Material.valueOf("BLACK_BUNDLE"))
                bundleList.add(Material.valueOf("BLUE_BUNDLE"))
                bundleList.add(Material.valueOf("BROWN_BUNDLE"))
                bundleList.add(Material.valueOf("CYAN_BUNDLE"))
                bundleList.add(Material.valueOf("GRAY_BUNDLE"))
                bundleList.add(Material.valueOf("GREEN_BUNDLE"))
                bundleList.add(Material.valueOf("LIGHT_BLUE_BUNDLE"))
                bundleList.add(Material.valueOf("LIGHT_GRAY_BUNDLE"))
                bundleList.add(Material.valueOf("LIME_BUNDLE"))
                bundleList.add(Material.valueOf("MAGENTA_BUNDLE"))
                bundleList.add(Material.valueOf("ORANGE_BUNDLE"))
                bundleList.add(Material.valueOf("PINK_BUNDLE"))
                bundleList.add(Material.valueOf("PURPLE_BUNDLE"))
                bundleList.add(Material.valueOf("RED_BUNDLE"))
                bundleList.add(Material.valueOf("WHITE_BUNDLE"))
                bundleList.add(Material.valueOf("YELLOW_BUNDLE"))
            }
        }

        return bundleList
    }

    private fun loadContainerContent(container: ItemStack): List<Material> {
        val content = mutableListOf<Material>()

        if (bundleList().contains(container.type)) {
            val bundle = container.itemMeta as BundleMeta
            for (item in bundle.items) {
                content.add(item.type)
            }
        } else {
            val shulkerBox = container.itemMeta as BlockStateMeta
            val inventory = shulkerBox.blockState as ShulkerBox
            for (item in inventory.inventory.contents) {
                if (item != null) {
                    content.add(item.type)
                }
            }
        }

        return content
    }

    fun addMultiple(sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(LanguageHandler.getMessage(
                "command.multiMine.multiple.console",
                sender,
                mapOf("prefix" to Data.prefix)
            ))
            return
        }

        if (!checkForContainer(sender.inventory.itemInMainHand.type)) {
            sender.sendMessage(LanguageHandler.getMessage(
                "command.multiMine.multiple.noContainer",
                sender,
                mapOf("prefix" to Data.prefix)
            ))
            return
        }

        val blocks = mutableListOf<Material>()

        for (block in loadContainerContent(sender.inventory.itemInMainHand)) {
            val b = modifyBlockList("+", block, sender)
            if (!b) {
                continue
            }

            blocks.add(block)
        }

        sender.sendMessage(LanguageHandler.getMessage(
            "command.multiMine.multiple.added",
            sender,
            mapOf("prefix" to Data.prefix, "blocks" to blocks.joinToString(", ") { it.name })
        ))
    }

    fun removeMultiple(sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(LanguageHandler.getMessage(
                "command.multiMine.multiple.console",
                sender,
                mapOf("prefix" to Data.prefix)
            ))
            return
        }

        if (!checkForContainer(sender.inventory.itemInMainHand.type)) {
            sender.sendMessage(LanguageHandler.getMessage(
                "command.multiMine.multiple.noContainer",
                sender,
                mapOf("prefix" to Data.prefix)
            ))
            return
        }

        val blocks = mutableListOf<Material>()

        for (block in loadContainerContent(sender.inventory.itemInMainHand)) {
            val b = modifyBlockList("-", block, sender)
            if (!b) {
                continue
            }

            blocks.add(block)
        }

        sender.sendMessage(LanguageHandler.getMessage(
            "command.multiMine.multiple.removed",
            sender,
            mapOf("prefix" to Data.prefix, "blocks" to blocks.joinToString(", ") { it.name })
        ))
    }

    private fun modifyBlockList(identifier: String, block: Material, sender: CommandSender): Boolean {
        val blocks = ConfigFile.blockList

        if (identifier == "+") {
            if (blocks.contains(block)) {
                sender.sendMessage(LanguageHandler.getMessage(
                    "command.multiMine.block.alreadyInList",
                    sender,
                    mapOf("prefix" to Data.prefix, "block" to block.name)
                ))
                return false
            }

            blocks.add(block)
        } else if (identifier == "-") {
            if (!blocks.contains(block)) {
                sender.sendMessage(LanguageHandler.getMessage(
                    "command.multiMine.block.notInList",
                    sender,
                    mapOf("prefix" to Data.prefix, "block" to block.name)
                ))
                return false
            }

            blocks.remove(block)
        }
        MultiMineConfig().updateBlockList(blocks)

        return true
    }

    fun openGUI(sender: Player) {

    }
}