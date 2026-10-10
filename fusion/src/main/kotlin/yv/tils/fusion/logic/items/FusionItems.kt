/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.logic.items

import yv.tils.fusion.FusionRecipe

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.profile.PlayerProfile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import java.util.Base64
import yv.tils.fusion.api.OutputSelection
import yv.tils.fusion.api.FusionRegistry

object FusionItems {
    val idKey = NamespacedKey("yvtils", "fusion_id")
    val kindKey = NamespacedKey("yvtils", "fusion_kind")
    val entityKey = NamespacedKey("yvtils", "fusion_frame_item")

    fun id(stack: ItemStack) = stack.itemMeta?.persistentDataContainer?.get(idKey, PersistentDataType.STRING)
    fun kind(stack: ItemStack) = stack.itemMeta?.persistentDataContainer?.get(kindKey, PersistentDataType.STRING)
    fun encode(stack: ItemStack): String = Base64.getEncoder().encodeToString(stack.serializeAsBytes())
    fun decode(value: String): ItemStack = ItemStack.deserializeBytes(Base64.getDecoder().decode(value))

    fun output(recipe: FusionRecipe, light: Int = 15, profile: PlayerProfile? = null): ItemStack =
        output(recipe, OutputSelection(light = light, profile = profile))

    fun output(recipe: FusionRecipe, selection: OutputSelection, preview: Boolean = false): ItemStack {
        val item =
            if (recipe.exactOutput.isBlank()) ItemStack(Material.valueOf(recipe.output)) else decode(recipe.exactOutput)
        item.amount = recipe.outputAmount
        item.editMeta {
            it.persistentDataContainer.set(idKey, PersistentDataType.STRING, recipe.id)
            it.persistentDataContainer.set(kindKey, PersistentDataType.STRING, recipe.outputKind)
            it.displayName(Component.text(recipe.name).decoration(TextDecoration.ITALIC, false))
        }
        val type = FusionRegistry.outputType(recipe.outputKind)
        val result = if (preview) type.preview(recipe, item, selection) else {
            check(type.ready(selection)) { "Output selection is incomplete" }
            type.create(recipe, item, selection)
        }
        require(!result.type.isAir && result.amount == recipe.outputAmount && result.amount <= result.maxStackSize) {
            "Output handler returned an invalid stack"
        }
        // Extension handlers cannot accidentally lose stable item identity.
        result.editMeta {
            it.persistentDataContainer.set(idKey, PersistentDataType.STRING, recipe.id)
            it.persistentDataContainer.set(kindKey, PersistentDataType.STRING, recipe.outputKind)
        }
        return result
    }
}
