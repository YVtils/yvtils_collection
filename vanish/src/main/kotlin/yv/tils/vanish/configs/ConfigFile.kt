package yv.tils.vanish.configs

import yv.tils.config.data.annotations.ConfigDescription
import yv.tils.config.files.ConfigFormat
import yv.tils.config.files.ObjectMapperFileUtils

data class VanishConfig(
    @ConfigDescription("Default vanish layer (1–100)") var defaultLayer: Int = 1,
    @ConfigDescription("Maximum selectable layer (1–100)") var maxLayer: Int = 100,
    var silentContainers: Boolean = true,
    var preventPickup: Boolean = true,
    var preventDrop: Boolean = true,
    var preventMobTarget: Boolean = true,
    var preventDamage: Boolean = true,
    var preventPhysicalInteraction: Boolean = true,
    var disableCollision: Boolean = true,
)

object ConfigFile {
    private const val PATH = "/vanish/config.yml"
    var state = VanishConfig()
        private set
    var changed: () -> Unit = {}
    fun load() = apply(ObjectMapperFileUtils.load(PATH, VanishConfig(), ConfigFormat.YAML))
    fun apply(next: VanishConfig) {
        require(next.maxLayer in 1..100 && next.defaultLayer in 1..next.maxLayer)
        ObjectMapperFileUtils.save(PATH, next, ConfigFormat.YAML)
        state = next.copy()
        changed()
    }
}
