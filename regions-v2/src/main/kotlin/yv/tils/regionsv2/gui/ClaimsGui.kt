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
package yv.tils.regionsv2.gui

import com.sk89q.worldguard.protection.flags.BooleanFlag
import com.sk89q.worldguard.protection.flags.Flag
import com.sk89q.worldguard.protection.flags.StateFlag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.Window
import yv.tils.gui.core.InvUIBootstrap
import yv.tils.gui.logic.DataClassConfigGui
import yv.tils.gui.logic.ToggleControl
import yv.tils.gui.utils.Filler
import yv.tils.gui.utils.HeadUtils
import yv.tils.gui.utils.Heads
import yv.tils.gui.utils.GuiStyle
import yv.tils.regionsv2.configs.ConfigFile
import yv.tils.regionsv2.configs.FlagPolicy
import yv.tils.regionsv2.data.Permissions
import yv.tils.regionsv2.language.LangStrings
import yv.tils.regionsv2.language.LangStrings.*
import yv.tils.regionsv2.language.RegionMessage
import yv.tils.regionsv2.language.RegionText
import yv.tils.regionsv2.language.message
import yv.tils.regionsv2.logic.*
import yv.tils.utils.colors.Colors
import yv.tils.utils.message.MessageUtils
import java.util.*

object ClaimsGui {
    fun action(player: Player, action: () -> Unit) = RegionText.action(player, action)
    private fun plain(text: String) = Component.text(text).decoration(TextDecoration.ITALIC, false)
    private fun button(head: Heads, title: RegionMessage, vararg lore: RegionMessage, click: () -> Unit): Item =
        button({ HeadUtils.createCustomHead(head, "") }, title, *lore, click = click)

    private fun button(material: Material, title: RegionMessage, vararg lore: RegionMessage, click: () -> Unit): Item =
        button({ ItemStack(material) }, title, *lore, click = click)

    private fun button(head: RegionHeads, title: RegionMessage, vararg lore: RegionMessage, click: () -> Unit): Item =
        button({ HeadUtils.createCustomHead(head.texture, "") }, title, *lore, click = click)

    private fun button(
        stack: () -> ItemStack,
        title: RegionMessage,
        vararg lore: RegionMessage,
        click: () -> Unit
    ): Item =
        literalButton(
            stack,
            { title.component(it) },
            { viewer -> lore.flatMap { MessageUtils.handleLore(it.component(viewer)) } },
            click
        )

    /** Player names, claim names and WorldGuard identifiers are literal values, not language keys. */
    private fun literalButton(
        head: Heads,
        title: (Player) -> Component,
        lore: (Player) -> List<Component> = { emptyList() },
        click: () -> Unit
    ): Item = literalButton({ HeadUtils.createCustomHead(head, "") }, title, lore, click)

    private fun literalButton(
        material: Material,
        title: (Player) -> Component,
        lore: (Player) -> List<Component> = { emptyList() },
        click: () -> Unit
    ): Item = literalButton({ ItemStack(material) }, title, lore, click)

    private fun literalButton(
        head: RegionHeads,
        title: (Player) -> Component,
        lore: (Player) -> List<Component> = { emptyList() },
        click: () -> Unit
    ): Item = literalButton({ HeadUtils.createCustomHead(head.texture, "") }, title, lore, click)

    private fun literalButton(
        stack: () -> ItemStack,
        title: (Player) -> Component,
        lore: (Player) -> List<Component> = { emptyList() },
        click: () -> Unit
    ): Item =
        Item.builder().setItemProvider { viewer ->
            val item = stack()
            item.editMeta { meta ->
                val color = when {
                    item.type == Material.BARRIER || item.type == Material.STRUCTURE_VOID -> Colors.RED
                    stackIsHead(item, Heads.X_CHARACTER) -> Colors.RED
                    stackIsHead(item, Heads.CHECK_MARK) -> Colors.GREEN
                    else -> Colors.SECONDARY
                }
                meta.displayName(GuiStyle.title(title(viewer), color))
                meta.lore(lore(viewer).map(GuiStyle::lore))
            }
            ItemWrapper(item)
        }.addClickHandler { _, _ -> click() }.build()

    private fun stackIsHead(stack: ItemStack, head: Heads): Boolean =
        (stack.itemMeta as? SkullMeta)?.playerProfile?.properties?.any {
            it.name == "textures" && it.value == head.texture
        } == true

    private fun pageButton(head: Heads, key: LangStrings, move: (PagedGui<*>) -> Unit): BoundItem.Builder<PagedGui<*>> =
        BoundItem.pagedBuilder().setItemProvider { viewer, gui ->
            val available = if (head == Heads.PREVIOUS_PAGE) gui.page > 0 else gui.page < gui.pageCount - 1
            if (available) ItemWrapper(HeadUtils.createCustomHead(head, "").apply {
                editMeta { it.displayName(GuiStyle.title(key.message().component(viewer))) }
            }) else Filler.pane()
        }.addClickHandler { _, gui, _ ->
            val available = if (head == Heads.PREVIOUS_PAGE) gui.page > 0 else gui.page < gui.pageCount - 1
            if (available) move(gui)
        }

