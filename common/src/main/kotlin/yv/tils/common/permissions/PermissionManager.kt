package yv.tils.common.permissions

import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger

class PermissionManager {
    companion object {
        fun registerPermissions(perms: List<YVtilsPermission>) {
            if (perms.isEmpty()) {
                Logger.warn("No permissions provided for registration.")
                return
            }

            Logger.debug("Registering permissions: ${perms.joinToString { it.name }}", 1)
            registerPermission(perms)
        }

        private fun registerPermission(perms: List<YVtilsPermission>) {
            val pluginManager = Data.instance.server.pluginManager
            val created = mutableMapOf<String, Permission>()

            perms.forEach { p ->
                val perm = Permission(
                    p.name,
                    p.description,
                    if (p.default) PermissionDefault.TRUE else PermissionDefault.FALSE
                )
                created[p.name] = perm
            }

            perms.forEach { p ->
                val perm = created[p.name] !!
                if (p.children.isNotEmpty()) {
                    perm.children.putAll(p.children)
                    perm.recalculatePermissibles()
                }
            }

            created.values.forEach { pluginManager.addPermission(it) }
        }
    }

    /**
     * Represents a permission in the YVtils system.
     *
     * @property name The name of the permission.
     * @property description A brief description of what the permission allows.
     * @property default The default state of the permission (true for granted, false for denied).
     * @property children A map of child permissions, where the key is the child permission name and the value indicates if it is granted by default.
     */
    data class YVtilsPermission(
        val name: String,
        val description: String,
        val default: Boolean = false,
        val children: Map<String, Boolean> = emptyMap(),
    )
}
