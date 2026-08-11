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

package yv.tils.yv_smp.permissions

import yv.tils.common.permissions.PermissionManager
import yv.tils.yv_smp.YV_SMPYVtils
import kotlin.collections.forEach
import kotlin.collections.plus

class PermissionsData {
    companion object {
        val permissionBase = "yvtils.${YV_SMPYVtils.MODULE.name}"

        val wildcard: PermissionManager.YVtilsPermission
            get() = PermissionManager.YVtilsPermission(
                "${permissionBase}.*",
                "Wildcard permission for all ${YV_SMPYVtils.MODULE.name} permissions",
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
    COMMAND_START(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.start",
            "Allows the use of the /start command",
            default = false
        )
    ),
    COMMAND_MUSIC(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.music",
            "Allows the use of the /music command",
            default = false
        )
    ),
    COMMAND_MUSIC_PLAY(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.music.play",
            "Allows playing audio for yourself",
            default = false
        )
    ),
    COMMAND_MUSIC_STOP(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.music.stop",
            "Allows stopping your own audio",
            default = false
        )
    ),
    COMMAND_MUSIC_BROADCAST(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.music.broadcast",
            "Allows broadcasting audio to all players",
            default = false
        )
    ),
    COMMAND_MUSIC_FADE(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.music.fade",
            "Allows fading out audio",
            default = false
        )
    ),
    COMMAND_MUSIC_STATUS(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.music.status",
            "Allows checking music system status",
            default = false
        )
    ),
}
