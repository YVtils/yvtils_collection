/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.moderation.gui

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.Window
import yv.tils.configv2.language.LanguageHandler
import yv.tils.gui.utils.Filler
import yv.tils.gui.utils.GuiStyle
import yv.tils.moderation.configs.saveFile.MuteSaveFile
import yv.tils.moderation.configs.saveFile.WarnSaveFile
import yv.tils.moderation.data.Permissions
import yv.tils.moderation.logic.*
import yv.tils.moderation.utils.TargetUtils
import yv.tils.utils.colors.Colors
import java.util.UUID
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Inventory actions reuse the command logic, on the server thread, with permissions rechecked. */
class PlayerGUI {
    companion object {
        fun knownPlayers(): List<OfflinePlayer> = (Bukkit.getOfflinePlayers().toList() +
                Bukkit.getOnlinePlayers() +
                (MuteSaveFile.saves.keys + WarnSaveFile.saves.keys).map(Bukkit::getOfflinePlayer))
            .distinctBy { it.uniqueId }.sortedWith(compareByDescending<OfflinePlayer> { it.isOnline }
                .thenBy { it.name ?: it.uniqueId.toString() })
    }

    private enum class Action(val permission: Permissions, val material: Material) {
        BAN(Permissions.COMMAND_MODERATION_BAN, Material.BARRIER),
        TEMPBAN(Permissions.COMMAND_MODERATION_TEMPBAN, Material.CLOCK),
        KICK(Permissions.COMMAND_MODERATION_KICK, Material.IRON_BOOTS),
        MUTE(Permissions.COMMAND_MODERATION_MUTE, Material.RED_DYE),
        TEMPMUTE(Permissions.COMMAND_MODERATION_TEMPMUTE, Material.CLOCK),
        WARN(Permissions.COMMAND_MODERATION_WARN, Material.PAPER),
        UNBAN(Permissions.COMMAND_MODERATION_UNBAN, Material.LIME_DYE),
        UNMUTE(Permissions.COMMAND_MODERATION_UNMUTE, Material.LIME_DYE);

        val temporary get() = this == TEMPBAN || this == TEMPMUTE
    }

    private fun text(player: Player, key: String, params: Map<String, Any> = emptyMap()) =
        LanguageHandler.getMessage("moderation.gui.$key", player, params)

    private fun access(player: Player, action: Action? = null): Boolean {
        val allowed = player.hasPermission(Permissions.COMMAND_MODERATION_MODGUI.permission.name) &&
                (action == null || player.hasPermission(action.permission.permission.name))
        if (!allowed) player.sendMessage(text(player, "denied"))
        return allowed
    }

    private fun button(
        player: Player, material: Material, key: String, lines: List<Component> = emptyList(),
        click: (Player) -> Unit = {}
    ): Item = Item.builder()
        .setItemProvider { GuiStyle.field(material, text(player, key), lines) }
        .addClickHandler { _, event -> if (access(event.player())) click(event.player()) }.build()

    private fun show(player: Player, key: String, gui: Gui) {
        Window.builder().setTitle(GuiStyle.title(text(player, key), Colors.MAIN)).setUpperGui(gui).open(player)
    }

    fun openGUI(player: Player) {
        if (!access(player)) return
        val items = knownPlayers().map { target ->
            Item.builder().setItemProvider {
                GuiStyle.field(
                    Material.PLAYER_HEAD, Component.text(label(target)), listOf(
                        text(player, if (target.isOnline) "online" else "offline"),
                        Component.text(target.uniqueId.toString())
                    )
                )
            }.addClickHandler { _, event -> details(event.player(), target) }.build()
        }.ifEmpty { listOf(button(player, Material.PAPER, "empty")) }
        val gui = PagedGui.itemsBuilder().setStructure(
            "# x x x x x x x #", "# x x x x x x x #", "# x x x x x x x #", "s # # < # > # r #"
        )
            .addIngredient('#', Filler.item()).addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('s', button(player, Material.COMPASS, "search") { player ->
                input(player, "search", "", { openGUI(it) }) { viewer, value -> openTarget(viewer, value) }
            })
            .addIngredient('r', button(player, Material.SUNFLOWER, "refresh") { openGUI(it) })
            .addIngredient('<', pageButton(player, false)).addIngredient('>', pageButton(player, true))
            .setContent(items).build()
        show(player, "players", gui)
    }

