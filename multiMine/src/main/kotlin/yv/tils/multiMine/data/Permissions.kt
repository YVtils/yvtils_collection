/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

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

    COMMAND_MULTIMINE_TOGGLE_SELF(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.multiMine.toggle.self",
            "Allows the use of the /multiMine toggle subcommand",
            default = true
        )
    ),

    COMMAND_MULTIMINE_TOGGLE_OTHERS(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.multiMine.toggle.others",
            "Allows toggling multiMine for other players using /multiMine toggle <player>",
            default = false
        )
    ),

    USE_MULTIMINE(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.use.multiMine",
            "Allows the use of the multiMine feature",
            default = true
        )
    ), ;
}
