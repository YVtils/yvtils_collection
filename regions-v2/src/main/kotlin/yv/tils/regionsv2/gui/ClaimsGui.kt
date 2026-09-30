/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.regionsv2.gui

import com.sk89q.worldguard.protection.flags.BooleanFlag
import com.sk89q.worldguard.protection.flags.Flag
import com.sk89q.worldguard.protection.flags.StateFlag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player
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
import yv.tils.regionsv2.language.*
import yv.tils.regionsv2.language.LangStrings.*
import yv.tils.regionsv2.logic.*
import yv.tils.utils.colors.Colors
import yv.tils.utils.message.MessageUtils
import java.util.UUID

object ClaimsGui {
    fun action(player: Player, action: () -> Unit) = RegionText.action(player, action)
    private fun plain(text: String) = Component.text(text).decoration(TextDecoration.ITALIC, false)
    private fun button(head: Heads, title: RegionMessage, vararg lore: RegionMessage, click: () -> Unit): Item =
        literalButton(
            head,
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
    ): Item =
        Item.builder().setItemProvider { viewer ->
            val stack = HeadUtils.createCustomHead(head, "")
            stack.editMeta { meta ->
                meta.displayName(title(viewer).decoration(TextDecoration.ITALIC, false))
                meta.lore(lore(viewer).map { it.decoration(TextDecoration.ITALIC, false) })
            }
            ItemWrapper(stack)
        }.addClickHandler { _, _ -> click() }.build()

    private fun pageButton(head: Heads, key: LangStrings, move: (PagedGui<*>) -> Unit): BoundItem.Builder<PagedGui<*>> =
        BoundItem.pagedBuilder().setItemProvider { viewer, gui ->
            val available = if (head == Heads.PREVIOUS_PAGE) gui.page > 0 else gui.page < gui.pageCount - 1
            if (available) HeadUtils.provider(head, viewer, key.key) else Filler.pane()
        }.addClickHandler { _, gui, _ -> move(gui) }

    private fun menu(player: Player, title: RegionMessage, items: List<Item>, back: (() -> Unit)? = null) {
        InvUIBootstrap.ensure()
        val rows = ((minOf(items.size, 28) + 6) / 7).coerceIn(1, 4)
        val structure = listOf("# # # # # # # # #") + List(rows) { "# x x x x x x x #" } + "# < # # b # # > #"
        val gui = PagedGui.itemsBuilder().setStructure(*structure.toTypedArray())
            .addIngredient('#', Filler.item())
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('<', pageButton(Heads.PREVIOUS_PAGE, PREVIOUS) { it.page-- })
            .addIngredient('>', pageButton(Heads.NEXT_PAGE, NEXT) { it.page++ })
            .addIngredient('b', button(Heads.PREVIOUS_PAGE, (if (back == null) CLOSE else BACK).message()) {
                if (back == null) player.closeInventory() else back()
            }).setContent(items).build()
        Window.builder().setTitle(MessageUtils.convert("<${Colors.MAIN.color}>").append(title.component(player)))
            .setUpperGui(gui).open(player)
    }

    private fun input(player: Player, title: RegionMessage, back: () -> Unit, submit: (String) -> Unit) {
        var pending = ""
        val gui = Gui.builder().setStructure("i b c")
            .addIngredient('i', button(Heads.I_CHARACTER, INPUT.message()) {})
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
        val items = ClaimService.all()
            .filter { ClaimService.admin(player) || ClaimService.role(it, player.uniqueId) != ClaimRole.VISITOR }
            .map { claim ->
                literalButton(Heads.SHIELD, { plain(claim.name) }, { viewer ->
                    MessageUtils.handleLore(
                        CLAIM_LORE.message(
                            "world" to claim.world.name,
                            "role" to roleName(viewer, ClaimService.role(claim, viewer.uniqueId))
                        ).component(viewer)
                    )
                }) { details(player, claim) }
            }.toMutableList()
        if (player.hasPermission(Permissions.CLAIM.permission.name)) items += button(
            Heads.PLUS_CHARACTER,
            CREATE.message(),
            CREATE_LORE.message()
        ) { creation(player) }
        items += button(Heads.I_CHARACTER, INSPECT.message()) {
            action(player) {
                details(
                    player,
                    ClaimService.at(player.location) ?: error(NO_CLAIM_HERE.key)
                )
            }
        }
        if (ClaimService.admin(player)) items += button(Heads.SERVER_RACK, SERVER.message()) { admin(player) }
        menu(player, CLAIMS.message(), items)
    }

