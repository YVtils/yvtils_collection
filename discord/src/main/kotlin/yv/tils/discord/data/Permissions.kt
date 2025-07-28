package yv.tils.discord.data

import yv.tils.common.permissions.PermissionManager
import yv.tils.discord.DiscordYVtils

class PermissionsData {
    companion object {
        val permissionBase = "yvtils.${DiscordYVtils.MODULE.name}"

        val wildcard: PermissionManager.YVtilsPermission
            get() = PermissionManager.YVtilsPermission(
                "${permissionBase}.*",
                "Wildcard permission for all ${DiscordYVtils.MODULE.name} permissions",
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
    SYNC_CHAT(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.sync.chat",
            "Sync chat messages between Discord and Minecraft",
            default = true
        )
    ),
    SYNC_ADVANCEMENTS(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.sync.advancements",
            "Sync advancements between Discord and Minecraft",
            default = true
        )
    ),
    SYNC_JOIN(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.sync.join",
            "Sync player join events between Discord and Minecraft",
            default = true
        )
    ),
    SYNC_QUIT(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.sync.quit",
            "Sync player quit events between Discord and Minecraft",
            default = true
        )
    ),
    SYNC_DEATHS(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.sync.deaths",
            "Sync player deaths between Discord and Minecraft",
            default = true
        )
    ),
}