    private fun menu(
        player: Player, title: RegionMessage, items: List<Item>, back: (() -> Unit)? = null,
        rows: Int? = null, tabs: Map<Char, Item> = emptyMap(), filter: Item? = null, side: Item? = null
    ) {
        InvUIBootstrap.ensure()
        val contentRows = rows ?: ((minOf(items.size, 28) + 6) / 7).coerceIn(1, 4)
        val top = if (tabs.isEmpty()) "b # # # # # # # q" else "b # i # m # f # s"
        val structure = listOf(top) + List(contentRows) { index ->
            if (index == 1 && side != null) "# x x x x x x x z" else "# x x x x x x x #"
        } + "# # # < w > # # #"
        val builder = PagedGui.itemsBuilder().setStructure(*structure.toTypedArray())
            .addIngredient('#', Filler.item())
            .addIngredient('q', filter ?: Filler.item())
            .addIngredient('z', side ?: Filler.item())
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('w', BoundItem.pagedBuilder().setItemProvider { viewer, paged ->
                if (paged.pageCount <= 1) return@setItemProvider Filler.pane()
                val stack = HeadUtils.createCustomHead(Heads.I_CHARACTER, "")
                stack.editMeta {
                    it.displayName(
                        GuiStyle.title(
                            PAGE.message(
                                "page" to paged.page + 1,
                                "pages" to maxOf(1, paged.pageCount)
                            ).component(viewer)
                        )
                    )
                }
                ItemWrapper(stack)
            })
            .addIngredient('<', pageButton(Heads.PREVIOUS_PAGE, PREVIOUS) { it.page-- })
            .addIngredient('>', pageButton(Heads.NEXT_PAGE, NEXT) { it.page++ })
            .addIngredient('b', button(Heads.PREVIOUS_PAGE, (if (back == null) CLOSE else BACK).message()) {
                if (back == null) player.closeInventory() else back()
            }).setContent(items)
        tabs.forEach { (key, item) -> builder.addIngredient(key, item) }
        val gui = builder.build()
        show(player, title, gui)
    }

    private fun show(player: Player, title: RegionMessage, gui: Gui) {
        Window.builder().setTitle(MessageUtils.convert("<${Colors.MAIN.color}>").append(title.component(player)))
            .setUpperGui(gui).open(player)
    }

    private fun input(
        player: Player,
        title: RegionMessage,
        back: () -> Unit,
        current: String = "",
        submit: (String) -> Unit
    ) {
        var pending = current
        val gui = Gui.builder().setStructure("i b c")
            .addIngredient('i', GuiStyle.inputPaper(current))
            .addIngredient('b', button(Heads.X_CHARACTER, CANCEL.message(), click = back))
            .addIngredient(
                'c',
                button(Heads.CHECK_MARK, CONFIRM.message()) { action(player) { submit(pending.trim()) } }).build()
        AnvilWindow.builder().setTitle(title.component(player)).setUpperGui(gui).addRenameHandler { pending = it }
            .open(player)
    }

    private fun roleMessage(role: ClaimRole?) = when (role) {
        ClaimRole.OWNER -> ROLE_OWNER.message()
        ClaimRole.MEMBER -> ROLE_MEMBER.message()
        ClaimRole.VISITOR -> ROLE_VISITOR.message()
        null -> GLOBAL.message()
    }

    private fun roleName(player: Player, role: ClaimRole?) = MessageUtils.strip(roleMessage(role).component(player))
    private fun valueName(player: Player, value: Any?): String = ClaimInformation.valueName(player, value)

    private fun toggle(
        player: Player, title: (Player) -> Component, value: () -> Any?,
        change: () -> Unit, reset: (() -> Unit)? = null, inherit: Boolean = false
    ): Item = ToggleControl.item(
        provider = { viewer ->
            val current = value()
            val stack = HeadUtils.createCustomHead(
                if (current == false || current == StateFlag.State.DENY) Heads.X_CHARACTER else Heads.CHECK_MARK, ""
            )
            stack.editMeta { meta ->
                meta.displayName(GuiStyle.title(title(viewer)))
                meta.lore(
                    (listOf(
                        CURRENT_VALUE.message(
                            "value" to if (inherit && current == null)
                                MessageUtils.strip(SUBZONE_INHERIT.message().component(viewer)) else valueName(
                                viewer,
                                current
                            )
                        ).component(viewer),
                        ToggleControl.controls(viewer)
                    ) + if (reset != null) listOf(RESET_CONTROL.message().component(viewer)) else emptyList())
                        .map(GuiStyle::lore)
                )
            }
            ItemWrapper(stack)
        },
        toggle = { action(player, change) },
        reset = reset?.let { { action(player, it) } }
    )

    fun open(player: Player): Unit = action(player) {
        check(player.hasPermission(Permissions.MANAGE.permission.name)) { MANAGE_DENIED.key }
        check(ConfigFile.state.enabled) { DISABLED.key }
        InvUIBootstrap.ensure()
        val structure = listOf("# # # # # # # # #", "# r # c # s # l #", "# # # # # # # # #")
        val gui = Gui.builder().setStructure(*structure.toTypedArray())
            .addIngredient('#', Filler.item())
            .addIngredient(
                'r',
                if (Permissions.LIST.allowed(player)) button(
                    Material.FILLED_MAP,
                    CLAIMS.message()
                ) { myRegions(player) } else Filler.item())
            .addIngredient(
                'c',
                if (Permissions.CLAIM.allowed(player)) button(
                    Heads.PLUS_CHARACTER,
                    CREATE.message(),
                    CREATE_LORE.message()
                ) { creation(player) } else Filler.item())
            .addIngredient(
                's', if (Permissions.ADMIN_CONFIG.allowed(player) || Permissions.ADMIN_POLICIES.allowed(player))
                    button(Heads.TOOLBOX, SERVER.message()) { admin(player) } else Filler.item())
            .addIngredient('l', if (Permissions.INFO.allowed(player)) button(Material.SPYGLASS, INSPECT.message()) {
                action(player) {
                    player.closeInventory()
                    val claim = ClaimService.at(player.location)
                    if (claim == null) RegionText.send(player, NO_CLAIM_HERE.message())
                    else ClaimInformation.sendSummary(player, claim)
                }
            } else Filler.item()).build()
        show(player, MAIN_MENU.message(), gui)
    }

