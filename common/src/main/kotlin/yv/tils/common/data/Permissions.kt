package yv.tils.common.data

import org.bukkit.permissions.PermissionDefault

enum class Permissions(val permission: String, val description: String? = null, val default: PermissionDefault = PermissionDefault.OP) {
    COMMON_UPDATE_CHECK("yvtils.common.updatecheck", "Get announcements about new versions of the plugin", PermissionDefault.OP),
}