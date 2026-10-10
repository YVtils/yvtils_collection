/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.logic.items

import yv.tils.fusion.FusionRecipe
import yv.tils.fusion.gui.FusionGui
import yv.tils.fusion.language.FusionText
import yv.tils.fusion.logic.items.flask.NourishingFlask

import org.bukkit.Material
import org.bukkit.block.data.type.Light
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockDataMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.entity.Player
import yv.tils.fusion.api.*
import yv.tils.gui.utils.GuiStyle
import java.util.function.Consumer

object BuiltinOutputTypes {
    fun register() {
        FusionRegistry.registerOutputType("NORMAL", object : FusionOutputType {
            override fun validate(recipe: FusionRecipe) {}
            override fun create(recipe: FusionRecipe, base: ItemStack, selection: OutputSelection) = base.apply {
                if (recipe.exactOutput.isBlank()) OutputLore.apply(this, recipe)
            }
        })
        FusionRegistry.registerOutputType("INVISIBLE_FRAME", object : FusionOutputType {
            override fun validate(recipe: FusionRecipe) {
                require(
                    recipe.output in listOf(
                        "ITEM_FRAME",
                        "GLOW_ITEM_FRAME"
                    )
                ) { "Invisible frame requires a frame material" }
            }

            override fun create(recipe: FusionRecipe, base: ItemStack, selection: OutputSelection) = base.apply {
                OutputLore.apply(this, recipe, listOf("Place to display an item without a visible frame."),
                    listOf("Frame" to "Invisible"))
            }
        })
        FusionRegistry.registerOutputType("LIGHT", object : FusionOutputType {
            override fun validate(recipe: FusionRecipe) {
                require(recipe.output == "LIGHT")
            }

            override fun create(recipe: FusionRecipe, base: ItemStack, selection: OutputSelection): ItemStack =
                base.apply {
                    val meta = itemMeta as BlockDataMeta
                    val data = Material.LIGHT.createBlockData() as Light
                    data.level = selection.light.coerceIn(0, 15)
                    meta.setBlockData(data)
                    itemMeta = meta
                    OutputLore.apply(this, recipe, listOf("Place to illuminate your build."),
                        listOf("Light level" to "${data.level} / 15"))
                }

            override fun control(player: Player, recipe: FusionRecipe, selection: OutputSelection) =
                GuiStyle.field(
                    Material.LIGHT,
                    FusionText.text(player, "light", mapOf("value" to selection.light)),
                    listOf(
                        yv.tils.configv2.language.LanguageHandler.getMessage(
                            "action.gui.lore.controls.number",
                            player
                        )
                    )
                ).get()

            override fun configure(
                player: Player,
                recipe: FusionRecipe,
                selection: OutputSelection,
                selected: Consumer<OutputSelection>
            ) {
                selected.accept(selection.copy(light = (selection.light + 1) % 16))
            }

            override fun configureClick(
                player: Player, recipe: FusionRecipe, selection: OutputSelection,
                click: org.bukkit.event.inventory.ClickType, selected: Consumer<OutputSelection>
            ) {
                val delta = yv.tils.fusion.gui.ValueControls.delta(click) ?: return
                selected.accept(selection.copy(light = (selection.light + delta).coerceIn(0, 15)))
            }
        })
        FusionRegistry.registerOutputType("PLAYER_HEAD", object : FusionOutputType {
            override fun validate(recipe: FusionRecipe) {
                require(recipe.output == "PLAYER_HEAD")
            }

            override fun ready(selection: OutputSelection) = selection.profile != null
            override fun create(recipe: FusionRecipe, base: ItemStack, selection: OutputSelection): ItemStack =
                base.apply {
                    val meta = itemMeta as SkullMeta
                    meta.ownerProfile = selection.profile ?: error("Select a player first")
                    itemMeta = meta
                    OutputLore.apply(this, recipe, stats = listOf("Player" to (selection.profile?.name ?: "Selected player")))
                }

            override fun control(player: Player, recipe: FusionRecipe, selection: OutputSelection) =
                GuiStyle.field(Material.PLAYER_HEAD, FusionText.text(player, "head"), emptyList()).get()

            override fun configure(
                player: Player,
                recipe: FusionRecipe,
                selection: OutputSelection,
                selected: Consumer<OutputSelection>
            ) {
                FusionGui.selectHead(player, recipe, selection, selected)
            }
        })
        FusionRegistry.registerOutputType("NOURISHING_FLASK", NourishingFlask)
        FusionRegistry.registerBehavior("NOURISHING_FLASK", NourishingFlask)
    }
}
