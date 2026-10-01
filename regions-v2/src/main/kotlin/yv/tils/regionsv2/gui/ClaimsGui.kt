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
import yv.tils.gui.utils.Filler
import yv.tils.gui.utils.HeadUtils
import yv.tils.gui.utils.Heads
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
                meta.displayName(title(viewer).decoration(TextDecoration.ITALIC, false))
                meta.lore(lore(viewer).map { it.decoration(TextDecoration.ITALIC, false) })
            }
            ItemWrapper(item)
        }.addClickHandler { _, _ -> click() }.build()

    private fun pageButton(head: Heads, key: LangStrings, move: (PagedGui<*>) -> Unit): BoundItem.Builder<PagedGui<*>> =
        BoundItem.pagedBuilder().setItemProvider { viewer, gui ->
            val available = if (head == Heads.PREVIOUS_PAGE) gui.page > 0 else gui.page < gui.pageCount - 1
            if (available) HeadUtils.provider(head, viewer, key.key) else Filler.pane()
        }.addClickHandler { _, gui, _ ->
            val available = if (head == Heads.PREVIOUS_PAGE) gui.page > 0 else gui.page < gui.pageCount - 1
            if (available) move(gui)
        }

    private fun menu(
        player: Player, title: RegionMessage, items: List<Item>, back: (() -> Unit)? = null,
        rows: Int? = null, tabs: Map<Char, Item> = emptyMap()
    ) {
        InvUIBootstrap.ensure()
        val contentRows = rows ?: ((minOf(items.size, 28) + 6) / 7).coerceIn(1, 4)
        val top = if (tabs.isEmpty()) "b # # # # # # # #" else "b # i # m # f # s"
        val structure = listOf(top) + List(contentRows) { "# x x x x x x x #" } + "# # # < w > # # #"
        val builder = PagedGui.itemsBuilder().setStructure(*structure.toTypedArray())
            .addIngredient('#', Filler.item())
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('w', BoundItem.pagedBuilder().setItemProvider { viewer, paged ->
                if (paged.pageCount <= 1) return@setItemProvider Filler.pane()
                val stack = HeadUtils.createCustomHead(Heads.I_CHARACTER, "")
                stack.editMeta {
                    it.displayName(
                        PAGE.message(
                            "page" to paged.page + 1,
                            "pages" to maxOf(1, paged.pageCount)
                        ).component(viewer)
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

    private fun input(player: Player, title: RegionMessage, back: () -> Unit, submit: (String) -> Unit) {
        var pending = ""
        val gui = Gui.builder().setStructure("i b c")
            .addIngredient('i', button(Material.ANVIL, INPUT.message()) {})
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

    fun open(player: Player): Unit = action(player) {
        check(player.hasPermission(Permissions.MANAGE.permission.name)) { MANAGE_DENIED.key }
        check(ConfigFile.state.enabled) { DISABLED.key }
        InvUIBootstrap.ensure()
        val structure = listOf("# # # # # # # # #", "# r # c # s # l #", "# # # # # # # # #")
        val gui = Gui.builder().setStructure(*structure.toTypedArray())
            .addIngredient('#', Filler.item())
            .addIngredient('r', button(Material.MAP, CLAIMS.message()) { myRegions(player) })
            .addIngredient(
                'c',
                button(Heads.PLUS_CHARACTER, CREATE.message(), CREATE_LORE.message()) { creation(player) })
            .addIngredient(
                's', if (ClaimService.admin(player))
                    button(Heads.TOOLBOX, SERVER.message()) { admin(player) } else Filler.item())
            .addIngredient('l', button(Material.SPYGLASS, INSPECT.message()) {
                action(player) {
                    player.closeInventory()
                    val claim = ClaimService.at(player.location)
                    if (claim == null) RegionText.send(player, NO_CLAIM_HERE.message())
                    else ClaimInformation.sendSummary(player, claim)
                }
            }).build()
        show(player, MAIN_MENU.message(), gui)
    }

    private fun myRegions(player: Player): Unit = action(player) {
        check(player.hasPermission(Permissions.MANAGE.permission.name)) { MANAGE_DENIED.key }
        check(ConfigFile.state.enabled) { DISABLED.key }
        val items = ClaimService.all()
            .filter { ClaimService.admin(player) || ClaimService.role(it, player.uniqueId) != ClaimRole.VISITOR }
            .sortedWith(compareBy<Claim> { it.world != player.world }.thenBy { it.name.lowercase() }.thenBy { it.uuid })
            .map { claim ->
                literalButton(Material.MAP, { plain(claim.name) }, { viewer ->
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
        if (items.isEmpty()) items += button(Material.MAP, EMPTY_CLAIMS.message(), CREATE_LORE.message()) {
            creation(player)
        }
        menu(player, CLAIMS.message(), items, { open(player) }, rows = 3)
    }

    private fun creation(player: Player): Unit = action(player) {
        check(player.hasPermission(Permissions.CLAIM.permission.name)) { CREATE_DENIED.key }
        menu(
            player, CREATE.message(), listOf(
                cornerButton(player, true),
                cornerButton(player, false),
                button(
                    Material.PAPER,
                    PREVIEW.message()
                ) { action(player) { ClaimSelection.preview(player); player.closeInventory() } },
                button(Heads.CHECK_MARK, CREATE.message()) {
                    input(player, NAME_INPUT.message(), { creation(player) }) { name ->
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
                },
                button(Heads.X_CHARACTER, CLEAR.message()) { ClaimSelection.clear(player.uniqueId); creation(player) }
            ), { open(player) })
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
            player, CLAIM.message("region" to claim.name), items, { myRegions(player) }, rows = 3, tabs = mapOf(
                'i' to tab({ HeadUtils.createCustomHead(Heads.I_CHARACTER, "") }, INFORMATION) {
                    details(
                        player,
                        claim
                    )
                },
                'm' to tab({ HeadUtils.createCustomHead(RegionHeads.MEMBERS.texture, "") }, PEOPLE) {
                    people(
                        player,
                        claim
                    )
                },
                'f' to tab(
                    { HeadUtils.createCustomHead(RegionHeads.FLAGS.texture, "") },
                    FLAGS_TAB
                ) { flagScopes(player, claim) },
                's' to tab({ HeadUtils.createCustomHead(Heads.TOOLBOX, "") }, SETTINGS_TAB) { settings(player, claim) }
            ))
    }

    private fun flagScopes(player: Player, claim: Claim): Unit = action(player) {
        val editable = ClaimService.admin(player) || ClaimService.role(claim, player.uniqueId) == ClaimRole.OWNER
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
                meta.displayName(plain(offline.name ?: uuid.toString()))
                meta.lore(
                    MessageUtils.handleLore(
                        ROLE_LORE.message("role" to roleName(viewer, ClaimService.role(claim, uuid))).component(viewer)
                    ).map { it.decoration(TextDecoration.ITALIC, false) })
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
        items += button(Material.NAME_TAG, RENAME.message()) {
            input(player, NAME_INPUT.message(), { settings(player, claim) }) { name ->
                ClaimService.rename(
                    player,
                    claim,
                    name
                ); settings(player, claim)
            }
        }
        items += button(Material.CARTOGRAPHY_TABLE, RESIZE.message(), RESIZE_LORE.message()) {
            action(player) {
                val bounds = ClaimSelection.bounds(player)
                menu(
                    player, RESIZE.message(), listOf(
                        button(
                            Heads.CHECK_MARK, CONFIRM.message(),
                            COST_LORE.message(
                                "cost" to ClaimCurrency.quote(
                                    player,
                                    bounds,
                                    ClaimCurrency.credit(claim)
                                )
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
        items += button(Material.WRITABLE_BOOK, MERGE.message()) {
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
                                "cost" to ClaimCurrency.quote(
                                    player, bounds,
                                    Math.addExact(ClaimCurrency.credit(claim), ClaimCurrency.credit(other))
                                )
                            )
                        ) {
                            action(player) { details(player, ClaimService.merge(player, claim, other)) }
                        }),
                    { settings(player, claim) })
            }
        }
        items += button(Material.STRUCTURE_VOID, RESTORE_DEFAULTS.message(), RESTORE_LORE.message()) {
            menu(
                player, RESTORE_DEFAULTS.message(), listOf(
                    button(Heads.CHECK_MARK, CONFIRM.message(), RESTORE_LORE.message()) {
                        action(player) { ClaimFlags.restoreDefaults(player, claim); settings(player, claim) }
                    }), { settings(player, claim) })
        }
        if (ClaimService.admin(player)) items += button(Heads.ENVELOPE, MESSAGES.message()) { messages(player, claim) }
        items += button(Material.BARRIER, DELETE.message(), DELETE_LORE.message()) {
            menu(
                player, DELETE_CONFIRM.message("region" to claim.name), listOf(
                    button(Heads.X_CHARACTER, CONFIRM.message(), DELETE_LORE.message()) {
                        action(player) {
                            ClaimService.delete(player, claim)
                            RegionText.send(player, COMPLETED.message())
                            myRegions(player)
                        }
                    }), { settings(player, claim) })
        }
        managementMenu(player, claim, SETTINGS_TAB, items)
    }

    private fun messages(player: Player, claim: Claim): Unit = action(player) {
        check(ClaimService.admin(player)) { ADMIN_REQUIRED.key }
        ClaimService.requireOwner(player, claim)
        val items = mutableListOf<Item>()
        for (welcome in listOf(true, false)) items += button(
            Heads.ENVELOPE,
            (if (welcome) WELCOME else GOODBYE).message(),
            MESSAGE_LORE.message()
        ) {
            input(player, (if (welcome) WELCOME else GOODBYE).message(), { messages(player, claim) }) { text ->
                ClaimService.messages(player, claim, welcome, if (text == "unset") "" else text); messages(
                player,
                claim
            )
            }
        }
        managementMenu(player, claim, SETTINGS_TAB, items)
    }

    private fun people(player: Player, claim: Claim): Unit = action(player) {
        ClaimService.requireOwner(player, claim)
        val items = (claim.region.owners.uniqueIds + claim.region.members.uniqueIds).distinct().map { uuid ->
            memberHead(player, claim, uuid)
        }.toMutableList()
        items += button(Heads.PLUS_CHARACTER, ADD_PLAYER.message()) {
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

    private fun chooseRole(player: Player, claim: Claim, uuid: UUID) =
        managementMenu(player, claim, PEOPLE, ClaimRole.entries.map { role ->
            button(
                RegionHeads.role(role), roleMessage(role), *(when (role) {
                    ClaimRole.OWNER -> arrayOf(OWNER_LORE.message())
                    ClaimRole.VISITOR -> arrayOf(VISITOR_LORE.message())
                    else -> emptyArray()
                })
            ) { action(player) { ClaimService.setRole(player, claim, uuid, role); people(player, claim) } }
        })

    private fun flags(player: Player, claim: Claim, role: ClaimRole?): Unit = action(player) {
        ClaimService.requireOwner(player, claim)
        val items = ClaimFlags.available(role).map { flag ->
            val value = ClaimFlags.value(claim, flag, role)
            literalButton(
                if (value == StateFlag.State.DENY || value == false) Heads.X_CHARACTER else Heads.CHECK_MARK,
                { plain(flag.name) },
                { viewer ->
                    MessageUtils.handleLore(VALUE.message("value" to valueName(viewer, value)).component(viewer))
                }) {
                action(player) {
                    if (flag is StateFlag || flag is BooleanFlag) {
                        ClaimFlags.set(
                            player,
                            claim,
                            flag,
                            role,
                            ClaimFlags.cycle(ClaimFlags.value(claim, flag, role), flag)
                        ); flags(player, claim, role)
                    } else input(player, INPUT.message(), { flags(player, claim, role) }) { text ->
                        ClaimFlags.set(player, claim, flag, role, ClaimFlags.parse(player, flag, text)); flags(
                        player,
                        claim,
                        role
                    )
                    }
                }
            }
        }
        menu(player, FLAGS.message("role" to roleName(player, role)), items, { flagScopes(player, claim) }, rows = 3)
    }

    fun admin(player: Player): Unit = action(player) {
        check(ClaimService.admin(player)) { ADMIN_REQUIRED.key }
        menu(
            player, SERVER.message(), listOf(
                button(Heads.TOOLBOX, CONFIG.message()) { configuration(player) },
                button(RegionHeads.FLAGS, POLICIES.message()) { policies(player) }), { open(player) })
    }

    fun configuration(player: Player): Unit = action(player) {
        check(ClaimService.admin(player)) { ADMIN_REQUIRED.key }
        val config = ConfigFile.state
        val numbers = listOf(
            MIN_AREA to config.minClaimArea,
            MAX_TOTAL to config.maxClaimsTotal.toLong(),
            MAX_WORLD to config.maxClaimsPerWorld.toLong(),
            MAX_MEMBERS to config.maxMembersPerClaim.toLong(),
            MAX_MEMBERSHIPS to config.maxMembershipsPerPlayer.toLong(),
            MAX_VOLUME to config.maxClaimVolume,
            MAX_SIDE to config.maxClaimSide.toLong(),
            FREE_CHUNKS to config.freeClaimChunks.toLong(),
            DIAMONDS_PER_CHUNK to config.diamondsPerChunk.toLong()
        )
        val items = numbers.map { (label, value) ->
            button(Heads.CHART, label.message(), LIMIT_LORE.message("value" to value)) {
                input(player, label.message(), { configuration(player) }) { text ->
                    check(ClaimService.admin(player)) { ADMIN_REQUIRED.key }
                    val number = text.toLongOrNull() ?: error(INVALID_INPUT.key)
                    check(
                        label in listOf(
                            MIN_AREA,
                            MAX_VOLUME
                        ) || number in -1..Int.MAX_VALUE.toLong()
                    ) { INVALID_INPUT.key }
                    val current = ConfigFile.state
                    ConfigFile().applyState(
                        when (label) {
                            MIN_AREA -> current.copy(minClaimArea = number)
                            MAX_TOTAL -> current.copy(maxClaimsTotal = number.toInt())
                            MAX_WORLD -> current.copy(maxClaimsPerWorld = number.toInt())
                            MAX_MEMBERS -> current.copy(maxMembersPerClaim = number.toInt())
                            MAX_MEMBERSHIPS -> current.copy(maxMembershipsPerPlayer = number.toInt())
                            MAX_VOLUME -> current.copy(maxClaimVolume = number)
                            FREE_CHUNKS -> current.copy(freeClaimChunks = number.toInt())
                            DIAMONDS_PER_CHUNK -> current.copy(diamondsPerChunk = number.toInt())
                            else -> current.copy(maxClaimSide = number.toInt())
                        }
                    ); configuration(player)
                }
            }
        }.toMutableList()
        for (label in listOf(ENABLED, SURVIVAL, TRANSITIONS, CURRENCY)) items += button(
            Heads.CHECK_MARK, label.message(), VALUE.message(
                "value" to valueName(
                    player,
                    when (label) {
                        ENABLED -> config.enabled; SURVIVAL -> config.survivalOnly
                        CURRENCY -> config.currencyEnabled
                        else -> config.actionBarTransitions
                    }
                )
            )
        ) {
            action(player) {
                check(ClaimService.admin(player)) { ADMIN_REQUIRED.key }
                val current = ConfigFile.state
                ConfigFile().applyState(
                    when (label) {
                        ENABLED -> current.copy(enabled = !current.enabled); SURVIVAL -> current.copy(survivalOnly = !current.survivalOnly)
                        CURRENCY -> current.copy(currencyEnabled = !current.currencyEnabled)
                        else -> current.copy(
                            actionBarTransitions = !current.actionBarTransitions
                        )
                    }
                )
                configuration(player)
            }
        }
        items += button(
            Heads.SHIELD,
            DISABLED_WORLDS.message(),
            WORLDS_LORE.message("worlds" to config.disabledWorlds.joinToString(", "))
        ) {
            input(player, DISABLED_WORLDS.message(), { configuration(player) }) { text ->
                check(ClaimService.admin(player)) { ADMIN_REQUIRED.key }
                ConfigFile().applyState(ConfigFile.state.copy(disabledWorlds = text.split(',').map { it.trim() }
                    .filter { it.isNotEmpty() })); configuration(player)
            }
        }
        menu(player, CONFIG.message(), items, { admin(player) })
    }

    private fun policies(player: Player): Unit = action(player) {
        check(ClaimService.admin(player)) { ADMIN_REQUIRED.key }
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

    private fun updatePolicy(player: Player, flag: Flag<*>, change: (FlagPolicy) -> FlagPolicy) {
        check(ClaimService.admin(player)) { ADMIN_REQUIRED.key }
        check(!ClaimFlags.locked(flag)) { FLAG_RESERVED.key }
        val changed = change(ClaimFlags.policy(flag))
        check(!changed.roleBased || ClaimFlags.supportsRoles(flag)) { FLAG_SCOPE.key }
        ConfigFile().applyState(ConfigFile.state.copy(flagPolicies = ConfigFile.state.flagPolicies + (flag.name to changed)))
        RegionText.send(player, POLICY_SAVED.message()); flagPolicy(player, flag)
    }

    private fun flagPolicy(player: Player, flag: Flag<*>): Unit = action(player) {
        check(ClaimService.admin(player)) { ADMIN_REQUIRED.key }
        val policy = ClaimFlags.policy(flag)
        if (ClaimFlags.locked(flag)) {
            menu(
                player,
                POLICY.message("flag" to flag.name),
                listOf(button(Heads.X_CHARACTER, RESERVED.message()) {}),
                { policies(player) }); return@action
        }
        val items = mutableListOf(
            button(
                if (policy.enabled) Heads.CHECK_MARK else Heads.X_CHARACTER,
                EDIT_ENABLED.message("enabled" to valueName(player, policy.enabled)), RESET_LORE.message()
            ) {
                action(player) { updatePolicy(player, flag) { it.copy(enabled = !it.enabled) } }
            },
            button(
                RegionHeads.FLAGS,
                EDIT_SCOPE.message(
                    "scope" to MessageUtils.strip(
                        (if (policy.roleBased) ROLE_SCOPE else GLOBAL).message().component(player)
                    )
                ),
                RESET_LORE.message()
            ) {
                action(player) { updatePolicy(player, flag) { it.copy(roleBased = !it.roleBased) } }
            })
        for (role in if (policy.roleBased) ClaimRole.entries.map { it as ClaimRole? } else listOf(null)) {
            val value = ClaimFlags.defaultValue(flag, role)
            val key = role?.name ?: "GLOBAL"
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
                    if (flag is StateFlag || flag is BooleanFlag) save(
                        ClaimFlags.cycle(
                            ClaimFlags.defaultValue(
                                flag,
                                role
                            ), flag
                        )
                    )
                    else input(
                        player,
                        DEFAULT_INPUT.message("flag" to flag.name),
                        { flagPolicy(player, flag) }) { text -> save(ClaimFlags.parse(player, flag, text)) }
                }
            }
            items += button(Material.STRUCTURE_VOID, CLEAR_DEFAULT.message("role" to roleName(player, role))) {
                action(player) { updatePolicy(player, flag) { it.copy(defaults = it.defaults + (key to "")) } }
            }
        }
        menu(player, POLICY.message("flag" to flag.name), items, { policies(player) })
    }
}
