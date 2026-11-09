/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.remadeEnderDragon.logic

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import org.bukkit.Bukkit
import org.bukkit.HeightMap
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import yv.tils.remadeEnderDragon.configs.ConfigFile
import yv.tils.remadeEnderDragon.configs.DragonEggSettings
import yv.tils.remadeEnderDragon.language.Messages
import yv.tils.utils.modules.Core
import java.util.UUID

/** Global egg rules: carrying an egg outside the End must not bypass protection. */
class DragonEggProtection(
    private val settings: () -> DragonEggSettings = { ConfigFile.state.dragonEgg },
) : Listener {
    private val fallingEggs = mutableMapOf<UUID, Entity>()
    private var protectingVoid = false
    private var task: BukkitTask? = null

    fun enable() {
        task = Bukkit.getScheduler().runTaskTimer(Core.instance, Runnable {
            if (settings().voidDisappearing) {
                fallingEggs.clear()
                protectingVoid = false
                return@Runnable
            }
            // Scan loaded entities once when protection is enabled (also after a live config edit).
            if (!protectingVoid) {
                protectingVoid = true
                Bukkit.getWorlds().forEach { world -> world.entities.forEach(::track) }
            }
            fallingEggs.values.toList().forEach { entity ->
                if (!entity.isValid || !isEgg(entity)) fallingEggs.remove(entity.uniqueId)
                // Falling blocks can be discarded without a damage event on crossing minHeight.
                // Check every tick, before their normal terminal velocity can cross that boundary.
                else if (!entity.isOnGround && entity.location.y < entity.world.minHeight + 4.0) recover(entity)
            }
        }, 1L, 1L)
    }

    fun close() {
        task?.cancel()
        task = null
        fallingEggs.clear()
        protectingVoid = false
    }

    private fun blocked(inventory: Inventory): Boolean = when (inventory.type) {
        InventoryType.ENDER_CHEST -> !DragonEggRules.allowsStorage(settings(), enderChest = true)
        InventoryType.CHEST, InventoryType.BARREL, InventoryType.SHULKER_BOX,
        InventoryType.HOPPER, InventoryType.DISPENSER, InventoryType.DROPPER,
        InventoryType.FURNACE, InventoryType.BLAST_FURNACE, InventoryType.SMOKER,
        InventoryType.BREWING, InventoryType.CRAFTER, InventoryType.DECORATED_POT,
        InventoryType.SHELF -> !DragonEggRules.allowsStorage(settings(), enderChest = false)
        else -> false
    }

    private fun warn(player: Player, inventory: Inventory) {
        Messages.actionBar(player, if (inventory.type == InventoryType.ENDER_CHEST)
            "egg.ender-chest-blocked" else "egg.chest-blocked")
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun click(event: InventoryClickEvent) {
        val top = event.view.topInventory
        if (!blocked(top) || event.clickedInventory == null) return
        val player = event.whoClicked as? Player ?: return
        val swapped = when (event.click) {
            ClickType.NUMBER_KEY -> if (event.hotbarButton in 0..8) player.inventory.getItem(event.hotbarButton)
                else player.inventory.itemInOffHand
            ClickType.SWAP_OFFHAND -> player.inventory.itemInOffHand
            else -> null
        }
        if (DragonEggRules.insertsEgg(event.action, event.clickedInventory == top,
                DragonEggRules.containsEgg(event.cursor), DragonEggRules.containsEgg(event.currentItem),
                DragonEggRules.containsEgg(swapped))) {
            event.isCancelled = true
            warn(player, top)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun drag(event: InventoryDragEvent) {
        val top = event.view.topInventory
        if (!blocked(top) || !DragonEggRules.containsEgg(event.oldCursor)) return
        if (event.rawSlots.none { it in 0 until top.size }) return
        event.isCancelled = true
        (event.whoClicked as? Player)?.let { warn(it, top) }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun move(event: InventoryMoveItemEvent) {
        if (blocked(event.destination) && DragonEggRules.containsEgg(event.item)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun pickup(event: InventoryPickupItemEvent) {
        if (blocked(event.inventory) && DragonEggRules.containsEgg(event.item.itemStack)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun place(event: BlockPlaceEvent) {
        // Placing an already-filled shulker box would otherwise bypass the storage rule.
        if (!settings().chestPuttable && event.itemInHand.type != Material.DRAGON_EGG &&
            DragonEggRules.containsEgg(event.itemInHand)) {
            event.isCancelled = true
            Messages.actionBar(event.player, "egg.chest-blocked")
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun interact(event: PlayerInteractEvent) {
        if (settings().chestPuttable || event.action != Action.RIGHT_CLICK_BLOCK ||
            event.useInteractedBlock() == Event.Result.DENY || event.player.isSneaking) return
        val type = event.clickedBlock?.type ?: return
        // These containers accept items directly, without opening an inventory window.
        val insertsEgg = when {
            type == Material.DECORATED_POT -> DragonEggRules.containsEgg(event.item)
            type.name.endsWith("_SHELF") -> event.player.inventory.storageContents.any(DragonEggRules::containsEgg)
            else -> false
        }
        if (insertsEgg) {
            event.setUseInteractedBlock(Event.Result.DENY)
            Messages.actionBar(event.player, "egg.chest-blocked")
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun quit(event: PlayerQuitEvent) {
        if (settings().logoutable) return
        val player = event.player
        takeCarriedEggs(player).forEach { player.world.dropItem(player.location, it) }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun death(event: PlayerDeathEvent) {
        if (event.isCancelled || !settings().alwaysDropOnDeath) return
        val carried = takeCarriedEggs(event.entity)
        val kept = event.itemsToKeep.filter(DragonEggRules::containsEgg)
        event.itemsToKeep.removeIf(DragonEggRules::containsEgg)
        DragonEggRules.addMissingDeathDrops(carried, event.drops)
        DragonEggRules.addMissingDeathDrops(kept, event.drops)
        // Keep the death event's keep-inventory/XP flags unchanged for every other item.
    }

    private fun takeCarriedEggs(player: Player): List<ItemStack> {
        val carried = mutableListOf<ItemStack>()
        // Return crafting/menu inputs and the cursor before inspecting carried stacks.
        player.closeInventory()
        val inventory = player.inventory
        for (slot in 0 until inventory.size) {
            val stack = inventory.getItem(slot) ?: continue
            if (!DragonEggRules.containsEgg(stack)) continue
            inventory.setItem(slot, null)
            carried += stack
        }
        val cursor = player.itemOnCursor
        if (DragonEggRules.containsEgg(cursor)) {
            player.setItemOnCursor(null)
            carried += cursor
        }
        return carried
    }

    @EventHandler
    fun added(event: EntityAddToWorldEvent) {
        if (!settings().voidDisappearing) track(event.entity)
    }

    private fun track(entity: Entity) {
        if (isEgg(entity)) fallingEggs[entity.uniqueId] = entity
    }

    private fun isEgg(entity: Entity): Boolean = when (entity) {
        is Item -> DragonEggRules.containsEgg(entity.itemStack)
        is FallingBlock -> entity.blockData.material == Material.DRAGON_EGG
        else -> false
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun voidDamage(event: EntityDamageEvent) {
        if (settings().voidDisappearing || event.cause != EntityDamageEvent.DamageCause.VOID || !isEgg(event.entity)) return
        event.isCancelled = true
        recover(event.entity)
    }

    private fun recover(entity: Entity) {
        val world = entity.world
        val spawn = world.spawnLocation
        val y = (world.getHighestBlockYAt(spawn.blockX, spawn.blockZ, HeightMap.MOTION_BLOCKING_NO_LEAVES) + 1.5)
            .coerceIn(world.minHeight + 2.0, world.maxHeight - 1.0)
        val destination = spawn.clone().apply { x = blockX + 0.5; z = blockZ + 0.5; this.y = y }
        if (entity.teleport(destination)) {
            entity.velocity = Vector()
            entity.fallDistance = 0f
        }
    }
}
