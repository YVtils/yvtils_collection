/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.gui

import yv.tils.fusion.FusionRecipe
import yv.tils.fusion.Ingredient
import yv.tils.fusion.configs.RecipeStore
import yv.tils.fusion.language.FusionText
import yv.tils.fusion.logic.items.FusionItems

import org.bukkit.Material
import org.bukkit.entity.Player
import net.kyori.adventure.text.Component
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemWrapper
import yv.tils.gui.utils.GuiStyle
import xyz.xenondevs.invui.gui.Gui
import yv.tils.gui.utils.Filler
import yv.tils.utils.logger.Logger

object RecipeEditor {
    fun open(player: Player, draft: FusionRecipe, original: FusionRecipe?) {
        if (!FusionGui.allowed(player, true)) return
        val gui = Gui.builder().setStructure(
            "# # # # v # # # #",
            "# n d c t # e p #",
            "# # # # # # # # #",
            "# o a k f # i j #",
            "# h # x # # l # #",
            "b # # # s # # # #"
        )
            .addIngredient('#', Filler.item())

        fun field(symbol: Char, material: Material, key: String, value: String, update: (String) -> FusionRecipe) {
            gui.addIngredient(
                symbol,
                Item.builder().setItemProvider {
                    FusionStyle.item(
                        material, FusionText.text(player, key), listOf(
                            GuiStyle.lore(FusionText.text(player, "currentValue", mapOf("value" to value))),
                            GuiStyle.lore(FusionText.text(player, "fieldHint")),
                            GuiStyle.lore(FusionText.text(player, "draftHint"))
                        )
                    )
                }.addClickHandler { _, event ->
                    val viewer = event.player()
                    if (FusionGui.allowed(viewer, true)) FusionGui.input(
                        viewer, "editor", value, true,
                        { open(it, draft, original) }) { actor, input ->
                        try {
                            open(actor, update(input), original)
                        } catch (error: Exception) {
                            FusionText.send(actor, "error", mapOf("error" to (error.message ?: "Invalid input"))); open(
                                actor,
                                draft,
                                original
                            )
                        }
                    }
                }.build()
            )
        }
        field('n', Material.NAME_TAG, "editName", draft.name) { draft.copy(name = it) }
        field('d', Material.WRITABLE_BOOK, "editDescription", draft.description) { draft.copy(description = it) }
        field('c', Material.BOOKSHELF, "editCategory", draft.category) { draft.copy(category = it) }
        gui.addIngredient('t', Item.builder().setItemProvider {
            FusionStyle.item(
                if (draft.exactThumbnail.isBlank()) org.bukkit.inventory.ItemStack(Material.valueOf(draft.thumbnail))
                else FusionItems.decode(draft.exactThumbnail), FusionText.text(player, "editThumbnail"),
                listOf(GuiStyle.lore(FusionText.text(player, "pickHint")))
            )
        }.addClickHandler { _, event ->
            val viewer = event.player()
            ItemPicker.open(viewer, { open(it, draft, original) }) { actor, item ->
                open(
                    actor, draft.copy(
                        thumbnail = item.type.name,
                        exactThumbnail = FusionItems.encode(item.apply { amount = 1 })
                    ), original
                )
            }
        }.build())
        gui.addIngredient('e', Item.builder().setItemProvider {
            FusionStyle.item(
                if (draft.enabled) Material.LIME_DYE else Material.RED_DYE,
                FusionText.text(player, "editEnabled"), listOf(
                    FusionStyle.status(
                        FusionText.text(
                            player,
                            if (draft.enabled) "enabled" else "disabled"
                        ), draft.enabled
                    ),
                    GuiStyle.lore(FusionText.text(player, "toggleHint"))
                )
            )
        }.addClickHandler { _, event ->
            if (FusionGui.allowed(event.player(), true))
                open(event.player(), draft.copy(enabled = !draft.enabled), original)
        }.build())
        field('p', Material.TRIPWIRE_HOOK, "editPermission", draft.permission) { draft.copy(permission = it) }
        gui.addIngredient(
            'x', ValueControls.number(
                player,
                Material.EXPERIENCE_BOTTLE,
                "editXp",
                draft.xpLevels,
                0..10000,
                { open(it, draft, original) },
                { viewer, value -> open(viewer, draft.copy(xpLevels = value), original) })
        )
        gui.addIngredient(
            'a', ValueControls.number(
                player,
                Material.BUNDLE,
                "editAmount",
                draft.outputAmount,
                1..64,
                { open(it, draft, original) },
                { viewer, value -> open(viewer, draft.copy(outputAmount = value), original) })
        )
        gui.addIngredient('o', Item.builder().setItemProvider {
            FusionStyle.item(
                if (draft.exactOutput.isBlank()) org.bukkit.inventory.ItemStack(Material.valueOf(draft.output))
                else FusionItems.decode(draft.exactOutput), FusionText.text(player, "editOutput"),
                listOf(GuiStyle.lore(FusionText.text(player, "pickHint")))
            )
        }.addClickHandler { _, event ->
            val viewer = event.player()
            ItemPicker.open(viewer, { open(it, draft, original) }) { actor, item ->
                open(
                    actor, draft.copy(
                        output = item.type.name, outputAmount = item.amount,
                        exactOutput = FusionItems.encode(item)
                    ), original
                )
            }
        }.build())
        field('k', Material.BLAZE_POWDER, "editKind", draft.outputKind) { draft.copy(outputKind = it) }
        gui.addIngredient(
            'f',
            FusionGui.button(player, Material.COMPARATOR, "editSettings") { settings(it, draft, original) })
        gui.addIngredient(
            'i',
            FusionGui.button(player, Material.CRAFTING_TABLE, "editIngredients") { ingredients(it, draft, original) })
        gui.addIngredient('h', FusionGui.button(player, Material.ENDER_CHEST, "capture") {
            if (FusionGui.allowed(it, true)) {
                val held = it.inventory.itemInMainHand
                if (!held.type.isAir) open(
                    it, draft.copy(
                        output = held.type.name, outputKind = "NORMAL",
                        outputAmount = held.amount, exactOutput = FusionItems.encode(held)
                    ), original
                )
            }
        })
        gui.addIngredient('j', FusionGui.button(player, Material.HOPPER, "add") {
            if (draft.ingredients.size < 36) ItemPicker.open(it, { open(it, draft, original) }) { viewer, item ->
                ingredientChoice(viewer, item, { open(it, draft, original) }) { actor, ingredient ->
                    open(actor, draft.copy(ingredients = draft.ingredients + ingredient), original)
                }
            }
        })
        gui.addIngredient('l', FusionGui.button(player, Material.SHEARS, "remove") {
            if (FusionGui.allowed(it, true)) open(it, draft.copy(ingredients = draft.ingredients.dropLast(1)), original)
        })
        gui.addIngredient(
            'b',
            FusionGui.button(player, Material.BARRIER, "cancel") { FusionGui.browser(it, manage = true) })
        gui.addIngredient('s', FusionGui.button(player, Material.EMERALD_BLOCK, "save") {
            if (FusionGui.allowed(it, true)) {
                try {
                    RecipeStore.save(draft, original); Logger.info("Fusion recipe ${draft.id} saved by ${it.name}")
                    FusionText.send(it, "saved"); FusionGui.browser(it, manage = true)
                } catch (error: Exception) {
                    FusionText.send(it, "error", mapOf("error" to (error.message ?: "Invalid recipe"))); open(
                        it,
                        draft,
                        original
                    )
                }
            }
        })
        gui.addIngredient('v', Item.builder().setItemProvider {
            val output = runCatching {
                FusionItems.output(draft, yv.tils.fusion.api.OutputSelection(), preview = true)
            }.getOrElse { org.bukkit.inventory.ItemStack(Material.BARRIER) }
            output.editMeta { meta ->
                meta.displayName(GuiStyle.title(Component.text(draft.name)))
                meta.lore((listOf(Component.text(draft.description)) + draft.ingredients.map {
                    Component.text(
                        "${it.amount} × " + if (it.exactItem.isNotBlank()) "${FusionItems.decode(it.exactItem).type} (exact)"
                        else if (it.fusionId.isNotBlank()) "@${it.fusionId}" else if (it.tag.isNotBlank()) "#${it.tag}"
                        else it.materials.joinToString(" / ")
                    )
                }).map(GuiStyle::lore))
            }
            ItemWrapper(output)
        }.build())
        FusionGui.show(player, "editor", gui.build())
    }

