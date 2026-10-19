package yv.tils.vanish.configs

import org.bukkit.entity.Player
import yv.tils.config.files.ObjectMapperFileUtils
import yv.tils.vanish.permissions.VanishPermissions
import java.util.UUID

data class VanishSettings(
    val layer: Int = 1,
    val announce: Boolean = true,
    val silentContainers: Boolean = true,
    val preventPickup: Boolean = true,
    val preventDrop: Boolean = true,
    val preventMobTarget: Boolean = true,
    val preventDamage: Boolean = true,
    val preventPhysical: Boolean = true,
    val disableCollision: Boolean = true,
) {
    fun enabled(permission: String): Boolean = when (permission) {
        VanishPermissions.SILENT_CONTAINERS -> silentContainers
        VanishPermissions.PICKUP -> preventPickup
        VanishPermissions.DROP -> preventDrop
        VanishPermissions.MOB_TARGET -> preventMobTarget
        VanishPermissions.DAMAGE -> preventDamage
        VanishPermissions.PHYSICAL -> preventPhysical
        VanishPermissions.COLLISION -> disableCollision
        else -> false
    }

    fun toggle(permission: String): VanishSettings = when (permission) {
        VanishPermissions.SILENT_CONTAINERS -> copy(silentContainers = !silentContainers)
        VanishPermissions.PICKUP -> copy(preventPickup = !preventPickup)
        VanishPermissions.DROP -> copy(preventDrop = !preventDrop)
        VanishPermissions.MOB_TARGET -> copy(preventMobTarget = !preventMobTarget)
        VanishPermissions.DAMAGE -> copy(preventDamage = !preventDamage)
        VanishPermissions.PHYSICAL -> copy(preventPhysical = !preventPhysical)
        VanishPermissions.COLLISION -> copy(disableCollision = !disableCollision)
        else -> this
    }
}

data class SavedSettings(val players: Map<UUID, VanishSettings> = emptyMap())

object PlayerSettings {
    private const val PATH = "/vanish/players.json"
    private var state = SavedSettings()
    fun load() {
        state = ObjectMapperFileUtils.load(PATH, SavedSettings())
    }

    fun get(uuid: UUID) = state.players[uuid] ?: VanishSettings(layer = ConfigFile.state.defaultLayer)
    fun save(player: Player, next: VanishSettings) {
        check(player.hasPermission(VanishPermissions.USE))
        if (next.layer != get(player.uniqueId).layer) {
            require(next.layer in 1..ConfigFile.state.maxLayer && player.hasPermission(VanishPermissions.layer(next.layer)))
            check(player.hasPermission(VanishPermissions.LAYERS))
        }
        val updated = SavedSettings(state.players + (player.uniqueId to next))
        ObjectMapperFileUtils.save(PATH, updated)
        state = updated
    }
}
