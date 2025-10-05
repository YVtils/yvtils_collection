package yv.tils.multiMine.logic

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.Tag.REGISTRY_ITEMS
import org.bukkit.block.ShulkerBox
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.BundleMeta
import yv.tils.config.language.LanguageHandler
import yv.tils.multiMine.configs.ConfigFile
import yv.tils.multiMine.configs.MultiMineConfig
import yv.tils.utils.data.Data
import yv.tils.utils.server.VersionUtils


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
        val itemTag = Tag.SHULKER_BOXES
        itemTag.values.forEach {
            if (!shulkerList.contains(it)) {
                shulkerList.add(it)
            }
        }

        return shulkerList
    }

    private fun bundleList(): MutableList<Material> {
        val bundleList = mutableListOf<Material>()
        bundleList.add(Material.BUNDLE)

        if (VersionUtils().isServerVersionAtLeast("1.21.4")) {
            val ITEMS_BUNDLES: Tag<Material?>? = Bukkit.getTag(REGISTRY_ITEMS, NamespacedKey.minecraft("bundles"), Material::class.java)
            if (ITEMS_BUNDLES != null) {
                for (item in ITEMS_BUNDLES.values) {
                    if (!bundleList.contains(item) && item != null) {
                        bundleList.add(item)
                    }
                }
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
        if (!checkMultipleLogic(sender)) return
        val sender = sender as Player

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
        if (!checkMultipleLogic(sender)) return
        val sender = sender as Player

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

    private fun checkMultipleLogic(sender: CommandSender): Boolean {
        if (sender !is Player) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    "command.multiMine.multiple.console",
                    sender,
                    mapOf("prefix" to Data.prefix)
                )
            )
            return false
        }

        if (!checkForContainer(sender.inventory.itemInMainHand.type)) {
            sender.sendMessage(
                LanguageHandler.getMessage(
                    "command.multiMine.multiple.noContainer",
                    sender,
                    mapOf("prefix" to Data.prefix)
                )
            )
            return false
        }

        return true
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
