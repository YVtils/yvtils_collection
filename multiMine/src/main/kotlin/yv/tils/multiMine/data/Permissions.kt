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
//    SYNC_CHAT(
//        PermissionManager.YVtilsPermission(
//            "${PermissionsData.permissionBase}.sync.chat",
//            "Sync chat messages between Discord and Minecraft",
//            default = true
//        )
//    ),
}