    private fun pageButton(player: Player, next: Boolean) = BoundItem.pagedBuilder()
        .setItemProvider { _, gui ->
            if (if (next) gui.page < gui.pageCount - 1 else gui.page > 0)
                GuiStyle.field(Material.ARROW, text(player, if (next) "next" else "previous"), emptyList())
            else ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ")
        }.addClickHandler { _, gui, event -> if (access(event.player())) gui.page += if (next) 1 else -1 }.build()

    fun openTarget(player: Player, value: String) {
        if (!access(player)) return
        val input = value.trim()
        val target = runCatching { Bukkit.getOfflinePlayer(UUID.fromString(input)) }.getOrNull()
            ?: Bukkit.getOfflinePlayerIfCached(input)
        if (target == null) {
            player.sendMessage(text(player, "unknown"))
            return
        }
        details(player, target)
    }

    private fun label(target: OfflinePlayer) = target.name ?: target.uniqueId.toString()

    private fun details(player: Player, target: OfflinePlayer) {
        if (!access(player)) return
        val muted = TargetUtils.isTargetMuted(target)
        val lines = listOf(
            Component.text(label(target)), Component.text(target.uniqueId.toString()),
            text(player, if (target.isOnline) "online" else "offline"),
            text(
                player, "state", mapOf(
                    "banned" to target.isBanned, "muted" to muted,
                    "warnings" to WarnSaveFile().getWarningCount(target.uniqueId)
                )
            )
        ) +
                MuteSaveFile().getMuteInfo(target.uniqueId)?.let {
                    listOf(
                        text(player, "muteReason", mapOf("reason" to it.reason)),
                        text(
                            player, "expires", mapOf(
                                "time" to (it.expires.toLongOrNull()?.let(::date)
                                    ?: LanguageHandler.getRawMessage("moderation.placeholder.duration.none", player))
                            )
                        )
                    )
                }.orEmpty()
        val actions = Action.entries.filter { player.hasPermission(it.permission.permission.name) }.map { action ->
            button(player, action.material, action.name.lowercase()) { viewer ->
                if (access(viewer, action)) {
                    val reason = LanguageHandler.getRawMessage("moderation.placeholder.reason.none", viewer)
                    editReason(viewer, target, action, reason)
                }
            }
        }
        val gui = PagedGui.itemsBuilder().setStructure("b # # # i # # # r", "# x x x x x x x #", "# # # < h > # # #")
            .addIngredient('#', Filler.item()).addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('b', button(player, Material.ARROW, "back") { openGUI(it) })
            .addIngredient('r', button(player, Material.SUNFLOWER, "refresh") { details(it, target) })
            .addIngredient('i', button(player, Material.PLAYER_HEAD, "details", lines))
            .addIngredient('h', button(player, Material.BOOK, "history") { history(it, target) })
            .addIngredient('<', pageButton(player, false)).addIngredient('>', pageButton(player, true))
            .setContent(actions).build()
        show(player, "details", gui)
    }

    private fun input(
        player: Player, key: String, initial: String, cancel: (Player) -> Unit,
        submit: (Player, String) -> Unit
    ) {
        if (!access(player)) return
        var pending = initial
        var submitted = false
        val gui = Gui.builder().setStructure("i b c").addIngredient('i', GuiStyle.inputPaper(initial))
            .addIngredient('b', button(player, Material.BARRIER, "cancel", click = cancel))
            .addIngredient('c', button(player, Material.LIME_DYE, "continue") {
                if (!submitted) {
                    submitted = true
                    submit(it, pending.trim())
                }
            }).build()
        AnvilWindow.builder().setTitle(GuiStyle.title(text(player, key), Colors.MAIN))
            .setUpperGui(gui).addRenameHandler { pending = it }.open(player)
    }

    private fun editReason(player: Player, target: OfflinePlayer, action: Action, initial: String) {
        if (!access(player, action)) return
        input(player, "reason", initial, { details(it, target) }) { viewer, value ->
            if (access(viewer, action)) {
                val reason = value.ifBlank { initial }
                if (action.temporary) editDuration(viewer, target, action, reason)
                else confirm(viewer, target, action, reason)
            }
        }
    }

