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

package yv.tils.message.data

import yv.tils.common.permissions.PermissionManager
import yv.tils.message.MessageYVtils

class PermissionsData {
    companion object {
        val permissionBase = "yvtils.${MessageYVtils.MODULE.name}"

        val wildcard: PermissionManager.YVtilsPermission
            get() = PermissionManager.YVtilsPermission(
                "$permissionBase.*",
                "Wildcard permission for all ${MessageYVtils.MODULE.name} permissions",
                children = Permissions.entries.associate { it.permission.name to true }
            )
    }

    fun getPermissionList(includeWildcard: Boolean = false): List<PermissionManager.YVtilsPermission> {
        val permissions = Permissions.entries.map { it.permission }
        return if (includeWildcard) permissions + wildcard else permissions
    }
}

enum class Permissions(val permission: PermissionManager.YVtilsPermission) {
    COMMAND_MSG(PermissionManager.YVtilsPermission(
        "${PermissionsData.permissionBase}.command.msg", "Send private messages with /msg and its aliases", default = true
    )),
    COMMAND_MSG_ALL(PermissionManager.YVtilsPermission(
        "${PermissionsData.permissionBase}.command.msg.all", "Send messages to multiple players using selectors such as @a"
    )),
    COMMAND_REPLY(PermissionManager.YVtilsPermission(
        "${PermissionsData.permissionBase}.command.reply", "Reply to private messages with /reply or /r", default = true
    )),
    RECEIVE(PermissionManager.YVtilsPermission(
        "${PermissionsData.permissionBase}.receive", "Receive private messages", default = true
    )),
}
