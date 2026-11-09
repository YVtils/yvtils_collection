/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.remadeEnderDragon.logic

import org.bukkit.GameRules
import org.bukkit.World
import yv.tils.config.files.ObjectMapperFileUtils
import java.util.UUID

private const val KEEP_INVENTORY_JOURNAL = "/remade-ender-dragon/keep-inventory-journal.json"

data class KeepInventoryJournal(var originalRules: Map<String, Boolean> = emptyMap())

/** Journal before changing a world rule, so an interrupted encounter can be recovered. */
internal class TemporaryKeepInventory(
    initial: KeepInventoryJournal = ObjectMapperFileUtils.load(KEEP_INVENTORY_JOURNAL, KeepInventoryJournal()),
    private val persist: (KeepInventoryJournal) -> Unit = { ObjectMapperFileUtils.save(KEEP_INVENTORY_JOURNAL, it) },
    private val readRule: (World) -> Boolean = { checkNotNull(it.getGameRuleValue(GameRules.KEEP_INVENTORY)) },
    private val writeRule: (World, Boolean) -> Boolean = { world, value -> world.setGameRule(GameRules.KEEP_INVENTORY, value) },
    private val onActivated: (World) -> Unit = {},
    private val onRestored: (World, Boolean) -> Unit = { _, _ -> },
) {
    private var journal = initial.copy(originalRules = initial.originalRules.toMap())
    private val active = mutableSetOf<UUID>()

    fun begin(world: World, enabled: Boolean, preview: Boolean): Boolean {
        if (!enabled || preview || world.environment != World.Environment.THE_END || world.uid in active) return true
        if (!recover(world)) return false
        val original = readRule(world)
        save(journal.copy(originalRules = journal.originalRules + (world.uid.toString() to original)))
        if (original || (writeRule(world, true) && readRule(world))) {
            active += world.uid
            onActivated(world)
            return true
        }
        restore(world)
        return false
    }

    fun restore(world: World): Boolean {
        val wasActive = active.remove(world.uid)
        val original = journal.originalRules[world.uid.toString()] ?: return true
        val changed = readRule(world) != original
        if (changed && (!writeRule(world, original) || readRule(world) != original)) return false
        save(journal.copy(originalRules = journal.originalRules - world.uid.toString()))
        if (world.environment == World.Environment.THE_END && (wasActive || changed)) onRestored(world, original)
        return true
    }

    /** Loaded worlds recover before discovery; late-loaded worlds recover on WorldLoadEvent. */
    fun recover(world: World): Boolean = world.uid in active || restore(world)

    fun isActive(world: World): Boolean =
        world.environment == World.Environment.THE_END && world.uid in active && readRule(world)

    private fun save(next: KeepInventoryJournal) {
        persist(next)
        journal = next
    }
}
