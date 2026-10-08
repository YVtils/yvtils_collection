package yv.tils.discord.gui

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.AnvilWindow
import yv.tils.configv2.language.LanguageHandler
import yv.tils.discord.commands.DiscordAccountsCommand
import yv.tils.discord.data.Permissions
import yv.tils.discord.language.AccountText
import yv.tils.discord.logic.whitelist.*
import yv.tils.gui.core.InvUIBootstrap
import yv.tils.gui.utils.Filler
import yv.tils.gui.utils.GuiStyle
import yv.tils.gui.utils.HeadUtils
import yv.tils.gui.utils.Heads
import yv.tils.utils.colors.Colors
import yv.tils.utils.message.MessageUtils
import yv.tils.utils.coroutine.CoroutineHandler

object AccountsGui {
    private fun text(key: String, viewer: Player, params: Map<String, Any> = emptyMap()) =
        LanguageHandler.getMessage("discord.accounts.$key", viewer, params)

    private fun access(player: Player) =
        DiscordAccountsCommand.allowed(player, Permissions.ACCOUNTS_GUI) &&
                DiscordAccountsCommand.allowed(player, Permissions.ACCOUNTS_READ)

    private fun current(player: Player, entry: WhitelistEntry): Boolean {
        if (!access(player)) return false
        if (WhitelistLogic.getEntryByDiscordID(entry.discordUserID) == entry) return true
        AccountText.send(player, "stale")
        return false
    }

    private fun styled(
        stack: ItemStack,
        title: Component,
        lore: List<Component>,
        color: Colors = Colors.SECONDARY
    ): ItemWrapper {
        stack.editMeta {
            it.displayName(GuiStyle.title(title, color))
            it.lore(lore.map(GuiStyle::lore))
        }
        return ItemWrapper(stack)
    }

    private fun control(head: Heads, title: String, lore: String? = null, click: (Player) -> Unit): Item =
        Item.builder().setItemProvider { viewer ->
            val color = when (head) {
                Heads.X_CHARACTER -> Colors.RED
                Heads.CHECK_MARK -> Colors.GREEN
                else -> Colors.SECONDARY
            }
            styled(
                HeadUtils.createCustomHead(head, ""), text(title, viewer),
                lore?.let { MessageUtils.handleLore(text(it, viewer)) }.orEmpty(), color
            )
        }.addClickHandler { _, event -> click(event.player()) }.build()

    private fun account(
        entry: WhitelistEntry,
        hint: String? = null,
        extra: Map<String, Any> = emptyMap(),
        click: (Player) -> Unit = {}
    ): Item {
        val item = Item.builder().setItemProvider { viewer ->
            val lines = MessageUtils.handleLore(text("entry", viewer, DiscordAccountsCommand.params(entry, viewer))) +
                    if (hint == null) emptyList() else listOf(Component.empty()) + MessageUtils.handleLore(
                        text(
                            hint,
                            viewer,
                            extra
                        )
                    )
            styled(AccountHeads.head(entry), Component.text(entry.minecraftName), lines)
        }.addClickHandler { _, event -> click(event.player()) }.build()
        AccountHeads.load(entry, item)
        yv.tils.discord.utils.DiscordAccountNames.retrieve(entry.discordUserID).whenComplete { _, _ ->
            if (yv.tils.utils.modules.Core.instance.isEnabled) {
                org.bukkit.Bukkit.getScheduler().runTask(yv.tils.utils.modules.Core.instance, Runnable { item.notifyWindows() })
            }
        }
        return item
    }

    private fun show(player: Player, title: String, gui: Gui) = Window.builder()
        .setTitle(GuiStyle.title(text(title, player), Colors.MAIN)).setUpperGui(gui).open(player)

    fun open(player: Player) {
        if (!access(player)) return
        InvUIBootstrap.ensure()
        val entries = WhitelistLogic.getAllEntries()
        val items = entries.map { entry ->
            account(entry, "guiInspectHint") { viewer -> if (current(viewer, entry)) details(viewer, entry) }
        }.ifEmpty { listOf(control(Heads.I_CHARACTER, "empty") {}) }

        fun navigation(forward: Boolean) = BoundItem.pagedBuilder()
            .setItemProvider { viewer, gui ->
                val available = if (forward) gui.page < gui.pageCount - 1 else gui.page > 0
                if (!available) Filler.pane() else styled(
                    HeadUtils.createCustomHead(if (forward) Heads.NEXT_PAGE else Heads.PREVIOUS_PAGE, ""),
                    text(if (forward) "next" else "previous", viewer), emptyList()
                )
            }.addClickHandler { _, gui, click ->
                if (access(click.player())) {
                    val page = gui.page + if (forward) 1 else -1
                    if (page >= 0 && page < gui.pageCount) gui.page = page
                }
            }

        val rows = ((minOf(items.size, 28) + 6) / 7).coerceIn(1, 4)
        val structure = listOf("b # # # i # # # r") + List(rows) { "# x x x x x x x #" } + "# # # < w > # # #"
        val gui = PagedGui.itemsBuilder().setStructure(*structure.toTypedArray())
            .addIngredient('#', Filler.item()).addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('<', navigation(false)).addIngredient('>', navigation(true))
            .addIngredient('b', control(Heads.X_CHARACTER, "guiClose") { if (access(it)) it.closeInventory() })
            .addIngredient('i', control(Heads.DISCORD_LOGO, "title", "guiListHint") {})
            .addIngredient('r', control(Heads.TOOLBOX, "guiRefresh", "guiRefreshHint") { open(it) })
            .addIngredient('w', BoundItem.pagedBuilder().setItemProvider { viewer, paged ->
                if (paged.pageCount <= 1) Filler.pane() else styled(
                    HeadUtils.createCustomHead(Heads.I_CHARACTER, ""),
                    text("guiPage", viewer, mapOf("page" to paged.page + 1, "pages" to paged.pageCount)), emptyList()
                )
            }).setContent(items).build()
        show(player, "title", gui)
    }

