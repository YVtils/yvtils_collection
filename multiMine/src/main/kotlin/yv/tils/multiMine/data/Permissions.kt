package yv.tils.multiMine.data

import yv.tils.common.permissions.PermissionManager
import yv.tils.multiMine.MultiMineYVtils


class PermissionsData {
    companion object {
        val permissionBase = "yvtils.${MultiMineYVtils.MODULE.name}"

        val wildcard: PermissionManager.YVtilsPermission
            get() = PermissionManager.YVtilsPermission(
                "${permissionBase}.*",
                "Wildcard permission for all ${MultiMineYVtils.MODULE.name} permissions",
                default = true,
                children = getPermissionsForWildcard()
            )

        private fun getPermissionsForWildcard(): Map<String, Boolean> {
            val permissions = mutableMapOf<String, Boolean>()
            val permissionList = PermissionsData().getPermissionList()

            permissionList.forEach { permissions[it.name] = true }

            return permissions
        }
    }

    fun getPermissionList(includeWildcard: Boolean = false): List<PermissionManager.YVtilsPermission> {
        val permList = Permissions.entries.map { it.permission }

        return if (includeWildcard) {
            permList + wildcard
        } else {
            permList
        }
    }
}

enum class Permissions(val permission: PermissionManager.YVtilsPermission) {
    COMMAND_MULTIMINE_MANAGE(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.multiMine.manage",
            "Allows the use of the /multiMine manage subcommand",
            default = false
        )
    ),

    COMMAND_MULTIMINE_TOGGLE(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.multiMine.toggle",
            "Allows the use of the /multiMine toggle subcommand",
            default = true
        )
    ),

    USE_MULTIMINE(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.use.multiMine",
            "Allows the use of the multiMine feature",
            default = true
        )
    ),
}
