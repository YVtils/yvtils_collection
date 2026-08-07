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

package yv.tils.essentials.permissions

import yv.tils.common.permissions.PermissionManager
import yv.tils.essentials.EssentialYVtils

class PermissionsData {
    companion object {
        val permissionBase = "yvtils.${EssentialYVtils.MODULE.name}"

        val wildcard: PermissionManager.YVtilsPermission
            get() = PermissionManager.YVtilsPermission(
                "${permissionBase}.*",
                "Wildcard permission for all ${EssentialYVtils.MODULE.name} permissions",
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
    BYPASS_GLOBAL_MUTE(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.bypass.globalmute",
            "Bypass the global mute",
            default = false
        )
    ),

    BYPASS_DIMENSION_BLOCK(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.bypass.dimension.block",
            "Bypass dimension travel block",
            default = false
        )
    ),

    BYPASS_PVP_DISABLED(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.bypass.pvp.disabled",
            "Bypass disabled pvp",
            default = false
        )
    ),

    COMMAND_DIMENSION(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.dimension",
            "Use the /dimension command",
            default = false
        )
    ),

    COMMAND_FLY(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.fly",
            "Use the /fly command",
            default = false
        )
    ),

    COMMAND_GAMEMODE(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.gamemode",
            "Use the /gamemode command",
            default = false
        )
    ),

    COMMAND_GLOBALMUTE(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.globalmute",
            "Use the /globalmute command",
            default = false
        )
    ),

    COMMAND_GOD(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.god",
            "Use the /god command",
            default = false
        )
    ),

    COMMAND_HEAL(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.heal",
            "Use the /heal command",
            default = false
        )
    ),

    COMMAND_PING(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.ping",
            "Use the /ping command",
            default = true
        )
    ),

    COMMAND_PING_ARG_OTHER(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.ping.other",
            "Use the /ping command with another player as argument",
            default = false
        )
    ),

    COMMAND_PVP(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.pvp",
            "Use the /pvp command",
            default = false
        )
    ),

    COMMAND_SEED(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.seed",
            "Use the /seed command",
            default = false
        )
    ),

    COMMAND_SPEED(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.speed",
            "Use the /speed command",
            default = false
        )
    ),
}
