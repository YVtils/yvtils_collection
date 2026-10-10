/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.gui

import yv.tils.fusion.FusionRecipe
import yv.tils.fusion.Ingredient
import yv.tils.fusion.FusionYVtils
import yv.tils.fusion.language.FusionText
import yv.tils.fusion.logic.crafting.Crafting
import yv.tils.fusion.logic.crafting.IngredientAllocation
import yv.tils.fusion.logic.items.FusionItems
import yv.tils.fusion.utils.BukkitTags

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.profile.PlayerProfile
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.Window
import yv.tils.gui.utils.Filler
import yv.tils.gui.utils.GuiStyle
import yv.tils.utils.colors.Colors
import yv.tils.utils.modules.Core
import java.util.UUID
import java.util.concurrent.TimeUnit
import yv.tils.fusion.api.OutputSelection
import yv.tils.fusion.api.FusionRegistry
import java.util.function.Consumer

object FusionGui {
    fun allowed(player: Player, manage: Boolean = false): Boolean {
        if (!FusionYVtils.active) return false
        val permission = if (manage) "yvtils.fusion.manage" else "yvtils.fusion.use"
        if (!player.hasPermission(permission)) {
            FusionText.send(player, "denied"); return false
        }
        return true
    }

    internal fun button(
        player: Player, material: Material, key: String, params: Map<String, Any> = emptyMap(),
        click: (Player) -> Unit
    ): Item = Item.builder()
        .setItemProvider { GuiStyle.field(material, FusionText.text(player, key, params), emptyList()) }
        .addClickHandler { _, event -> click(event.player()) }.build()

    internal fun show(player: Player, key: String, gui: Gui) = Window.builder()
        .setTitle(GuiStyle.title(FusionText.text(player, key), Colors.MAIN)).setUpperGui(gui).open(player)

    internal fun page(player: Player, next: Boolean, manage: Boolean = false) = BoundItem.pagedBuilder()
        .setItemProvider { _, gui ->
            GuiStyle.field(
                if (if (next) gui.page < gui.pageCount - 1 else gui.page > 0) Material.ARROW else Material.GRAY_STAINED_GLASS_PANE,
                FusionText.text(player, if (next) "next" else "previous"), emptyList()
            )
        }
        .addClickHandler { _, gui, event -> if (allowed(event.player(), manage)) gui.page += if (next) 1 else -1 }
        .build()

    internal fun input(
        player: Player, key: String, initial: String, manage: Boolean = false,
        cancel: (Player) -> Unit, submit: (Player, String) -> Unit
    ) {
        if (!allowed(player, manage)) return
        var pending = initial
        var submitted = false
        val gui = Gui.builder().setStructure("i n y").addIngredient('i', GuiStyle.inputPaper(initial))
            .addIngredient('n', button(player, Material.BARRIER, "cancel") { if (allowed(it, manage)) cancel(it) })
            .addIngredient('y', button(player, Material.LIME_DYE, "confirm") {
                if (!submitted && allowed(it, manage)) {
                    submitted = true; submit(it, pending.trim())
                }
            }).build()
        AnvilWindow.builder().setTitle(GuiStyle.title(FusionText.text(player, key), Colors.MAIN))
            .setUpperGui(gui).addRenameHandler { pending = it }.open(player)
    }

    internal fun ingredientName(ingredient: Ingredient) = when {
        ingredient.materials.toSet() == yv.tils.fusion.configs.DefaultRecipes.glassMaterials.toSet() -> "Glass (any color)"
        ingredient.exactItem.isNotBlank() -> FusionItems.decode(ingredient.exactItem).type.name + " (exact)"
        ingredient.fusionId.isNotBlank() -> "@${ingredient.fusionId}"
        ingredient.tag.isNotBlank() -> "#${ingredient.tag}"
        else -> ingredient.materials.joinToString(" / ")
    }

    private fun allocation(player: Player, recipe: FusionRecipe): IngredientAllocation.Plan {
        val contents = player.inventory.storageContents
        return IngredientAllocation.plan(
            IntArray(contents.size) { contents[it]?.amount ?: 0 },
            IntArray(recipe.ingredients.size) { recipe.ingredients[it].amount }) { slot, demand ->
            contents[slot]?.let { recipe.ingredients[demand].matches(it) } == true
        }
    }

