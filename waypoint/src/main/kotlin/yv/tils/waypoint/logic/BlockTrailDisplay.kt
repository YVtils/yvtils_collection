package yv.tils.waypoint.logic

import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import yv.tils.utils.modules.Core
import org.bukkit.Location
import yv.tils.waypoint.config.ConfigFile

/** Displays are hidden by default, never saved and only revealed to their navigator. */
class BlockTrailDisplay {
    private val displays = mutableListOf<BlockDisplay>()
    private var cells = emptyList<Triple<Int, Int, Int>>()
    private var material = ""

    fun update(player: Player, target: Location) {
        val blocks = TerrainTrail.blocks(player.location, target, ConfigFile.state.trailLength)
        val next = blocks.map { Triple(it.x, it.y, it.z) }
        val configuredMaterial = ConfigFile.state.highlightMaterial
        if (next == cells && configuredMaterial == material && displays.all { it.isValid }) return
        clear()
        cells = next
        material = configuredMaterial
        try {
            blocks.forEach { block ->
                val display = block.world.spawn(block.location.add(-0.01, -0.01, -0.01), BlockDisplay::class.java) {
                    it.isVisibleByDefault = false
                    it.isPersistent = false
                    it.block = Material.valueOf(ConfigFile.state.highlightMaterial.uppercase()).createBlockData()
                    it.isGlowing = true
                    it.glowColorOverride = Color.LIME
                    it.transformation =
                        Transformation(Vector3f(), Quaternionf(), Vector3f(1.02f, 1.02f, 1.02f), Quaternionf())
                    it.viewRange = 2f
                }
                displays.add(display)
                player.showEntity(Core.instance, display)
            }
        } catch (error: Exception) {
            clear()
            throw error
        }
    }

    fun clear() {
        displays.forEach { it.remove() }
        displays.clear()
        cells = emptyList()
    }
}