    private fun myRegions(player: Player, others: Boolean = false): Unit = action(player) {
        Permissions.LIST.require(player)
        check(player.hasPermission(Permissions.MANAGE.permission.name)) { MANAGE_DENIED.key }
        check(ConfigFile.state.enabled) { DISABLED.key }
        if (others) check(ClaimService.admin(player)) { ADMIN_REQUIRED.key }
        val items = ClaimService.all()
            .filter { (ClaimService.role(it, player.uniqueId) == ClaimRole.VISITOR) == others }
            .sortedWith(compareBy<Claim> { it.world != player.world }.thenBy { it.name.lowercase() }.thenBy { it.uuid })
            .map { claim ->
                literalButton(Material.FILLED_MAP, { plain(claim.name) }, { viewer ->
                    MessageUtils.handleLore(
                        CLAIM_LORE.message(
                            "world" to claim.world.name,
                            "role" to roleName(viewer, ClaimService.role(claim, viewer.uniqueId)),
                            "x" to claim.region.maximumPoint.x() - claim.region.minimumPoint.x() + 1,
                            "z" to claim.region.maximumPoint.z() - claim.region.minimumPoint.z() + 1
                        ).component(viewer)
                    )
                }) { details(player, claim) }
            }.toMutableList()
        if (items.isEmpty()) {
            items += if (others) button(Material.FILLED_MAP, NO_OTHER_CLAIMS.message()) {} else
                button(Heads.PLUS_CHARACTER, CREATE.message(), CREATE_LORE.message()) { creation(player) }
        }
        val filter = if (ClaimService.admin(player)) button(
            Material.FILLED_MAP,
            (if (others) CLAIMS else OTHER_CLAIMS).message(),
            (if (others) PERSONAL_CLAIMS_LORE else OTHER_CLAIMS_LORE).message()
        ) {
            myRegions(player, !others)
        } else null
        menu(
            player, (if (others) OTHER_CLAIMS else CLAIMS).message(), items,
            { if (others) myRegions(player) else open(player) }, rows = 3, filter = filter
        )
    }

    private fun creation(player: Player): Unit = action(player) {
        Permissions.CLAIM.require(player)
        InvUIBootstrap.ensure()
        val name = ClaimSelection.name(player)
        val gui = Gui.builder().setStructure(
            "b # # # # # # # #",
            "# 1 2 # n # # p #",
            "# # # c # x # # #"
        ).addIngredient('#', Filler.item())
            .addIngredient('b', button(Heads.PREVIOUS_PAGE, BACK.message()) { open(player) })
            .addIngredient('1', cornerButton(player, true))
            .addIngredient('2', cornerButton(player, false))
            .addIngredient(
                'n', button(
                    Material.NAME_TAG, NAME_INPUT.message(),
                    CURRENT_VALUE.message("value" to name.ifEmpty {
                        MessageUtils.strip(
                            UNSELECTED.message().component(player)
                        )
                    })
                ) {
                    input(player, NAME_INPUT.message(), { creation(player) }, current = name) { updated ->
                        ClaimSelection.name(player, updated)
                        creation(player)
                    }
                })
            .addIngredient('p', button(Material.SPYGLASS, PREVIEW.message()) {
                action(player) { ClaimSelection.preview(player); player.closeInventory() }
            })
            .addIngredient('c', button(Heads.CHECK_MARK, CONFIRM.message()) {
                action(player) {
                    check(name.isNotBlank()) { INVALID_NAME.key }
                    val bounds = ClaimSelection.bounds(player)
                    menu(
                        player, CREATE_CONFIRM.message("region" to name), listOf(
                            button(
                                Heads.CHECK_MARK, CONFIRM.message(),
                                BOUNDS.message(
                                    "world" to bounds.world.name,
                                    "x" to bounds.sides[0],
                                    "z" to bounds.sides[2],
                                    "height" to bounds.sides[1]
                                ), COST_LORE.message("cost" to ClaimCurrency.quote(player, bounds))
                            ) {
                                action(player) {
                                    val claim = ClaimService.create(player, name, player.uniqueId, bounds)
                                    ClaimSelection.clear(player.uniqueId)
                                    details(player, claim)
                                }
                            }), { creation(player) })
                }
            })
            .addIngredient('x', button(Heads.X_CHARACTER, CANCEL.message()) {
                ClaimSelection.clear(player.uniqueId)
                open(player)
            }).build()
        show(player, CREATE.message(), gui)
    }

    private fun cornerButton(player: Player, first: Boolean): Item = literalButton(
        if (first) Heads.NUMBER_1 else Heads.NUMBER_2,
        { CORNER.message("position" to if (first) 1 else 2).component(it) }, { viewer ->
            val position = ClaimSelection.description(player, first) ?: MessageUtils.strip(
                UNSELECTED.message().component(viewer)
            )
            MessageUtils.handleLore(CORNER_LORE.message("position" to position).component(viewer))
        }) { action(player) { ClaimSelection.select(player, first); creation(player) } }