    private fun ingredients(player: Player, draft: FusionRecipe, original: FusionRecipe?) {
        if (!FusionGui.allowed(player, true)) return
        val items = draft.ingredients.mapIndexed { index, ingredient ->
            ValueControls.number(
                player,
                FusionGui.ingredientIcon(ingredient).type,
                "settingValue",
                ingredient.amount,
                1..4096,
                { ingredients(it, draft, original) },
                { viewer, amount ->
                    ingredients(viewer, draft.copy(ingredients = draft.ingredients.mapIndexed { i, value ->
                        if (i == index) value.copy(amount = amount) else value
                    }), original)
                },
                mapOf("setting" to FusionGui.ingredientName(ingredient))
            )
        }
        val gui = xyz.xenondevs.invui.gui.PagedGui.itemsBuilder()
            .setStructure("# x x x x x x x #", "# x x x x x x x #", "b # # < a > # # #")
            .addIngredient('#', Filler.item())
            .addIngredient('x', xyz.xenondevs.invui.gui.Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('<', FusionGui.page(player, false, true))
            .addIngredient('>', FusionGui.page(player, true, true))
            .addIngredient('b', FusionGui.button(player, Material.ARROW, "back") { open(it, draft, original) })
            .addIngredient('a', FusionGui.button(player, Material.HOPPER, "add") { viewer ->
                if (draft.ingredients.size < 36) ItemPicker.open(
                    viewer,
                    { ingredients(it, draft, original) }) { actor, item ->
                    ingredientChoice(actor, item, { ingredients(it, draft, original) }) { owner, ingredient ->
                        ingredients(owner, draft.copy(ingredients = draft.ingredients + ingredient), original)
                    }
                }
            }).setContent(items).build()
        FusionGui.show(player, "editIngredients", gui)
    }

    private fun ingredientChoice(
        player: Player, item: org.bukkit.inventory.ItemStack, back: (Player) -> Unit,
        select: (Player, Ingredient) -> Unit
    ) {
        val gui = Gui.builder().setStructure("# # # # # # # # #", "# m # i # e # # #", "b # # # # # # # #")
            .addIngredient('#', Filler.item())
            .addIngredient('b', FusionGui.button(player, Material.ARROW, "back", click = back))
            .addIngredient('i', Item.simple(xyz.xenondevs.invui.item.ItemWrapper(item.clone())))
            .addIngredient('m', FusionGui.button(player, Material.CRAFTING_TABLE, "matchMaterial") {
                if (FusionGui.allowed(it, true)) select(
                    it,
                    Ingredient(amount = item.amount, materials = listOf(item.type.name))
                )
            })
            .addIngredient('e', FusionGui.button(player, Material.ENCHANTED_BOOK, "matchExact") {
                if (FusionGui.allowed(it, true)) select(
                    it, Ingredient(
                        amount = item.amount,
                        exactItem = FusionItems.encode(item.clone().apply { amount = 1 })
                    )
                )
            }).build()
        FusionGui.show(player, "editIngredients", gui)
    }

    private fun settings(player: Player, draft: FusionRecipe, original: FusionRecipe?) {
        if (!FusionGui.allowed(player, true)) return
        val items = draft.settings.map { (key, value) ->
            val number = value.toIntOrNull()
            val range = when (key) {
                "xpPerFoodPoint" -> 1..10000
                "foodPerUse" -> 1..20
                "saturationPerUse" -> 0..20
                "xpPerSaturationPoint" -> 1..10000
                "cooldownTicks" -> 1..1200
                else -> Int.MIN_VALUE..Int.MAX_VALUE
            }
            if (number != null) ValueControls.number(
                player, Material.COMPARATOR, "settingValue", number, range,
                { settings(it, draft, original) }, { viewer, updated ->
                    settings(viewer, draft.copy(settings = draft.settings + (key to updated.toString())), original)
                }, mapOf("setting" to key)
            )
            else FusionGui.button(player, Material.NAME_TAG, "settingValue", mapOf("setting" to key)) { viewer ->
                FusionGui.input(
                    viewer,
                    "editSettings",
                    value,
                    true,
                    { settings(it, draft, original) }) { actor, input ->
                    settings(actor, draft.copy(settings = draft.settings + (key to input)), original)
                }
            }
        }
        val gui = xyz.xenondevs.invui.gui.PagedGui.itemsBuilder()
            .setStructure("# # # # # # # # #", "# x x x x x x x #", "b # d < a > # # #")
            .addIngredient('#', Filler.item())
            .addIngredient('x', xyz.xenondevs.invui.gui.Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('<', FusionGui.page(player, false, true))
            .addIngredient('>', FusionGui.page(player, true, true))
            .addIngredient('b', FusionGui.button(player, Material.ARROW, "back") { open(it, draft, original) })
            .addIngredient(
                'd',
                FusionGui.button(player, Material.SHEARS, "removeSetting") { removeSetting(it, draft, original) })
            .addIngredient('a', FusionGui.button(player, Material.WRITABLE_BOOK, "addSetting") { viewer ->
                FusionGui.input(
                    viewer, "settingKey", "", true, { settings(it, draft, original) }) { actor, key ->
                    if (key.isBlank() || key.length > 64 || key in draft.settings || draft.settings.size >= 64) {
                        FusionText.send(actor, "error", mapOf("error" to "Invalid or existing setting key"))
                        settings(actor, draft, original)
                    } else {
                        FusionGui.input(
                            actor,
                            "settingInput",
                            "",
                            true,
                            { settings(it, draft, original) }) { owner, value ->
                            if (value.length <= 1024) settings(
                                owner,
                                draft.copy(settings = draft.settings + (key to value)),
                                original
                            )
                            else {
                                FusionText.send(owner, "error", mapOf("error" to "Value too long")); settings(
                                    owner,
                                    draft,
                                    original
                                )
                            }
                        }
                    }
                }
            }).setContent(items).build()
        FusionGui.show(player, "editSettings", gui)
    }

    private fun removeSetting(player: Player, draft: FusionRecipe, original: FusionRecipe?) {
        if (!FusionGui.allowed(player, true)) return
        val items = draft.settings.map { (key, value) ->
            FusionGui.button(player, Material.SHEARS, "settingValue", mapOf("setting" to "$key: $value")) {
                if (FusionGui.allowed(it, true)) settings(it, draft.copy(settings = draft.settings - key), original)
            }
        }
        val gui = xyz.xenondevs.invui.gui.PagedGui.itemsBuilder()
            .setStructure("# x x x x x x x #", "b # # < # > # # #")
            .addIngredient('#', Filler.item())
            .addIngredient('x', xyz.xenondevs.invui.gui.Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('<', FusionGui.page(player, false, true))
            .addIngredient('>', FusionGui.page(player, true, true))
            .addIngredient('b', FusionGui.button(player, Material.ARROW, "back") { settings(it, draft, original) })
            .setContent(items).build()
        FusionGui.show(player, "removeSetting", gui)
    }
}
