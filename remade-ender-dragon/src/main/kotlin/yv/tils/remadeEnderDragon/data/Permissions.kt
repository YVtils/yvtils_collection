/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.remadeEnderDragon.data

import yv.tils.common.permissions.PermissionManager

class PermissionsData {
    companion object {
        const val permissionBase = "yvtils.remade-ender-dragon"
        val wildcard: PermissionManager.YVtilsPermission
            get() = PermissionManager.YVtilsPermission(
                "$permissionBase.*", "All Remade Ender Dragon permissions", default = false,
                children = Permissions.entries.associate { it.permission.name to true },
            )
    }

    fun getPermissionList(includeWildcard: Boolean = false): List<PermissionManager.YVtilsPermission> =
        Permissions.entries.map { it.permission } + if (includeWildcard) listOf(wildcard) else emptyList()
}

enum class Permissions(val permission: PermissionManager.YVtilsPermission) {
    ADMIN(PermissionManager.YVtilsPermission(
        "${PermissionsData.permissionBase}.admin", "Manage and preview dragon encounters", default = false,
        children = mapOf("${PermissionsData.permissionBase}.config" to true),
    )),
    CONFIG(PermissionManager.YVtilsPermission(
        "${PermissionsData.permissionBase}.config", "Edit dragon encounter configuration", default = false,
    )),
}