    fun details(player: Player, claim: Claim): Unit = action(player) {
        Permissions.INFO.require(player)
        check(ConfigFile.state.enabled && player.hasPermission(Permissions.MANAGE.permission.name)) { MANAGE_DENIED.key }
        check(ClaimService.manager(claim.world).getRegion(claim.region.id) === claim.region) { STALE_CLAIM.key }
        val items = listOf(
            button(Heads.I_CHARACTER, BASIC_INFO.message(), *ClaimInformation.basic(claim).toTypedArray()) {},
            button(
                Material.PAPER, LOCATION_INFO.message(), *ClaimInformation.location(claim).toTypedArray(),
                HIGHLIGHT_LORE.message()
            ) {
                action(player) {
                    ClaimSelection.preview(
                        player,
                        ClaimBounds(claim.world, claim.region.minimumPoint, claim.region.maximumPoint)
                    )
                    player.closeInventory()
                    RegionText.send(player, PREVIEW_STARTED.message())
                }
            })
        managementMenu(player, claim, INFORMATION, items)
    }

    private fun managementMenu(player: Player, claim: Claim, selected: LangStrings, items: List<Item>) {
        check(ConfigFile.state.enabled && player.hasPermission(Permissions.MANAGE.permission.name)) { MANAGE_DENIED.key }
        check(ClaimService.manager(claim.world).getRegion(claim.region.id) === claim.region) { STALE_CLAIM.key }
        fun tab(stack: () -> ItemStack, label: LangStrings, click: () -> Unit) = button(
            stack,
            label.message(),
            *(if (selected == label) arrayOf(ACTIVE_TAB.message()) else emptyArray()),
            click = click
        )
        menu(
            player,
            CLAIM.message("region" to claim.name),
            items,
            {
                myRegions(
                    player,
                    ClaimService.admin(player) && ClaimService.role(claim, player.uniqueId) == ClaimRole.VISITOR
                )
            },
            rows = 3,
            side = if (Permissions.SUBZONES_VIEW.allowed(player)) button(
                Material.REDSTONE,
                SUBZONES.message()
            ) { subzones(player, claim) } else null,
            tabs = mapOf(
                'i' to tab({ HeadUtils.createCustomHead(Heads.I_CHARACTER, "") }, INFORMATION) {
                    details(
                        player,
                        claim
                    )
                },
                'm' to if (Permissions.MEMBERS_VIEW.allowed(player)) tab({
                    HeadUtils.createCustomHead(
                        RegionHeads.MEMBERS.texture,
                        ""
                    )
                }, PEOPLE) {
                    people(
                        player,
                        claim
                    )
                } else Filler.item(),
                'f' to if (Permissions.FLAGS_VIEW.allowed(player)) tab(
                    { HeadUtils.createCustomHead(RegionHeads.FLAGS.texture, "") },
                    FLAGS_TAB
                ) { flagScopes(player, claim) } else Filler.item(),
                's' to tab({ HeadUtils.createCustomHead(Heads.TOOLBOX, "") }, SETTINGS_TAB) { settings(player, claim) }
            ))
    }

    private fun flagScopes(player: Player, claim: Claim): Unit = action(player) {
        Permissions.FLAGS_VIEW.require(player)
        val editable = Permissions.FLAGS_EDIT.allowed(player) &&
                (ClaimService.admin(player) || ClaimService.role(claim, player.uniqueId) == ClaimRole.OWNER)
        managementMenu(player, claim, FLAGS_TAB, (listOf(null) + ClaimRole.entries).map { role ->
            literalButton(
                RegionHeads.role(role),
                { FLAGS.message("role" to roleName(it, role)).component(it) },
                { viewer ->
                    val lines = ClaimInformation.flagMessages(viewer, claim, role)
                    val summary = if (lines.isEmpty()) listOf(NO_FLAGS.message().component(viewer))
                    else lines.map { it.component(viewer) }
                    summary + if (editable) listOf(EDIT_FLAGS_LORE.message().component(viewer)) else emptyList()
                }) {
                if (editable) flags(player, claim, role)
            }
        })
    }

    private fun memberHead(player: Player, claim: Claim, uuid: UUID): Item =
        Item.builder().setItemProvider { viewer ->
            val offline = Bukkit.getOfflinePlayer(uuid)
            val stack = ItemStack(Material.PLAYER_HEAD)
            stack.editMeta { meta ->
                (meta as SkullMeta).owningPlayer = offline
                meta.displayName(GuiStyle.title(plain(offline.name ?: uuid.toString())))
                meta.lore(
                    MessageUtils.handleLore(
                        ROLE_LORE.message("role" to roleName(viewer, ClaimService.role(claim, uuid))).component(viewer)
                    ).map(GuiStyle::lore)
                )
            }
            ItemWrapper(stack)
        }.addClickHandler { _, _ ->
            action(player) {
                ClaimService.requireOwner(player, claim)
                chooseRole(player, claim, uuid)
            }
        }.build()

