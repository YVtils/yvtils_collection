/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.remadeEnderDragon.logic

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import yv.tils.configv2.files.ObjectMapperFileUtils
import yv.tils.utils.logger.Logger
import yv.tils.utils.logger.DEBUG_LEVEL

data class MarkerRecord(var world: String = "", var x: Int = 0, var y: Int = 0, var z: Int = 0, var material: String = "SHORT_GRASS")
data class TerrainJournal(var markers: List<MarkerRecord> = emptyList())

/** Only owns added dry grass (and legacy short grass). Journal before modifying air. */
class TemporaryTerrain : Listener {
    private val path = "/remade-ender-dragon/terrain-journal.json"
    private val markers = mutableMapOf<MarkerRecord, Long>()
    private val coordinates = mutableMapOf<String, MarkerRecord>()
    private fun coordinate(world: String, x: Int, y: Int, z: Int) = "$world:$x:$y:$z"

    fun recover() {
        ObjectMapperFileUtils.load(path, TerrainJournal()).markers.forEach { markers[it] = 0L }
        markers.keys.forEach { coordinates[coordinate(it.world, it.x, it.y, it.z)] = it }
        if (markers.isNotEmpty()) Logger.info("[Remade Ender Dragon] Recovering ${markers.size} temporary terrain markers.")
        restore { true }
    }

    private fun record(block: Block) = coordinates[coordinate(block.world.uid.toString(), block.x, block.y, block.z)]
        ?: MarkerRecord(block.world.uid.toString(), block.x, block.y, block.z, Material.SHORT_DRY_GRASS.name)
    fun contains(block: Block) = markers.containsKey(record(block))

    fun place(blocks: List<Block>, expires: Long) {
        val added = blocks.filter { it.type == Material.AIR && !contains(it) }
        added.forEach {
            val r = record(it)
            markers[r] = expires
            coordinates[coordinate(r.world, r.x, r.y, r.z)] = r
        }
        save()
        added.forEach { it.setType(Material.SHORT_DRY_GRASS, false) }
        Logger.debug("[Remade Ender Dragon] Placed ${added.size} temporary infestation markers.", DEBUG_LEVEL.DETAILED)
    }

    fun expire(now: Long) = restore { markers.getValue(it) <= now }
    fun restoreBlocks(blocks: List<Block>) {
        val owned = blocks.map(::record).toSet()
        restore { it in owned }
    }
    fun restoreWorld(world: String) = restore { it.world == world }
    fun close() = restore { true }

    private fun restore(predicate: (MarkerRecord) -> Boolean) {
        var changed = false
        markers.keys.toList().filter(predicate).forEach { r ->
            val world = Bukkit.getWorld(java.util.UUID.fromString(r.world)) ?: return@forEach
            if (!world.isChunkLoaded(r.x shr 4, r.z shr 4)) return@forEach
            val block = world.getBlockAt(r.x, r.y, r.z)
            if (block.type.name == r.material) block.setType(Material.AIR, false)
            markers.remove(r)
            coordinates.remove(coordinate(r.world, r.x, r.y, r.z))
            changed = true
        }
        if (changed) save()
    }

    private fun save() = ObjectMapperFileUtils.save(path, TerrainJournal(markers.keys.toList()))

    @EventHandler
    fun physics(event: BlockPhysicsEvent) { if (contains(event.block)) event.isCancelled = true }

    @EventHandler(ignoreCancelled = true)
    fun breakBlock(event: BlockBreakEvent) {
        if (contains(event.block) || contains(event.block.getRelative(0, 1, 0))) event.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun placeBlock(event: BlockPlaceEvent) { if (contains(event.block)) event.isCancelled = true }

    @EventHandler(ignoreCancelled = true)
    fun fluid(event: BlockFromToEvent) { if (contains(event.toBlock)) event.isCancelled = true }

    @EventHandler
    fun load(event: ChunkLoadEvent) {
        restore { it.world == event.world.uid.toString() && it.x shr 4 == event.chunk.x && it.z shr 4 == event.chunk.z && markers.getValue(it) == 0L }
    }

    @EventHandler(ignoreCancelled = true)
    fun unload(event: ChunkUnloadEvent) {
        restore { it.world == event.world.uid.toString() && it.x shr 4 == event.chunk.x && it.z shr 4 == event.chunk.z }
    }
}
