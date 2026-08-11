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
            "Allows the use of the /yvsmp start command",
            default = false
        )
    ),
    COMMAND_START_STOP(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.start.stop",
            "Allows aborting a running /yvsmp start sequence",
            default = false
        )
    ),
    COMMAND_SETUP(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.setup",
            "Allows the use of the /yvsmp setup command",
            default = false
        )
    ),
    COMMAND_SETUP_BORDER(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.setup.border",
            "Allows setting the world border size",
            default = false
        )
    ),
    COMMAND_SETUP_BORDERCENTER(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.setup.bordercenter",
            "Allows setting the world border center",
            default = false
        )
    ),
    COMMAND_SETUP_SPAWN(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.setup.spawn",
            "Allows setting the world spawn location",
            default = false
        )
    ),
    COMMAND_SETUP_RESCAN(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.setup.rescan",
            "Allows clearing the cached island edge scan",
            default = false
        )
    ),
    COMMAND_SETUP_ALL(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.setup.all",
            "Allows running the full SMP setup routine",
            default = false
        )
    ),
    COMMAND_SETUP_STATUS(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.setup.status",
            "Allows viewing the current SMP setup status",
            default = false
        )
    ),
    COMMAND_GUI(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.gui",
            "Allows opening the /yvsmp GUI control menu",
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