    private fun settings(player: Player, claim: Claim): Unit = action(player) {
        ClaimService.requireOwner(player, claim)
        val items = mutableListOf<Item>()
        if (Permissions.RENAME.allowed(player)) items += button(Material.NAME_TAG, RENAME.message()) {
            input(player, NAME_INPUT.message(), { settings(player, claim) }, current = claim.name) { name ->
                ClaimService.rename(
                    player,
                    claim,
                    name
                ); settings(player, claim)
            }
        }
        if (Permissions.RESIZE.allowed(player)) items += button(
            Material.CARTOGRAPHY_TABLE,
            RESIZE.message(),
            RESIZE_LORE.message()
        ) {
            action(player) {
                val bounds = ClaimSelection.bounds(player)
                menu(
                    player, RESIZE.message(), listOf(
                        button(
                            Heads.CHECK_MARK, CONFIRM.message(),
                            COST_LORE.message(
                                "cost" to ClaimCurrency.resizeQuote(player, claim, bounds)
                            )
                        ) {
                            action(player) {
                                details(
                                    player,
                                    ClaimService.resize(player, claim, bounds)
                                )
                            }
                        }), { settings(player, claim) })
            }
        }
        if (Permissions.MERGE.allowed(player)) items += button(Material.WRITABLE_BOOK, MERGE.message()) {
            input(player, MERGE_INPUT.message(), { settings(player, claim) }) { name ->
                val other = ClaimService.find(claim.world, name)
                val merged = ClaimGeometry.mergeBounds(
                    claim.region.minimumPoint, claim.region.maximumPoint,
                    other.region.minimumPoint, other.region.maximumPoint
                )
                val bounds = ClaimBounds(claim.world, merged.first, merged.second)
                menu(
                    player,
                    MERGE.message(),
                    listOf(
                        button(
                            Heads.CHECK_MARK, CONFIRM.message(), MERGE_LORE.message("region" to other.name),
                            COST_LORE.message(
                                "cost" to ClaimCurrency.mergeQuote(player, claim, other, bounds)
                            )
                        ) {
                            action(player) { details(player, ClaimService.merge(player, claim, other)) }
                        }),
                    { settings(player, claim) })
            }
        }
        if (Permissions.FLAGS_RESET.allowed(player)) items += button(
            Material.STRUCTURE_VOID,
            RESTORE_DEFAULTS.message(),
            RESTORE_LORE.message()
        ) {
            menu(
                player, RESTORE_DEFAULTS.message(), listOf(
                    button(Heads.CHECK_MARK, CONFIRM.message(), RESTORE_LORE.message()) {
                        action(player) { ClaimFlags.restoreDefaults(player, claim); settings(player, claim) }
                    }), { settings(player, claim) })
        }
        if (Permissions.ADMIN_MESSAGES.allowed(player)) items += button(Heads.ENVELOPE, MESSAGES.message()) {
            messages(
                player,
                claim
            )
        }
        if (Permissions.DELETE.allowed(player)) items += button(
            Material.BARRIER,
            DELETE.message(),
            DELETE_LORE.message()
        ) {
            menu(
                player, DELETE_CONFIRM.message("region" to claim.name), listOf(
                    button(Heads.X_CHARACTER, CONFIRM.message(), DELETE_LORE.message()) {
                        action(player) {
                            ClaimService.delete(player, claim)
                            RegionText.send(player, COMPLETED.message())
                            myRegions(
                                player,
                                ClaimService.admin(player) && ClaimService.role(
                                    claim,
                                    player.uniqueId
                                ) == ClaimRole.VISITOR
                            )
                        }
                    }), { settings(player, claim) })
        }
        managementMenu(player, claim, SETTINGS_TAB, items)
    }

    private fun messages(player: Player, claim: Claim): Unit = action(player) {
        Permissions.ADMIN_MESSAGES.require(player)
        ClaimService.requireOwner(player, claim)
        val items = mutableListOf<Item>()
        for (welcome in listOf(true, false)) items += button(
            Heads.ENVELOPE,
            (if (welcome) WELCOME else GOODBYE).message(),
            MESSAGE_LORE.message()
        ) {
            input(
                player, (if (welcome) WELCOME else GOODBYE).message(), { messages(player, claim) },
                current = if (welcome) claim.metadata.welcome else claim.metadata.goodbye
            ) { text ->
                ClaimService.messages(player, claim, welcome, if (text == "unset") "" else text); messages(
                player,
                claim
            )
            }
        }
        managementMenu(player, claim, SETTINGS_TAB, items)
    }

    private fun people(player: Player, claim: Claim): Unit = action(player) {
        Permissions.MEMBERS_VIEW.require(player)
        ClaimService.requireOwner(player, claim)
        val items = (claim.region.owners.uniqueIds + claim.region.members.uniqueIds).distinct().map { uuid ->
            memberHead(player, claim, uuid)
        }.toMutableList()
        if (Permissions.MEMBERS_EDIT.allowed(player) || Permissions.OWNERS_EDIT.allowed(player)) items += button(
            Heads.PLUS_CHARACTER,
            ADD_PLAYER.message()
        ) {
            input(player, PLAYER_INPUT.message(), { people(player, claim) }) { name ->
                PlayerProfiles.resolve(player, name) { uuid ->
                    ClaimService.requireOwner(player, claim); chooseRole(
                    player,
                    claim,
                    uuid
                )
                }
            }
        }
        managementMenu(player, claim, PEOPLE, items)
    }

