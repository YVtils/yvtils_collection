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

package yv.tils.waypoint.permissions

import yv.tils.common.permissions.PermissionManager
import yv.tils.waypoint.WaypointYVtils

class PermissionsData {
    companion object {
        val permissionBase = "yvtils.${WaypointYVtils.MODULE.name}"

        val wildcard: PermissionManager.YVtilsPermission
            get() = PermissionManager.YVtilsPermission(
                "${permissionBase}.*",
                "Wildcard permission for all ${WaypointYVtils.MODULE.name} permissions",
                default = false,
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
    USE(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.use",
            "Use waypoints and navigation",
            default = true
        )
    ),
    CREATE(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.create",
            "Create waypoints",
            default = true
        )
    ),
    ADMIN(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.admin",
            "Manage all waypoints",
            default = false
        )
    ),
    CONFIG(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.config",
            "Edit Waypoint configuration in game",
            default = false
        )
    ),
}