    private fun lore(player: Player, recipe: FusionRecipe): List<Component> {
        val allocation = allocation(player, recipe)
        return listOf(
            GuiStyle.lore(Component.text(recipe.description)), GuiStyle.lore(Component.text(recipe.category)),
            FusionStyle.status(
                FusionText.text(player, "xpStatus", mapOf("have" to player.level, "need" to recipe.xpLevels)),
                player.level >= recipe.xpLevels
            )
        ) +
                recipe.ingredients.mapIndexed { i, ingredient ->
                    FusionStyle.status(
                        FusionText.text(
                            player, "ingredientSummary", mapOf(
                                "mark" to if (allocation.missing[i] == 0) "✓" else "✗",
                                "have" to ingredient.amount - allocation.missing[i], "need" to ingredient.amount,
                                "ingredient" to ingredientName(ingredient)
                            )
                        ), allocation.missing[i] == 0
                    )
                } +
                if (recipe.permission.isNotBlank() && !player.hasPermission(recipe.permission))
                    listOf(
                        FusionStyle.status(
                            FusionText.text(
                                player,
                                "locked",
                                mapOf("permission" to recipe.permission)
                            ), false
                        )
                    )
                else listOf(
                    FusionStyle.status(
                        FusionText.text(
                            player,
                            if (allocation.complete) "available" else "missingIngredients"
                        ), allocation.complete
                    )
                )
    }

    fun browser(player: Player, search: String = "", category: String = "", manage: Boolean = false) {
        if (!allowed(player, manage)) return
        val recipes = FusionRegistry.recipes().filter {
            (!manage || it.id !in FusionRegistry.runtimeIds()) &&
                    (manage || it.enabled) && (category.isBlank() || it.category == category) &&
                    "${it.name} ${it.id} ${it.description}".contains(search, ignoreCase = true)
        }.sortedWith(compareByDescending<FusionRecipe> {
            val inventory = player.inventory.storageContents
            IngredientAllocation.plan(
                IntArray(inventory.size) { index -> inventory[index]?.amount ?: 0 },
                IntArray(it.ingredients.size) { index -> it.ingredients[index].amount }) { slot, demand ->
                inventory[slot]?.let { stack -> it.ingredients[demand].matches(stack) } == true
            }.complete && player.level >= it.xpLevels && (it.permission.isBlank() || player.hasPermission(it.permission))
        }.thenBy { it.name })
        val items = recipes.map { recipe ->
            Item.builder().setItemProvider {
                FusionStyle.item(
                    if (recipe.exactThumbnail.isBlank()) ItemStack(Material.valueOf(recipe.thumbnail))
                    else FusionItems.decode(recipe.exactThumbnail), Component.text(recipe.name),
                    (if (!recipe.enabled) listOf(
                        FusionStyle.status(
                            FusionText.text(player, "disabled"),
                            false
                        )
                    ) else emptyList()) + lore(
                        player,
                        recipe
                    ) + listOf(GuiStyle.lore(FusionText.text(player, if (manage) "editHint" else "recipeHint"))),
                    if (!recipe.enabled || !allocation(player, recipe).complete || player.level < recipe.xpLevels ||
                        recipe.permission.isNotBlank() && !player.hasPermission(recipe.permission)
                    ) Colors.RED else Colors.GREEN
                )
            }.addClickHandler { _, event ->
                if (allowed(event.player(), manage)) {
                    if (manage) RecipeEditor.open(event.player(), recipe, recipe)
                    else details(event.player(), recipe)
                }
            }.build()
        }.ifEmpty { listOf(Item.simple(GuiStyle.field(Material.PAPER, FusionText.text(player, "empty"), emptyList()))) }
        val categories = listOf("") + FusionRegistry.recipes().map { it.category }.distinct().sorted()
        val gui = PagedGui.itemsBuilder().setStructure(
            "# # s # c # m n #", "# x x x x x x x #", "# x x x x x x x #",
            "# x x x x x x x #", "# # # # # # # # #", "# # # < r > # # #"
        )
            .addIngredient('#', Filler.item()).addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('<', page(player, false, manage)).addIngredient('>', page(player, true, manage))
            .addIngredient('s', button(player, Material.COMPASS, "search") { viewer ->
                if (allowed(viewer, manage)) input(
                    viewer, "search", search, manage,
                    { browser(it, search, category, manage) }) { actor, value ->
                    browser(
                        actor,
                        value,
                        category,
                        manage
                    )
                }
            })
            .addIngredient(
                'c',
                button(player, Material.HOPPER, "category", mapOf("value" to category.ifBlank { "All" })) {
                    browser(it, search, categories[(categories.indexOf(category) + 1) % categories.size], manage)
                })
            .addIngredient('r', button(player, Material.SUNFLOWER, "refresh") { browser(it, search, category, manage) })
            .addIngredient(
                'm', if (player.hasPermission("yvtils.fusion.manage"))
                    button(player, Material.CRAFTING_TABLE, if (manage) "browser" else "manage") {
                        browser(
                            it,
                            manage = !manage
                        )
                    }
                else Filler.item())
            .addIngredient('n', if (manage) button(player, Material.WRITABLE_BOOK, "new") { viewer ->
                input(viewer, "new", "new-recipe", true, { browser(it, manage = true) }) { actor, id ->
                    if (id.matches(Regex("[a-z0-9][a-z0-9_-]{0,63}")) && FusionRegistry.recipe(id) == null)
                        RecipeEditor.open(actor, FusionRecipe(id = id), null)
                    else {
                        FusionText.send(actor, "error", mapOf("error" to "Invalid or existing ID")); browser(
                            actor,
                            manage = true
                        )
                    }
                }
            } else Filler.item()).setContent(items).build()
        show(player, if (manage) "manage" else "browser", gui)
    }