    fun details(player: Player, entry: WhitelistEntry, replacement: String? = null) {
        if (!current(player, entry)) return
        InvUIBootstrap.ensure()
        val gui = Gui.builder().setStructure("b # # # # # # # #", "# i # r # p # # #", "# # # # # # # # #")
            .addIngredient('#', Filler.item())
            .addIngredient('i', account(entry))
            .addIngredient('b', control(Heads.PREVIOUS_PAGE, "guiBack") { open(it) })
            .addIngredient('r', control(Heads.X_CHARACTER, "remove", "guiRemoveHint") {
                if (current(it, entry) && DiscordAccountsCommand.allowed(it, Permissions.ACCOUNTS_REMOVE)) confirm(
                    it,
                    entry,
                    null
                )
            })
            .addIngredient(
                'p',
                control(Heads.TOOLBOX, "replace", "guiReplaceInputHint") {
                    if (current(it, entry) && DiscordAccountsCommand.allowed(it, Permissions.ACCOUNTS_REPLACE)) {
                        replacementInput(it, entry, replacement ?: entry.minecraftName)
                    }
                }).build()
        show(player, "details", gui)
    }

    private fun replacementInput(player: Player, entry: WhitelistEntry, initial: String) {
        if (!current(player, entry) || !DiscordAccountsCommand.allowed(player, Permissions.ACCOUNTS_REPLACE)) return
        var pending = initial
        var submitted = false
        val gui = Gui.builder().setStructure("i b c")
            .addIngredient('i', GuiStyle.inputPaper(initial))
            .addIngredient('b', control(Heads.X_CHARACTER, "cancel", "guiCancelHint") { details(it, entry) })
            .addIngredient('c', control(Heads.CHECK_MARK, "guiReviewReplacement", "guiReplaceInputHint") { viewer ->
                if (!submitted && current(viewer, entry) && DiscordAccountsCommand.allowed(
                        viewer,
                        Permissions.ACCOUNTS_REPLACE
                    )
                ) {
                    submitted = true
                    val input = pending.trim()
                    viewer.closeInventory()
                    AccountText.send(viewer, "guiResolving")
                    CoroutineHandler.launchTask(task = {
                        try {
                            val candidate = AccountService.prepareReplacement(input, entry)
                            AccountService.server {
                                if (viewer.isOnline && current(viewer, entry) && DiscordAccountsCommand.allowed(
                                        viewer,
                                        Permissions.ACCOUNTS_REPLACE
                                    )
                                ) {
                                    confirm(viewer, entry, candidate.minecraftUUID, candidate.minecraftName)
                                }
                            }
                        } catch (error: Exception) {
                            val reason = AccountText.reason(error)
                            AccountService.server {
                                if (viewer.isOnline) {
                                    AccountText.send(viewer, reason)
                                    if (current(viewer, entry)) replacementInput(viewer, entry, input)
                                }
                            }
                        }
                    }, isOnce = true)
                }
            }).build()
        AnvilWindow.builder().setTitle(GuiStyle.title(text("guiReplacementInputTitle", player), Colors.MAIN))
            .setUpperGui(gui).addRenameHandler { pending = it }.open(player)
    }

    private fun confirm(
        player: Player,
        entry: WhitelistEntry,
        replacement: String?,
        replacementName: String? = replacement
    ) {
        val permission = if (replacement == null) Permissions.ACCOUNTS_REMOVE else Permissions.ACCOUNTS_REPLACE
        val gui = Gui.builder().setStructure("# # # # # # # # #", "# # y # i # n # #", "# # # # # # # # #")
            .addIngredient('#', Filler.item())
            .addIngredient(
                'i', account(
                    entry, if (replacement == null) "guiRemoveWarning" else "guiReplaceWarning",
                    mapOf("replacement" to replacementName.orEmpty())
                )
            )
            .addIngredient(
                'n',
                control(Heads.X_CHARACTER, "cancel", "guiCancelHint") { details(it, entry, replacementName) })
            .addIngredient('y', control(Heads.CHECK_MARK, "confirm", "guiConfirmHint") {
                if (current(it, entry) && DiscordAccountsCommand.allowed(it, permission)) {
                    it.closeInventory()
                    DiscordAccountsCommand.action(it, permission) { actor ->
                        AccountService.server { if (!access(it)) throw AccountService.Failure("permission") }
                        if (replacement == null) AccountService.remove(entry.discordUserID, null, actor, entry)
                        else AccountService.register(replacement, entry.discordUserID, null, actor, entry)
                    }
                }
            }).build()
        show(player, "confirm", gui)
    }
}