    private fun chooseRole(player: Player, claim: Claim, uuid: UUID): Unit =
        managementMenu(player, claim, PEOPLE, ClaimRole.entries.filter { role ->
            if (role == ClaimRole.OWNER || ClaimService.role(claim, uuid) == ClaimRole.OWNER)
                Permissions.OWNERS_EDIT.allowed(player) else Permissions.MEMBERS_EDIT.allowed(player)
        }.map { role ->
            button(
                RegionHeads.role(role), roleMessage(role), *(when (role) {
                    ClaimRole.OWNER -> arrayOf(OWNER_LORE.message())
                    ClaimRole.VISITOR -> arrayOf(VISITOR_LORE.message())
                    else -> emptyArray()
                })
            ) {
                action(player) {
                    if (role == ClaimRole.OWNER && ClaimService.role(claim, uuid) != ClaimRole.OWNER) {
                        val bounds = ClaimBounds(claim.world, claim.region.minimumPoint, claim.region.maximumPoint)
                        val owners = claim.region.owners.uniqueIds + uuid
                        val cost = ClaimCurrency.plan(player, bounds, owners, listOf(claim)).due
                        menu(
                            player, CHOOSE_ROLE.message(), listOf(
                                button(
                                    Heads.CHECK_MARK, CONFIRM.message(),
                                    COST_LORE.message("cost" to cost)
                                ) {
                                    action(player) {
                                        ClaimService.setRole(player, claim, uuid, role); people(
                                        player,
                                        claim
                                    )
                                    }
                                }), { chooseRole(player, claim, uuid) })
                    } else {
                        ClaimService.setRole(player, claim, uuid, role)
                        people(player, claim)
                    }
                }
            }
        })

    private fun flags(player: Player, claim: Claim, role: ClaimRole?, zoneId: String? = null): Unit = action(player) {
        if (zoneId == null) Permissions.FLAGS_EDIT.require(player) else Permissions.SUBZONES_FLAGS.require(player)
        ClaimService.requireOwner(player, claim)
        if (zoneId != null) ClaimSubzones.requireZone(player, claim, zoneId)
        fun value(flag: Flag<*>) = if (zoneId == null) ClaimFlags.value(claim, flag, role)
        else ClaimSubzones.value(claim, zoneId, flag, role)

        fun set(flag: Flag<*>, value: Any?) {
            if (zoneId == null) ClaimFlags.set(player, claim, flag, role, value)
            else ClaimSubzones.set(player, claim, zoneId, flag, role, value)
        }

        fun provider(viewer: Player, flag: Flag<*>): ItemWrapper {
            val stack = HeadUtils.createCustomHead(RegionHeads.FLAGS.texture, "")
            stack.editMeta { meta ->
                meta.displayName(GuiStyle.title(plain(flag.name)))
                meta.lore(
                    listOf(
                        CURRENT_VALUE.message(
                            "value" to if (zoneId != null && value(flag) == null)
                                MessageUtils.strip(SUBZONE_INHERIT.message().component(viewer)) else valueName(
                                viewer,
                                value(flag)
                            )
                        )
                            .component(viewer),
                        EDIT_TEXT_CONTROL.message().component(viewer),
                        RESET_CONTROL.message().component(viewer)
                    ).map(GuiStyle::lore)
                )
            }
            return ItemWrapper(stack)
        }

        val items = ClaimFlags.available(role).map { flag ->
            if (flag is StateFlag || flag is BooleanFlag) return@map toggle(
                player, { plain(flag.name) }, { value(flag) },
                change = {
                    action(player) {
                        set(flag, ClaimFlags.cycle(value(flag), flag))
                    }
                },
                reset = { action(player) { set(flag, null) } }, inherit = zoneId != null
            )
            Item.builder().setItemProvider { viewer -> provider(viewer, flag) }.addClickHandler { item, click ->
                action(player) {
                    if (click.clickType() == org.bukkit.event.inventory.ClickType.RIGHT) {
                        set(flag, null)
                        item.notifyWindows()
                        return@action
                    }
                    if (click.clickType() != org.bukkit.event.inventory.ClickType.LEFT) return@action
                    val value = value(flag)
                    input(
                        player,
                        INPUT.message(),
                        { flags(player, claim, role, zoneId) },
                        current = value?.toString() ?: "unset"
                    ) { text ->
                        set(flag, ClaimFlags.parse(player, flag, text)); flags(
                        player,
                        claim,
                        role, zoneId
                    )
                    }
                }
            }.build()
        }
        menu(player, FLAGS.message("role" to roleName(player, role)), items, {
            if (zoneId == null) flagScopes(player, claim) else subzoneDetails(player, claim, zoneId)
        }, rows = 3)
    }

    private fun subzones(player: Player, claim: Claim): Unit = action(player) {
        Permissions.SUBZONES_VIEW.require(player)
        ClaimService.requireOwner(player, claim)
        val items = claim.metadata.subzones.map { zone ->
            literalButton(Material.REDSTONE, { plain(zone.name) }, { viewer ->
                MessageUtils.handleLore(
                    SUBZONE_BOUNDS.message(
                        "corners" to
                                "${zone.minX}, ${zone.minY}, ${zone.minZ} → ${zone.maxX}, ${zone.maxY}, ${zone.maxZ}"
                    ).component(viewer)
                )
            }) { subzoneDetails(player, claim, zone.uuid) }
        }.toMutableList()
        if (Permissions.SUBZONES_CREATE.allowed(player)) items += button(
            Heads.PLUS_CHARACTER,
            SUBZONE_CREATE.message()
        ) { subzoneCreate(player, claim) }
        menu(player, SUBZONES.message(), items, { details(player, claim) }, rows = 3)
    }

