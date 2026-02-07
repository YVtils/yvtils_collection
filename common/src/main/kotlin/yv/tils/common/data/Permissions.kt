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

package yv.tils.common.data

import yv.tils.common.CommonYVtils
import yv.tils.common.data.PermissionsData.Companion.permissionBase
import yv.tils.common.permissions.PermissionManager

class PermissionsData {
    companion object {
        val permissionBase = "yvtils.${CommonYVtils.MODULE.name}"

        val wildcard: PermissionManager.YVtilsPermission
            get() = PermissionManager.YVtilsPermission(
                "${permissionBase}.*",
                "Wildcard permission for all ${CommonYVtils.MODULE.name} permissions",
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
    COMMON_UPDATE_CHECK(
        PermissionManager.YVtilsPermission(
            "$permissionBase.updatecheck",
            "Get announcements about new versions of the plugin",
            default = false
        )
    ),
}
