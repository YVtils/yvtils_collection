/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.api

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.profile.PlayerProfile
import yv.tils.fusion.*
import java.util.function.Consumer

/** All methods and callbacks run on the server thread unless explicitly documented otherwise. */
interface FusionApi {
    fun recipes(): List<FusionRecipe>
    fun recipe(id: String): FusionRecipe?

    /** Runtime-only recipe; JSON remains owned by administrators. Duplicate IDs are rejected. */
    fun registerRecipe(recipe: FusionRecipe): AutoCloseable
    fun registerOutputType(id: String, type: FusionOutputType): AutoCloseable
    fun registerBehavior(kind: String, behavior: FusionItemBehavior): AutoCloseable
    fun registerCraftGuard(guard: FusionCraftGuard): AutoCloseable
    fun registerCraftObserver(observer: FusionCraftObserver): AutoCloseable
    fun itemId(item: ItemStack): String?
    fun itemKind(item: ItemStack): String?
    fun createOutput(recipe: FusionRecipe, selection: OutputSelection = OutputSelection()): ItemStack

    /** requested=0 crafts the maximum (up to 64). Generates its own output; never trusts a supplied item. */
    fun craft(player: Player, recipeId: String, requested: Int = 1, selection: OutputSelection = OutputSelection()): Int
}

/** Additional output-specific choices; use values for your extension's custom choices. */
data class OutputSelection(
    val light: Int = 15,
    val profile: PlayerProfile? = null,
    val values: Map<String, String> = emptyMap(),
)

interface FusionOutputType {
    /** Reject invalid materials/settings. Must not mutate game state. */
    fun validate(recipe: FusionRecipe)

    /** The fresh base already includes recipe name, quantity and stable identity. */
    fun create(recipe: FusionRecipe, base: ItemStack, selection: OutputSelection): ItemStack
    fun ready(selection: OutputSelection): Boolean = true
    fun preview(recipe: FusionRecipe, base: ItemStack, selection: OutputSelection): ItemStack =
        if (ready(selection)) create(recipe, base, selection) else base

    /** Optional details-screen control. Return null for no control. */
    fun control(player: Player, recipe: FusionRecipe, selection: OutputSelection): ItemStack? = null

    /** Click-aware control; existing addons keep their original configure callback. */
    fun configureClick(
        player: Player, recipe: FusionRecipe, selection: OutputSelection,
        click: org.bukkit.event.inventory.ClickType, selected: Consumer<OutputSelection>
    ) =
        configure(player, recipe, selection, selected)

    /** Open your input UI or cycle a value, then call selected on the server thread. */
    fun configure(
        player: Player,
        recipe: FusionRecipe,
        selection: OutputSelection,
        selected: Consumer<OutputSelection>
    ) {
    }
}

fun interface FusionItemBehavior {
    /** Called for the actual hand item. Right-click-air may be pre-cancelled by vanilla;
     * respect block/item use results and third-party cancellation before side effects. */
    fun interact(event: PlayerInteractEvent, item: ItemStack)
}

data class FusionCraftContext(val player: Player, val recipe: FusionRecipe, val crafts: Int, val output: ItemStack)

fun interface FusionCraftGuard {
    /** Return null to allow; otherwise return a rejection explanation. No inventory/XP mutation. */
    fun reject(context: FusionCraftContext): String?
}

fun interface FusionCraftObserver {
    /** After successful commit; exceptions are logged, never interpreted as transaction failure. */
    fun crafted(context: FusionCraftContext)
}