    private fun subzoneCreate(player: Player, claim: Claim): Unit = action(player) {
        Permissions.SUBZONES_CREATE.require(player)
        ClaimService.requireOwner(player, claim)
        fun corner(first: Boolean) = button(
            if (first) Heads.NUMBER_1 else Heads.NUMBER_2,
            CORNER.message("position" to if (first) 1 else 2),
            SUBZONE_CORNER_LORE.message(
                "position" to (SubzoneSelection.description(player, claim, first)
                    ?: MessageUtils.strip(UNSELECTED.message().component(player)))
            )
        ) {
            action(player) { SubzoneSelection.select(player, claim, first); subzoneCreate(player, claim) }
        }
        menu(
            player, SUBZONE_CREATE.message(), listOf(
                corner(true), corner(false),
                button(Material.SPYGLASS, PREVIEW.message()) {
                    action(player) {
                        ClaimSelection.preview(
                            player,
                            SubzoneSelection.bounds(player, claim)
                        ); player.closeInventory()
                    }
                },
                button(Heads.CHECK_MARK, CONFIRM.message()) {
                    input(player, NAME_INPUT.message(), { subzoneCreate(player, claim) }) { name ->
                        val bounds = SubzoneSelection.bounds(player, claim)
                        menu(
                            player, SUBZONE_CREATE.message(), listOf(
                                button(
                                    Heads.CHECK_MARK, CONFIRM.message(),
                                    SUBZONE_BOUNDS.message("corners" to "${bounds.min} → ${bounds.max}")
                                ) {
                                    action(player) {
                                        val zone = ClaimSubzones.create(player, claim, name)
                                        subzoneDetails(player, claim, zone.uuid)
                                    }
                                }), { subzoneCreate(player, claim) })
                    }
                }, button(Heads.X_CHARACTER, CANCEL.message()) {
                    SubzoneSelection.clear(player.uniqueId); subzones(player, claim)
                }), { subzones(player, claim) })
    }

    private fun subzoneDetails(player: Player, claim: Claim, id: String): Unit = action(player) {
        val zone = ClaimSubzones.requireZone(player, claim, id)
        val open = zone.openProtection && ConfigFile.state.allowOpenSubzones
        val items = mutableListOf(
            button(
                Material.SPYGLASS, PREVIEW.message(), SUBZONE_BOUNDS.message(
                    "corners" to "${zone.minX}, ${zone.minY}, ${zone.minZ} → ${zone.maxX}, ${zone.maxY}, ${zone.maxZ}"
                )
            ) {
                action(player) {
                    ClaimSelection.preview(
                        player,
                        ClaimBounds(claim.world, SubzonePolicies.min(zone), SubzonePolicies.max(zone))
                    )
                    player.closeInventory()
                }
            })
        if (!open && Permissions.SUBZONES_FLAGS.allowed(player)) for (role in listOf(null) + ClaimRole.entries) items += button(
            RegionHeads.role(role),
            FLAGS.message("role" to roleName(player, role)), SUBZONE_FLAGS_LORE.message()
        ) { flags(player, claim, role, id) }
        if (Permissions.SUBZONES_OPEN.allowed(player) && (ConfigFile.state.allowOpenSubzones || zone.openProtection)) items += button(
            if (open) Heads.X_CHARACTER else Heads.CHECK_MARK, SUBZONE_OPEN.message(),
            CURRENT_VALUE.message("value" to valueName(player, open)), SUBZONE_OPEN_LORE.message()
        ) {
            menu(
                player,
                SUBZONE_OPEN.message(),
                listOf(button(Heads.CHECK_MARK, CONFIRM.message(), SUBZONE_OPEN_LORE.message()) {
                    action(player) {
                        ClaimSubzones.open(player, claim, id, !zone.openProtection); subzoneDetails(
                        player,
                        claim,
                        id
                    )
                    }
                }),
                { subzoneDetails(player, claim, id) })
        }
        if (Permissions.SUBZONES_DELETE.allowed(player)) items += button(Material.BARRIER, DELETE.message()) {
            menu(
                player,
                DELETE_CONFIRM.message("region" to zone.name),
                listOf(button(Heads.X_CHARACTER, CONFIRM.message()) {
                    action(player) { ClaimSubzones.delete(player, claim, id); subzones(player, claim) }
                }),
                { subzoneDetails(player, claim, id) })
        }
        menu(player, SUBZONE_TITLE.message("zone" to zone.name), items, { subzones(player, claim) }, rows = 3)
    }

    fun admin(player: Player): Unit = action(player) {
        check(Permissions.ADMIN_CONFIG.allowed(player) || Permissions.ADMIN_POLICIES.allowed(player)) { ADMIN_REQUIRED.key }
        menu(
            player, SERVER.message(), listOf(
                if (Permissions.ADMIN_CONFIG.allowed(player)) button(Heads.TOOLBOX, CONFIG.message()) {
                    configuration(
                        player
                    )
                } else Filler.item(),
                if (Permissions.ADMIN_POLICIES.allowed(player)) button(
                    RegionHeads.FLAGS,
                    POLICIES.message()
                ) { policies(player) } else Filler.item()), { open(player) })
    }

    fun configuration(player: Player): Unit = action(player) {
        Permissions.ADMIN_CONFIG.require(player)
        // The shared editor mutates its instance: edit a snapshot, never the live config.
        val snapshot = ConfigFile.state.copy()
        DataClassConfigGui.openWithBack(
            player, MessageUtils.strip(CONFIG.message().component(player)), snapshot,
            saver = { updated ->
                Permissions.ADMIN_CONFIG.require(player)
                // Keep policy bookkeeping current if flag policies changed during this session.
                val current = ConfigFile.state
                ConfigFile().applyState(
                    updated.copy(
                        flagPolicies = current.flagPolicies,
                        policyRevision = current.policyRevision,
                        policyChanges = current.policyChanges,
                        appliedWorldRevisions = current.appliedWorldRevisions
                    )
                )
            }, back = { admin(player) })
    }

