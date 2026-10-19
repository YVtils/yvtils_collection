package yv.tils.vanish.gui

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.window.Window
import yv.tils.gui.logic.DataClassConfigGui
import yv.tils.gui.utils.Filler
import yv.tils.gui.utils.GuiStyle
import yv.tils.vanish.configs.ConfigFile
import yv.tils.vanish.configs.PlayerSettings
import yv.tils.vanish.language.VanishText
import yv.tils.vanish.logic.VanishService
import yv.tils.vanish.permissions.VanishPermissions
import yv.tils.utils.logger.Logger
import yv.tils.utils.message.MessageUtils

class SettingsGui(private val service: VanishService) {
    private fun control(material: Material, key: String, value: Any, click: (Player) -> Unit): Item =
        Item.builder().setItemProvider { viewer ->
            ItemWrapper(ItemStack(material).apply {
                editMeta { meta ->
                    meta.displayName(GuiStyle.title(VanishText.text(viewer, key)))
                    meta.lore(listOf(GuiStyle.lore(VanishText.text(viewer, "value", mapOf("value" to value)))))
                }
            })
        }.addClickHandler { _, event ->
            val actor = event.player()
            if (!actor.hasPermission(VanishPermissions.USE)) {
                VanishText.send(actor, "denied"); return@addClickHandler
            }
            try {
                click(actor)
            } catch (error: Exception) {
                Logger.error("Could not save Vanish preferences", error)
                VanishText.send(actor, "failure")
            }
        }.build()

    fun open(player: Player) {
        if (!player.hasPermission(VanishPermissions.USE)) {
            VanishText.send(player, "denied"); return
        }
        val settings = PlayerSettings.get(player.uniqueId)
        val features = listOf(
            Triple(VanishPermissions.PICKUP, "pickup", Material.HOPPER),
            Triple(VanishPermissions.SILENT_CONTAINERS, "silentContainers", Material.CHEST),
            Triple(VanishPermissions.MOB_TARGET, "mobs", Material.SPAWNER),
            Triple(VanishPermissions.DROP, "drop", Material.DROPPER),
            Triple(VanishPermissions.DAMAGE, "damage", Material.IRON_SWORD),
            Triple(VanishPermissions.PHYSICAL, "physical", Material.STONE_PRESSURE_PLATE),
            Triple(VanishPermissions.COLLISION, "collision", Material.ARMOR_STAND)
        )
        val toggleMaterial: (Boolean) -> Material = { if (it) Material.LIME_DYE else Material.RED_DYE }
        val gui = Gui.builder()
            .setStructure(
                "# # # # # # # # #", "# V # A # I J K #", "# v # a # 1 2 3 #",
                "# # # # # # # # #", "# L # M N O P # #", "# l # 4 5 6 7 # #"
            )
            .addIngredient('#', Filler.item())
            .addIngredient('V', control(Material.GLASS_BOTTLE, "toggle", service.hidden(player.uniqueId)) {})
            .addIngredient('A', control(Material.MAP, "announce", settings.announce) {})
            .addIngredient('L', control(Material.EXPERIENCE_BOTTLE, "layer", settings.layer) {})
            .addIngredient('a', control(toggleMaterial(settings.announce), "announce", settings.announce) { actor ->
                val current = PlayerSettings.get(actor.uniqueId)
                PlayerSettings.save(actor, current.copy(announce = !current.announce)); open(actor)
            })
            .addIngredient('l', control(Material.EXPERIENCE_BOTTLE, "layer", settings.layer) { layers(it) })
            .addIngredient(
                'v',
                control(toggleMaterial(service.hidden(player.uniqueId)), "toggle", service.hidden(player.uniqueId)) {
                    service.toggle(it, PlayerSettings.get(it.uniqueId).layer); open(it)
                })
        features.forEachIndexed { index, (permission, key, icon) ->
            val iconKey = "IJKMNOP"[index]
            gui.addIngredient(
                iconKey,
                if (player.hasPermission(permission)) control(
                    icon,
                    key,
                    settings.enabled(permission)
                ) {} else Filler.item())
            gui.addIngredient(
                ('1'.code + index).toChar(), if (player.hasPermission(permission))
                    control(
                        toggleMaterial(settings.enabled(permission)),
                        key,
                        settings.enabled(permission)
                    ) { actor ->
                        if (!actor.hasPermission(permission)) {
                            VanishText.send(actor, "denied"); return@control
                        }
                        PlayerSettings.save(actor, PlayerSettings.get(actor.uniqueId).toggle(permission))
                        service.refresh(); open(actor)
                    } else Filler.item())
        }
        Window.builder().setTitle(GuiStyle.title(VanishText.text(player, "settings"))).setUpperGui(gui.build())
            .open(player)
    }

    private fun layers(player: Player) {
        if (!player.hasPermission(VanishPermissions.LAYERS)) {
            VanishText.send(player, "denied"); return
        }
        val items =
            (1..ConfigFile.state.maxLayer).filter { player.hasPermission(VanishPermissions.layer(it)) }.map { layer ->
                control(Material.EXPERIENCE_BOTTLE, "layer", layer) { actor ->
                    PlayerSettings.save(actor, PlayerSettings.get(actor.uniqueId).copy(layer = layer))
                    service.changeLayer(actor, layer); open(actor)
                }
            }
        DataClassConfigGui.openItems(
            player,
            MessageUtils.strip(VanishText.text(player, "layer")),
            items
        ) { open(player) }
    }
}