    fun details(player: Player, recipe: FusionRecipe, selection: OutputSelection = OutputSelection()) {
        if (!allowed(player)) return
        if (FusionRegistry.recipe(recipe.id) != recipe) {
            FusionText.send(player, "stale"); browser(player); return
        }
        val type = FusionRegistry.outputType(recipe.outputKind)
        val output = FusionItems.output(recipe, selection, preview = true)
        val preview = Item.builder().setItemProvider {
            val stack = output.clone()
            stack.editMeta { meta ->
                meta.lore(
                    meta.lore().orEmpty() + listOf(
                        GuiStyle.lore(FusionText.text(player, "outputQuantity", mapOf("amount" to recipe.outputAmount)))
                    )
                )
            }
            ItemWrapper(stack)
        }.build()

        fun craft(viewer: Player, requested: Int) {
            if (!type.ready(selection)) {
                type.configure(viewer, recipe, selection, Consumer { details(viewer, recipe, it) }); return
            }
            try {
                val count = FusionRegistry.craft(viewer, recipe.id, requested, selection)
                if (count > 0) FusionText.send(
                    viewer,
                    "done",
                    mapOf("amount" to count * recipe.outputAmount, "recipe" to recipe.name)
                )
            } catch (error: Exception) {
                FusionText.send(
                    viewer,
                    if (error.message in listOf(
                            "missing",
                            "full",
                            "xp",
                            "denied",
                            "stale"
                        )
                    ) error.message!! else "error",
                    mapOf("error" to (error.message ?: "Unknown"))
                )
            }
            details(viewer, recipe, selection)
        }

        val option = type.control(player, recipe, selection)?.let { stack ->
            Item.builder()
                .setItemProvider { ItemWrapper(stack.clone()) }.addClickHandler { _, event ->
                    val viewer = event.player()
                    if (allowed(viewer)) type.configureClick(
                        viewer,
                        recipe,
                        selection,
                        event.clickType(),
                        Consumer { details(viewer, recipe, it) })
                }.build()
        } ?: Filler.item()
        val ingredients = recipe.ingredients.mapIndexed { index, ingredient ->
            Item.builder().setItemProvider {
                val missing = allocation(player, recipe).missing[index]
                val icon = ingredientIcon(ingredient)
                icon.amount = ingredient.amount.coerceIn(1, icon.maxStackSize)
                FusionStyle.item(
                    icon, Component.text(ingredientName(ingredient)), listOf(
                        FusionStyle.status(
                            FusionText.text(player, if (missing == 0) "owned" else "missingIngredients"),
                            missing == 0
                        ),
                        GuiStyle.lore(
                            FusionText.text(
                                player,
                                "haveNeed",
                                mapOf("have" to ingredient.amount - missing, "need" to ingredient.amount)
                            )
                        ),
                        FusionStyle.status(
                            FusionText.text(player, "missingCount", mapOf("amount" to missing)),
                            missing == 0
                        ),
                        GuiStyle.lore(FusionText.text(player, "allocationHint"))
                    ), if (missing == 0) Colors.GREEN else Colors.RED
                )
            }.build()
        }
        val summary = Item.builder().setItemProvider {
            val ready = type.ready(selection)
            val fits = ready && Crafting.plan(player.inventory.storageContents, recipe, output, 1).room
            FusionStyle.item(
                if (allocation(
                        player,
                        recipe
                    ).complete && player.level >= recipe.xpLevels && fits
                ) Material.LIME_DYE else Material.RED_DYE,
                FusionText.text(player, "requirements"), lore(player, recipe) + listOf(
                    FusionStyle.status(
                        FusionText.text(player, if (ready) "selectionReady" else "selectionMissing"),
                        ready
                    ),
                    FusionStyle.status(FusionText.text(player, if (fits) "roomReady" else "roomMissing"), fits)
                )
            )
        }.build()

        fun craftButton(material: Material, key: String, requested: Int): Item = Item.builder().setItemProvider {
            val selected = type.ready(selection)
            val count = if (requested == 0 && selected) Crafting.maximum(player, recipe, output) else requested
            val ready = selected && count > 0 && recipe.enabled && player.hasPermission("yvtils.fusion.use") &&
                    (recipe.permission.isBlank() || player.hasPermission(recipe.permission)) &&
                    player.level >= recipe.xpLevels * count && Crafting.plan(
                player.inventory.storageContents,
                recipe,
                output,
                count
            ).possible
            FusionStyle.item(
                material, FusionText.text(player, key), listOf(
                    FusionStyle.status(FusionText.text(player, if (ready) "craftReady" else "craftBlocked"), ready),
                    GuiStyle.lore(
                        FusionText.text(
                            player,
                            "craftAmount",
                            mapOf("amount" to count * recipe.outputAmount)
                        )
                    ),
                    GuiStyle.lore(FusionText.text(player, "cost", mapOf("amount" to recipe.xpLevels * count)))
                ),
                if (ready) Colors.GREEN else Colors.RED
            )
        }.addClickHandler { _, event -> if (allowed(event.player())) craft(event.player(), requested) }.build()

        val gui = PagedGui.itemsBuilder().setStructure(
            "b # # q i v # # r", "# x x x x x x x #", "# x x x x x x x #",
            "# x x x x x x x #", "# # # < # > # # #", "# # o # s # a # #"
        )
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL).setContent(ingredients)
            .addIngredient('q', summary).addIngredient('<', page(player, false)).addIngredient('>', page(player, true))
            .addIngredient('#', Filler.item()).addIngredient('i', preview).addIngredient('v', option)
            .addIngredient('b', button(player, Material.ARROW, "back") { browser(it) })
            .addIngredient('r', button(player, Material.SUNFLOWER, "refresh") { details(it, recipe, selection) })
            .addIngredient('o', craftButton(Material.LIME_DYE, "one", 1))
            .addIngredient(
                's',
                craftButton(Material.EMERALD, "stack", maxOf(1, output.maxStackSize / output.amount))
            )
            .addIngredient('a', craftButton(Material.DIAMOND, "maximum", 0)).build()
        show(player, "browser", gui)
    }

    internal fun ingredientIcon(ingredient: Ingredient): ItemStack = when {
        ingredient.exactItem.isNotBlank() -> FusionItems.decode(ingredient.exactItem)
        ingredient.fusionId.isNotBlank() -> ItemStack(
            FusionRegistry.recipe(ingredient.fusionId)?.thumbnail
                ?.let(Material::valueOf) ?: Material.NETHER_STAR
        )

        ingredient.materials.isNotEmpty() -> ItemStack(
            Material.matchMaterial(ingredient.materials.first()) ?: Material.BARRIER
        )

        ingredient.tag.isNotBlank() -> ItemStack(BukkitTags.block(ingredient.tag)?.values?.sortedBy { it.name }
            ?.firstOrNull { it.isItem } ?: Material.BARRIER)

        else -> ItemStack(Material.BARRIER)
    }

    internal fun selectHead(
        player: Player,
        recipe: FusionRecipe,
        selection: OutputSelection,
        selected: Consumer<OutputSelection>
    ) {
        if (!allowed(player)) return
        input(player, "head", player.name, cancel = { details(it, recipe, selection) }) { viewer, value ->
            val uuid = runCatching { UUID.fromString(value) }.getOrNull()
            if (uuid == null && !value.matches(Regex("[A-Za-z0-9_]{3,16}"))) {
                FusionText.send(viewer, "badHead"); details(viewer, recipe, selection); return@input
            }
            viewer.closeInventory()
            FusionText.send(viewer, "resolving")
            val profile = if (uuid != null) Bukkit.createPlayerProfile(uuid) else Bukkit.createPlayerProfile(value)
            profile.update().orTimeout(15, TimeUnit.SECONDS).whenComplete { resolved, error ->
                if (!Core.instance.isEnabled || !FusionYVtils.active) return@whenComplete
                Bukkit.getScheduler().runTask(Core.instance, Runnable {
                    if (!viewer.isOnline || !allowed(viewer)) return@Runnable
                    if (error != null || resolved?.textures?.isEmpty != false) {
                        FusionText.send(viewer, "badHead"); details(viewer, recipe, selection)
                    } else selected.accept(selection.copy(profile = resolved))
                })
            }
        }
    }
}