    private fun policies(player: Player): Unit = action(player) {
        Permissions.ADMIN_POLICIES.require(player)
        menu(player, POLICIES.message(), ClaimFlags.all().map { flag ->
            val policy = ClaimFlags.policy(flag)
            literalButton(RegionHeads.FLAGS, { plain(flag.name) }, { viewer ->
                MessageUtils.handleLore(
                    POLICY_LORE.message(
                        "type" to flag.javaClass.simpleName, "enabled" to valueName(viewer, policy.enabled),
                        "scope" to MessageUtils.strip(
                            (if (policy.roleBased) ROLE_SCOPE else GLOBAL).message().component(viewer)
                        )
                    ).component(viewer)
                ) +
                        if (ClaimFlags.locked(flag)) listOf(RESERVED.message().component(viewer)) else
                            (if (policy.roleBased) ClaimRole.entries.map { it as ClaimRole? } else listOf(null)).map { role ->
                                DEFAULT_SUMMARY.message(
                                    "role" to roleName(viewer, role),
                                    "value" to valueName(viewer, ClaimFlags.defaultValue(flag, role))
                                ).component(viewer)
                            }
            }) { flagPolicy(player, flag) }
        }, { admin(player) })
    }

    private fun updatePolicy(
        player: Player,
        flag: Flag<*>,
        reopen: Boolean = true,
        change: (FlagPolicy) -> FlagPolicy
    ) {
        Permissions.ADMIN_POLICIES.require(player)
        check(!ClaimFlags.locked(flag)) { FLAG_RESERVED.key }
        val changed = change(ClaimFlags.policy(flag))
        check(!changed.roleBased || ClaimFlags.supportsRoles(flag)) { FLAG_SCOPE.key }
        ConfigFile().applyState(ConfigFile.state.copy(flagPolicies = ConfigFile.state.flagPolicies + (flag.name to changed)))
        RegionText.send(player, POLICY_SAVED.message())
        if (reopen) flagPolicy(player, flag)
    }

    private fun flagPolicy(player: Player, flag: Flag<*>): Unit = action(player) {
        Permissions.ADMIN_POLICIES.require(player)
        val policy = ClaimFlags.policy(flag)
        if (ClaimFlags.locked(flag)) {
            menu(
                player,
                POLICY.message("flag" to flag.name),
                listOf(button(Heads.X_CHARACTER, RESERVED.message()) {}),
                { policies(player) }); return@action
        }
        val items = mutableListOf(
            toggle(
                player,
                { viewer ->
                    EDIT_ENABLED.message("enabled" to valueName(viewer, ClaimFlags.policy(flag).enabled))
                        .component(viewer)
                },
                { ClaimFlags.policy(flag).enabled },
                change = { updatePolicy(player, flag, reopen = false) { it.copy(enabled = !it.enabled) } }
            ),
            toggle(
                player,
                { viewer ->
                    EDIT_SCOPE.message(
                        "scope" to MessageUtils.strip(
                            (if (ClaimFlags.policy(flag).roleBased) ROLE_SCOPE else GLOBAL).message().component(viewer)
                        )
                    ).component(viewer)
                },
                { ClaimFlags.policy(flag).roleBased },
                change = { updatePolicy(player, flag) { it.copy(roleBased = !it.roleBased) } }
            ))
        for (role in if (policy.roleBased) ClaimRole.entries.map { it as ClaimRole? } else listOf(null)) {
            val value = ClaimFlags.defaultValue(flag, role)
            val key = role?.name ?: "GLOBAL"
            if (flag is StateFlag || flag is BooleanFlag) {
                fun saveDefault(value: Any?) = updatePolicy(player, flag, reopen = false) {
                    it.copy(defaults = it.defaults + (key to ClaimFlags.encode(flag, value)))
                }
                items += toggle(
                    player,
                    { viewer -> DEFAULT.message("role" to roleName(viewer, role)).component(viewer) },
                    { ClaimFlags.defaultValue(flag, role) },
                    change = { saveDefault(ClaimFlags.cycle(ClaimFlags.defaultValue(flag, role), flag)) },
                    reset = { saveDefault(null) }
                )
                continue
            }
            items += button(
                RegionHeads.role(role),
                DEFAULT.message("role" to roleName(player, role)),
                VALUE.message("value" to valueName(player, value))
            ) {
                action(player) {
                    fun save(value: Any?) = updatePolicy(player, flag) {
                        it.copy(
                            defaults = it.defaults + (key to ClaimFlags.encode(
                                flag,
                                value
                            ))
                        )
                    }
                    input(
                        player,
                        DEFAULT_INPUT.message("flag" to flag.name),
                        { flagPolicy(player, flag) }, current = value?.toString() ?: "unset"
                    ) { text -> save(ClaimFlags.parse(player, flag, text)) }
                }
            }
            items += button(Material.STRUCTURE_VOID, CLEAR_DEFAULT.message("role" to roleName(player, role))) {
                action(player) { updatePolicy(player, flag) { it.copy(defaults = it.defaults + (key to "")) } }
            }
        }
        menu(player, POLICY.message("flag" to flag.name), items, { policies(player) })
    }
}
