package yv.tils.waypoint.config

import org.bukkit.Material
import yv.tils.config.data.annotations.ConfigDescription
import yv.tils.config.files.ConfigFormat
import yv.tils.config.files.ObjectMapperFileUtils

data class WaypointConfig(
    @ConfigDescription("Maximum highlighted blocks (1–64)") var trailLength: Int = 32,
    @ConfigDescription("Arrival distance (0.5–16)") var arrivalDistance: Double = 3.0,
    @ConfigDescription("Highlight block material") var highlightMaterial: String = "LIME_STAINED_GLASS",
    @ConfigDescription("Maximum waypoints per player (1–1000)") var maxPerPlayer: Int = 50,
)

object ConfigFile {
    private const val PATH = "/waypoint/config.yml"
    var state = WaypointConfig()
        private set

    fun load() = apply(ObjectMapperFileUtils.load(PATH, WaypointConfig(), ConfigFormat.YAML))
    fun apply(next: WaypointConfig) {
        require(next.trailLength in 1..64 && next.arrivalDistance.isFinite() && next.arrivalDistance in 0.5..16.0)
        require(next.maxPerPlayer in 1..1000 && Material.matchMaterial(next.highlightMaterial)?.isBlock == true)
        val validated = next.copy(highlightMaterial = Material.matchMaterial(next.highlightMaterial)!!.name)
        ObjectMapperFileUtils.save(PATH, validated, ConfigFormat.YAML)
        state = validated
    }
}
