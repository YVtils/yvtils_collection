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

package yv.tils.moderation.data

import yv.tils.common.permissions.PermissionManager
import yv.tils.moderation.ModerationYVtils

class PermissionsData {
    companion object {
        val permissionBase = "yvtils.${ModerationYVtils.MODULE.name}"

        val wildcard: PermissionManager.YVtilsPermission
            get() = PermissionManager.YVtilsPermission(
                "${permissionBase}.*",
                "Wildcard permission for all ${ModerationYVtils.MODULE.name} permissions",
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
    COMMAND_MODERATION_BAN(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.ban",
            "Allows the use of the /ban command",
            default = false
        )
    ),

    COMMAND_MODERATION_TEMPBAN(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.tempban",
            "Allows the use of the /tempban command",
            default = false
        )
    ),

    COMMAND_MODERATION_UNBAN(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.unban",
            "Allows the use of the /unban command",
            default = false
        )
    ),

    COMMAND_MODERATION_MUTE(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.mute",
            "Allows the use of the /mute command",
            default = false
        )
    ),

    COMMAND_MODERATION_TEMPMUTE(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.tempmute",
            "Allows the use of the /tempmute command",
            default = false
        )
    ),

    COMMAND_MODERATION_UNMUTE(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.unmute",
            "Allows the use of the /unmute command",
            default = false
        )
    ),

    COMMAND_MODERATION_KICK(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.kick",
            "Allows the use of the /kick command",
            default = false
        )
    ),

    COMMAND_MODERATION_WARN(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.command.warn",
            "Allows the use of the /warn command",
            default = false
        )
    ),

    MODERATION_BROADCAST(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.broadcast",
            "Allows to receive broadcasts of moderation actions",
            default = false
        )
    )
}
