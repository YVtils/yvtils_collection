package yv.tils.vanish.permissions

import yv.tils.common.permissions.PermissionManager
import yv.tils.common.permissions.PermissionManager.YVtilsPermission

object VanishPermissions {
    const val USE = "yvtils.vanish.use"
    const val LAYERS = "yvtils.vanish.layers"
    const val CONFIG = "yvtils.vanish.config"
    const val SILENT_CONTAINERS = "yvtils.vanish.function.silentcontainers"
    const val PICKUP = "yvtils.vanish.function.preventpickup"
    const val DROP = "yvtils.vanish.function.preventdrop"
    const val MOB_TARGET = "yvtils.vanish.function.preventmobtarget"
    const val DAMAGE = "yvtils.vanish.function.preventdamage"
    const val PHYSICAL = "yvtils.vanish.function.preventphysical"
    const val COLLISION = "yvtils.vanish.function.disablecollision"
    fun layer(layer: Int) = "yvtils.vanish.layer.$layer"
    fun register() = PermissionManager.registerPermissions(
        listOf(
            YVtilsPermission(USE, "Toggle vanish"),
            YVtilsPermission(LAYERS, "Select vanish layer (also needs layer.N)"),
            YVtilsPermission(CONFIG, "Edit Vanish settings")
        ) + listOf(SILENT_CONTAINERS, PICKUP, DROP, MOB_TARGET, DAMAGE, PHYSICAL, COLLISION).map {
            YVtilsPermission(it, "Vanish capability ${it.substringAfterLast('.')}")
        } + (1..100).map { YVtilsPermission(layer(it), "Use vanish layer $it") } +
                YVtilsPermission(
                    "yvtils.vanish.*", "All vanish permissions", children =
                        (listOf(
                            USE,
                            LAYERS,
                            CONFIG,
                            SILENT_CONTAINERS,
                            PICKUP,
                            DROP,
                            MOB_TARGET,
                            DAMAGE,
                            PHYSICAL,
                            COLLISION
                        ) +
                                (1..100).map(::layer)).associateWith { true })
    )
}
