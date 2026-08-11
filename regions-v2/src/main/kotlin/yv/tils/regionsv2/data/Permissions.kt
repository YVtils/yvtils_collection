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

package yv.tils.regionsv2.data

import yv.tils.common.permissions.PermissionManager
import yv.tils.regionsv2.RegionsV2YVtils

class PermissionsData {
    companion object {
        val permissionBase = "yvtils.${RegionsV2YVtils.MODULE.name}"

        val wildcard: PermissionManager.YVtilsPermission
            get() = PermissionManager.YVtilsPermission(
                "${permissionBase}.*",
                "Wildcard permission for all ${RegionsV2YVtils.MODULE.name} permissions",
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
    EXAMPLE(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.example",
            "Allows using the regions-v2 example command",
            default = true
        )
    ),
    WORLDGUARD(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.worldguard",
            "Allows checking the WorldGuard hook status and querying regions at your location",
            default = true
        )
    ),
}
