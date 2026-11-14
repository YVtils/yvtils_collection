/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.remadeEnderDragon.logic

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.data.BlockData
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityCombustEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ItemDespawnEvent
import org.bukkit.event.world.EntitiesUnloadEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import yv.tils.utils.modules.Core
import java.util.UUID
import kotlin.math.sin

/** The real stack remains a hidden, non-ageing, hovering item for vanilla pickup and metadata. */
class DroppedDragonEggDisplay(
    private val eggBlock: () -> BlockData = { Material.DRAGON_EGG.createBlockData() },
) : Listener {
    private companion object {
        const val VISIBLE = 1
        const val AGEING = 2
        const val GRAVITY = 4
        const val GRAVITY_RECORDED = 8
        const val BOB_PERIOD_TICKS = 80
        const val BOB_HEIGHT = 0.1
    }

    private class DroppedEgg(val item: Item, val original: Byte, var display: BlockDisplay? = null)

    private val eggs = mutableMapOf<UUID, DroppedEgg>()
    private val removedDisplays = mutableListOf<BlockDisplay>()
    private val stateKey = NamespacedKey("yvtils", "dragon_egg_item_state")
    private var task: BukkitTask? = null
    private var animationTick = 0

    fun enable() {
        Bukkit.getWorlds().forEach { world -> world.entities.filterIsInstance<Item>().forEach(::track) }
        task = Bukkit.getScheduler().runTaskTimer(Core.instance, Runnable { sync() }, 1L, 1L)
    }

    /** Protect the backing item now; create its display only from the main-thread ticker. */
    internal fun track(item: Item) {
        if (!DragonEggRules.containsEgg(item.itemStack) || item.uniqueId in eggs) return
        // Retain the baseline across a restart if the server saved a hidden backing item.
        var original = item.persistentDataContainer.get(stateKey, PersistentDataType.BYTE)?.toInt()
            ?: ((if (item.isVisibleByDefault) VISIBLE else 0) or (if (item.willAge()) AGEING else 0))
        // Older saved state only recorded visibility/ageing; capture gravity before overriding it.
        if (original and GRAVITY_RECORDED == 0) {
            original = original or GRAVITY_RECORDED or (if (item.hasGravity()) GRAVITY else 0)
        }
        item.persistentDataContainer.set(stateKey, PersistentDataType.BYTE, original.toByte())
        eggs[item.uniqueId] = DroppedEgg(item, original.toByte())
        item.setWillAge(false)
        item.isVisibleByDefault = false
        extinguish(item)
        hover(item)
    }

    private fun spawnDisplay(item: Item): BlockDisplay =
        item.world.spawn(item.location, BlockDisplay::class.java) { display ->
            display.isPersistent = false
            display.isInvulnerable = true
            display.setGravity(false)
            display.block = eggBlock()
            display.isGlowing = true
            display.glowColorOverride = Color.fromRGB(0xAA00AA)
            display.brightness = Display.Brightness(15, 15)
            display.teleportDuration = 1
            display.transformation = Transformation(
                Vector3f(-0.75f, 0f, -0.75f), Quaternionf(), Vector3f(1.5f), Quaternionf(),
            )
        }

    private fun hover(item: Item) {
        if (item.hasGravity()) item.setGravity(false)
        // Drops can receive their throw velocity after the add-to-world event; sustain the hover.
        if (item.velocity.lengthSquared() > 0.0) item.velocity = Vector()
    }

    private fun extinguish(item: Item) {
        if (item.fireTicks > 0) item.fireTicks = 0
    }

    internal fun sync() {
        removeQueuedDisplays()
        val bob = sin(animationTick * (2.0 * Math.PI / BOB_PERIOD_TICKS)) * BOB_HEIGHT
        eggs.values.toList().forEach { egg ->
            val item = egg.item
            val location = item.location
            if (!item.isValid || !item.isInWorld || !DragonEggRules.containsEgg(item.itemStack) ||
                !item.world.isChunkLoaded(location.blockX shr 4, location.blockZ shr 4)) {
                remove(item.uniqueId)
            } else {
                if (item.willAge()) item.setWillAge(false)
                if (item.isVisibleByDefault) item.isVisibleByDefault = false
                extinguish(item)
                hover(item)
                // Entity lifecycle callbacks can run inside Moonrise's chunk-status update.
                // Spawn/recreate visuals here, after those callbacks have completed.
                val display = egg.display?.takeIf { it.isValid }
                    ?: spawnDisplay(item).also { egg.display = it }
                // Animate only the visual: pickup position and void-recovery checks remain stable.
                val at = location.add(0.0, bob, 0.0)
                if (display.world != at.world || display.location.distanceSquared(at) > 1.0e-8) {
                    display.teleport(at)
                }
            }
        }
        removeQueuedDisplays()
        animationTick = (animationTick + 1) % BOB_PERIOD_TICKS
    }

    private fun remove(id: UUID) {
        val egg = eggs.remove(id) ?: return
        egg.display?.let { removedDisplays += it }
        egg.item.isVisibleByDefault = egg.original.toInt() and VISIBLE != 0
        egg.item.setWillAge(egg.original.toInt() and AGEING != 0)
        egg.item.setGravity(egg.original.toInt() and GRAVITY != 0)
        egg.item.persistentDataContainer.remove(stateKey)
    }

    private fun removeQueuedDisplays() {
        val pending = removedDisplays.toList()
        removedDisplays.clear()
        pending.forEach { if (it.isValid) it.remove() }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun added(event: EntityAddToWorldEvent) { (event.entity as? Item)?.let(::track) }

    @EventHandler(priority = EventPriority.MONITOR)
    fun removed(event: EntityRemoveFromWorldEvent) { remove(event.entity.uniqueId) }

    @EventHandler(priority = EventPriority.MONITOR)
    fun unloaded(event: EntitiesUnloadEvent) {
        event.entities.forEach { remove(it.uniqueId) }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun despawn(event: ItemDespawnEvent) {
        if (DragonEggRules.containsEgg(event.entity.itemStack)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun combust(event: EntityCombustEvent) {
        val item = event.entity as? Item ?: return
        if (!DragonEggRules.containsEgg(item.itemStack)) return
        event.isCancelled = true
        extinguish(item)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun damage(event: EntityDamageEvent) {
        val item = event.entity as? Item ?: return
        if (!DragonEggRules.containsEgg(item.itemStack)) return
        // Protect the real pickup stack, not just its visual. Keep explosions and the
        // separately configurable void rule unchanged; do not make the item invulnerable.
        when (event.cause) {
            EntityDamageEvent.DamageCause.LAVA, EntityDamageEvent.DamageCause.FIRE,
            EntityDamageEvent.DamageCause.FIRE_TICK, EntityDamageEvent.DamageCause.CONTACT,
            EntityDamageEvent.DamageCause.HOT_FLOOR -> {
                event.isCancelled = true
                extinguish(item)
            }
            else -> Unit
        }
    }

    fun close() {
        task?.cancel()
        task = null
        eggs.keys.toList().forEach(::remove)
        removeQueuedDisplays()
    }
}
