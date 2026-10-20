/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.status.data

import yv.tils.common.permissions.PermissionManager
import yv.tils.status.StatusYVtils

class PermissionsData {
    companion object {
        val permissionBase = "yvtils.${StatusYVtils.MODULE.name}"

        val wildcard: PermissionManager.YVtilsPermission
            get() = PermissionManager.YVtilsPermission(
                "$permissionBase.*",
                "Wildcard permission for all ${StatusYVtils.MODULE.name} permissions",
                children = Permissions.entries.associate { it.permission.name to true }
            )
    }

    fun getPermissionList(includeWildcard: Boolean = false): List<PermissionManager.YVtilsPermission> {
        val permissions = Permissions.entries.map { it.permission }
        return if (includeWildcard) permissions + wildcard else permissions
    }
}

enum class Permissions(val permission: PermissionManager.YVtilsPermission) {
    COMMAND_STATUS(PermissionManager.YVtilsPermission(
        "${PermissionsData.permissionBase}.command", "Use /status and its aliases", default = true
    )),
    COMMAND_STATUS_MANAGE_SELF(PermissionManager.YVtilsPermission(
        "${PermissionsData.permissionBase}.command.manage.self", "Open your status management GUI", default = true
    )),
    COMMAND_STATUS_MANAGE_OTHERS(PermissionManager.YVtilsPermission(
        "${PermissionsData.permissionBase}.command.manage.others", "Manage another player's status through the GUI"
    )),
    COMMAND_STATUS_SET(PermissionManager.YVtilsPermission(
        "${PermissionsData.permissionBase}.command.set", "Set custom status text", default = true
    )),
    COMMAND_STATUS_DEFAULT(PermissionManager.YVtilsPermission(
        "${PermissionsData.permissionBase}.command.default", "Select a configured default status", default = true
    )),
    COMMAND_STATUS_CLEAR(PermissionManager.YVtilsPermission(
        "${PermissionsData.permissionBase}.command.clear", "Clear your status", default = true
    )),
    COMMAND_STATUS_CLEAR_OTHERS(PermissionManager.YVtilsPermission(
        "${PermissionsData.permissionBase}.command.clear.others", "Clear another player's status"
    )),
    CONFIG(PermissionManager.YVtilsPermission(
        "${PermissionsData.permissionBase}.config", "Edit Status configuration in game"
    )),
}