    private fun creation(player: Player): Unit = action(player) {
        check(player.hasPermission(Permissions.CLAIM.permission.name)) { CREATE_DENIED.key }
        menu(
            player, CREATE.message(), listOf(
                cornerButton(player, true),
                cornerButton(player, false),
                button(
                    Heads.I_CHARACTER,
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
                                    )
                                ) {
                                    action(player) { details(player, ClaimService.create(player, name)) }
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
        val items = mutableListOf(button(Heads.I_CHARACTER, PREVIEW.message()) {
            action(player) {
                ClaimSelection.preview(
                    player,
                    ClaimBounds(claim.world, claim.region.minimumPoint, claim.region.maximumPoint)
                )
            }
        }, button(Heads.I_CHARACTER, INFORMATION.message()) {
            menu(
                player,
                INFORMATION.message(),
                ClaimInformation.lines(player, claim).map { line -> literalButton(Heads.I_CHARACTER, { line }) {} },
                { details(player, claim) })
        })
        if (ClaimService.admin(player) || ClaimService.role(claim, player.uniqueId) == ClaimRole.OWNER) {
            items += button(Heads.HEART_RED, PEOPLE.message()) { people(player, claim) }
            for (role in listOf(null) + ClaimRole.entries) items += literalButton(
                Heads.TOOLBOX,
                { FLAGS.message("role" to roleName(it, role)).component(it) }) { flags(player, claim, role) }
            items += button(Heads.X_CHARACTER, DELETE.message()) {
                menu(
                    player,
                    DELETE_CONFIRM.message("region" to claim.name),
                    listOf(button(Heads.X_CHARACTER, CONFIRM.message(), DELETE_LORE.message()) {
                        action(player) {
                            ClaimService.delete(player, claim); RegionText.send(
                            player,
                            COMPLETED.message()
                        ); open(player)
                        }
                    }),
                    { details(player, claim) })
            }
            items += button(Heads.I_CHARACTER, RENAME.message()) {
                input(player, NAME_INPUT.message(), { details(player, claim) }) { name ->
                    ClaimService.rename(
                        player,
                        claim,
                        name
                    ); details(player, claim)
                }
            }
            items += button(Heads.SHIELD, RESIZE.message(), RESIZE_LORE.message()) {
                menu(player, RESIZE.message(), listOf(button(Heads.CHECK_MARK, CONFIRM.message()) {
                    action(player) {
                        details(
                            player,
                            ClaimService.resize(player, claim, ClaimSelection.bounds(player))
                        )
                    }
                }), { details(player, claim) })
            }
            items += button(Heads.SHIELD, MERGE.message()) {
                input(player, MERGE_INPUT.message(), { details(player, claim) }) { name ->
                    val other = ClaimService.find(claim.world, name)
                    menu(
                        player,
                        MERGE.message(),
                        listOf(button(Heads.CHECK_MARK, CONFIRM.message(), MERGE_LORE.message("region" to other.name)) {
                            action(player) { details(player, ClaimService.merge(player, claim, other)) }
                        }),
                        { details(player, claim) })
                }
            }
            for (welcome in listOf(true, false)) items += button(
                Heads.ENVELOPE,
                (if (welcome) WELCOME else GOODBYE).message(),
                MESSAGE_LORE.message()
            ) {
                input(player, (if (welcome) WELCOME else GOODBYE).message(), { details(player, claim) }) { text ->
                    ClaimService.messages(player, claim, welcome, if (text == "unset") "" else text); details(
                    player,
                    claim
                )
                }
            }
        }
        menu(player, CLAIM.message("region" to claim.name), items, { open(player) })
    }

    private fun people(player: Player, claim: Claim): Unit = action(player) {
        ClaimService.requireOwner(player, claim)
        val items = (claim.region.owners.uniqueIds + claim.region.members.uniqueIds).distinct().map { uuid ->
            literalButton(Heads.HEART_RED, { plain(Bukkit.getOfflinePlayer(uuid).name ?: uuid.toString()) }, { viewer ->
                MessageUtils.handleLore(
                    ROLE_LORE.message("role" to roleName(viewer, ClaimService.role(claim, uuid))).component(viewer)
                )
            }) { chooseRole(player, claim, uuid) }
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
        menu(player, PEOPLE.message(), items, { details(player, claim) })
    }

    private fun chooseRole(player: Player, claim: Claim, uuid: UUID) =
        menu(player, CHOOSE_ROLE.message(), ClaimRole.entries.map { role ->
            button(
                Heads.HEART_RED, roleMessage(role), *(when (role) {
                    ClaimRole.OWNER -> arrayOf(OWNER_LORE.message())
                    ClaimRole.VISITOR -> arrayOf(VISITOR_LORE.message())
                    else -> emptyArray()
                })
            ) { action(player) { ClaimService.setRole(player, claim, uuid, role); people(player, claim) } }
        }, { people(player, claim) })

    private fun flags(player: Player, claim: Claim, role: ClaimRole?): Unit = action(player) {
        ClaimService.requireOwner(player, claim)
        menu(player, FLAGS.message("role" to roleName(player, role)), ClaimFlags.available(role).map { flag ->
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
        }, { details(player, claim) })
    }

    fun admin(player: Player): Unit = action(player) {
        check(ClaimService.admin(player)) { ADMIN_REQUIRED.key }
        menu(
            player, SERVER.message(), listOf(
                button(Heads.SERVER_RACK, CONFIG.message()) { configuration(player) },
                button(Heads.TOOLBOX, POLICIES.message()) { policies(player) }), { open(player) })
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
            MAX_SIDE to config.maxClaimSide.toLong()
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
                            else -> current.copy(maxClaimSide = number.toInt())
                        }
                    ); configuration(player)
                }
            }
        }.toMutableList()
        for (label in listOf(ENABLED, SURVIVAL, TRANSITIONS)) items += button(
            Heads.CHECK_MARK, label.message(), VALUE.message(
                "value" to valueName(
                    player,
                    when (label) {
                        ENABLED -> config.enabled; SURVIVAL -> config.survivalOnly; else -> config.actionBarTransitions
                    }
                )
            )
        ) {
            action(player) {
                check(ClaimService.admin(player)) { ADMIN_REQUIRED.key }
                val current = ConfigFile.state
                ConfigFile().applyState(
                    when (label) {
                        ENABLED -> current.copy(enabled = !current.enabled); SURVIVAL -> current.copy(survivalOnly = !current.survivalOnly); else -> current.copy(
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
            literalButton(if (policy.enabled) Heads.CHECK_MARK else Heads.X_CHARACTER, { plain(flag.name) }, { viewer ->
                MessageUtils.handleLore(
                    POLICY_LORE.message(
                        "type" to flag.javaClass.simpleName, "enabled" to valueName(viewer, policy.enabled),
                        "scope" to MessageUtils.strip(
                            (if (policy.roleBased) ROLE_SCOPE else GLOBAL).message().component(viewer)
                        )
                    ).component(viewer)
                ) +
                        if (ClaimFlags.locked(flag)) listOf(RESERVED.message().component(viewer)) else emptyList()
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
                Heads.TOOLBOX,
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
                Heads.CHART,
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
            items += button(Heads.X_CHARACTER, CLEAR_DEFAULT.message("role" to roleName(player, role))) {
                action(player) { updatePolicy(player, flag) { it.copy(defaults = it.defaults + (key to "")) } }
            }
        }
        menu(player, POLICY.message("flag" to flag.name), items, { policies(player) })
    }
}
