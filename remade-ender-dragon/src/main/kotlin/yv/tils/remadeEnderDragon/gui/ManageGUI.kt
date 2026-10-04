/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.remadeEnderDragon.gui

import org.bukkit.Material
import org.bukkit.entity.Player
import xyz.xenondevs.invui.item.Item
import yv.tils.gui.core.InvUIBootstrap
import yv.tils.gui.logic.DataClassConfigGui
import yv.tils.gui.utils.GuiStyle
import yv.tils.remadeEnderDragon.configs.*
import yv.tils.remadeEnderDragon.data.Permissions
import yv.tils.remadeEnderDragon.language.Messages
import yv.tils.remadeEnderDragon.logic.FightManager
import yv.tils.utils.logger.Logger

/** Shared InvUI editor for scalar/list values, with module-local enum/map navigation. */
class ManageGUI(private val manager: FightManager) {
    private fun allowed(player: Player): Boolean {
        if (player.hasPermission(Permissions.CONFIG.permission.name)) return true
        Messages.send(player, "no-permission")
        return false
    }

    fun openGUI(player: Player) {
        if (!allowed(player)) return
        InvUIBootstrap.ensure()
        val items = listOf(
            button(player, Material.COMPARATOR, "gui.general") {
                val draft = ConfigFile.state.copy(attacks = ConfigFile.state.attacks.mapValues { it.value.copy() })
                DataClassConfigGui.openWithBack(player, Messages.plain("gui.general", player), draft,
                    saver = { updated -> save(player) {
                        // The separate mode/attack screens may have been saved by another editor.
                        manager.applyConfiguration(updated.copy(activation = ConfigFile.state.activation, attacks = ConfigFile.state.attacks))
                    } }, back = { openGUI(player) })
            },
            Item.builder().setItemProvider {
                GuiStyle.field(Material.END_CRYSTAL,
                    Messages.text("gui.activation", player, mapOf("mode" to Messages.plain("mode.${ConfigFile.state.activation.name.lowercase()}", player))),
                    listOf(Messages.text("gui.activation-lore", player)))
            }.addClickHandler { _, _ ->
                save(player) {
                    val current = ConfigFile.state
                    val next = Activation.entries[(current.activation.ordinal + 1) % Activation.entries.size]
                    manager.applyConfiguration(current.copy(activation = next))
                }
                openGUI(player)
            }.build(),
            button(player, Material.DRAGON_HEAD, "gui.attacks") { openAttacks(player) },
        )
        DataClassConfigGui.openItems(player, Messages.plain("gui.title", player), items) { player.closeInventory() }
    }

    private fun openAttacks(player: Player) {
        if (!allowed(player)) return
        val items = Attack.entries.map { attack ->
            Item.builder().setItemProvider {
                val s = ConfigFile.state.settings(attack)
                GuiStyle.field(Material.END_CRYSTAL, Messages.text("attack.${attack.id}", player),
                    listOf(Messages.text("gui.attack-lore", player,
                        mapOf("enabled" to s.enabled, "uses" to s.maxUses, "cooldown" to s.cooldownSeconds))))
            }.addClickHandler { _, _ ->
                if (!allowed(player)) return@addClickHandler
                val draft = ConfigFile.state.settings(attack).copy()
                DataClassConfigGui.openWithBack(player, Messages.plain("attack.${attack.id}", player), draft,
                    saver = { updated -> save(player) {
                        manager.applyConfiguration(ConfigFile.state.copy(attacks = ConfigFile.state.attacks + (attack.id to updated.copy())))
                    } }, back = { openAttacks(player) })
            }.build()
        }
        DataClassConfigGui.openItems(player, Messages.plain("gui.attacks", player), items) { openGUI(player) }
    }

    private fun button(player: Player, material: Material, key: String, click: () -> Unit): Item =
        Item.builder().setItemProvider { GuiStyle.field(material, Messages.text(key, player), listOf(Messages.text("gui.edit-lore", player))) }
            .addClickHandler { _, _ -> if (allowed(player)) click() }.build()

    private fun save(player: Player, apply: () -> Unit) {
        if (!allowed(player)) return
        runCatching(apply).onSuccess {
            Logger.info("[Remade Ender Dragon] Configuration updated by ${player.name} (${player.uniqueId}).")
            Messages.send(player, "saved")
        }.onFailure {
            Logger.warn("[Remade Ender Dragon] Rejected configuration edit by ${player.name}: ${it.message}")
            Messages.send(player, "reload-failed", mapOf("error" to (it.message ?: it.javaClass.simpleName)))
        }
    }
}