    private fun editDuration(player: Player, target: OfflinePlayer, action: Action, reason: String) {
        input(player, "duration", "30 m", { editReason(it, target, action, reason) }) { viewer, value ->
            if (!access(viewer, action)) return@input
            val match = Regex("([1-9][0-9]{0,8})\\s+([smhdw])").matchEntire(value)
            if (match == null) {
                viewer.sendMessage(text(viewer, "invalidDuration"))
                editDuration(viewer, target, action, reason)
            } else confirm(viewer, target, action, reason, match.groupValues[1].toInt(), match.groupValues[2])
        }
    }

    private fun confirm(
        player: Player, target: OfflinePlayer, action: Action, reason: String,
        duration: Int = 0, unit: String = "m"
    ) {
        if (!access(player, action)) return
        val banned = target.isBanned
        val mute = MuteSaveFile().getMuteInfo(target.uniqueId)?.copy()
        var submitted = false
        val lines = listOf(
            Component.text(label(target)), text(player, action.name.lowercase()),
            Component.text(reason)
        ) + if (action.temporary) listOf(Component.text("$duration $unit")) else emptyList()
        val gui = Gui.builder().setStructure("# # # # # # # # #", "# # y # i # n # #", "# # # # # # # # #")
            .addIngredient('#', Filler.item()).addIngredient('i', button(player, action.material, "review", lines))
            .addIngredient('n', button(player, Material.BARRIER, "cancel") { details(it, target) })
            .addIngredient('y', button(player, Material.LIME_DYE, "confirm") { viewer ->
                if (!submitted && access(viewer, action)) {
                    submitted = true
                    viewer.closeInventory()
                    if (banned != target.isBanned || mute != MuteSaveFile().getMuteInfo(target.uniqueId)) {
                        viewer.sendMessage(text(viewer, "stale"))
                        details(viewer, target)
                    } else {
                        val profiles = listOf(Bukkit.createProfile(target.uniqueId, target.name))
                        when (action) {
                            Action.BAN -> BanLogic().triggerBan(profiles, reason, viewer)
                            Action.TEMPBAN -> TempBanLogic().triggerTempBan(profiles, reason, duration, unit, viewer)
                            Action.KICK -> KickLogic().triggerKick(profiles, reason, viewer)
                            Action.MUTE -> MuteLogic().triggerMute(profiles, reason, viewer)
                            Action.TEMPMUTE -> TempMuteLogic().triggerTempMute(profiles, reason, duration, unit, viewer)
                            Action.WARN -> WarnLogic().triggerWarn(profiles, reason, viewer)
                            Action.UNBAN -> UnbanLogic().triggerUnban(profiles, reason, viewer)
                            Action.UNMUTE -> UnmuteLogic().triggerUnmute(profiles, reason, viewer)
                        }
                        if (viewer.isOnline) details(viewer, target)
                    }
                }
            }).build()
        show(player, "review", gui)
    }

    private fun history(player: Player, target: OfflinePlayer) {
        if (!access(player)) return
        val items = WarnSaveFile().getWarnings(target.uniqueId).toList().map { warning ->
            button(
                player, Material.PAPER, "warning", listOf(
                    Component.text(warning.reason),
                    Component.text(warning.id), text(
                        player, "issued", mapOf(
                            "issuer" to
                                    (runCatching { Bukkit.getOfflinePlayer(UUID.fromString(warning.modAction.uuid)).name }.getOrNull()
                                        ?: warning.modAction.uuid),
                            "time" to (warning.modAction.timestamp.toLongOrNull()?.let(::date)
                                ?: warning.modAction.timestamp)
                        )
                    )
                )
            )
        }.ifEmpty { listOf(button(player, Material.PAPER, "empty")) }
        val gui = PagedGui.itemsBuilder().setStructure("# x x x x x x x #", "# x x x x x x x #", "b # # < # > # # #")
            .addIngredient('#', Filler.item()).addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('b', button(player, Material.ARROW, "back") { details(it, target) })
            .addIngredient('<', pageButton(player, false)).addIngredient('>', pageButton(player, true))
            .setContent(items).build()
        show(player, "history", gui)
    }

    private fun date(timestamp: Long): String = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(timestamp))
}
