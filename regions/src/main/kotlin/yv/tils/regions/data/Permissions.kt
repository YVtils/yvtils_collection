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

package yv.tils.regions.data

import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.RemoteConsoleCommandSender
import yv.tils.common.permissions.PermissionManager
import yv.tils.regions.RegionsYVtils
import yv.tils.regions.language.LangStrings
import yv.tils.regions.language.RegionFailure

class PermissionsData {
    companion object {
        val permissionBase = "yvtils.${RegionsYVtils.MODULE.name}"

        val wildcard: PermissionManager.YVtilsPermission
            get() = PermissionManager.YVtilsPermission(
                "${permissionBase}.*",
                "Wildcard permission for all ${RegionsYVtils.MODULE.name} permissions",
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
        val permList = Permissions.entries.map { entry ->
            if (entry == Permissions.ADMIN) entry.permission.copy(
                children = Permissions.entries
                    .filter { it != Permissions.ADMIN }.associate { it.permission.name to true }) else entry.permission
        } + listOf("claims", "flags", "members", "subzones", "bypass", "admin").map { group ->
            PermissionManager.YVtilsPermission(
                "$permissionBase.$group.*", "All $group actions", false,
                Permissions.entries.filter { it.permission.name.startsWith("$permissionBase.$group.") }
                    .associate { it.permission.name to true })
        }

        return if (includeWildcard) {
            permList + wildcard
        } else {
            permList
        }
    }
}

enum class Permissions(val permission: PermissionManager.YVtilsPermission) {
    CLAIM(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.claim",
            "Create survival claims",
            default = true
        )
    ),
    MANAGE(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.manage",
            "Open claim management",
            default = true
        )
    ),
    ADMIN(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.admin",
            "Manage all claims and server flag policy",
            default = false
        )
    ),
    WORLDGUARD(
        PermissionManager.YVtilsPermission(
            "${PermissionsData.permissionBase}.worldguard",
            "Allows checking the WorldGuard hook status and querying regions at your location",
            default = true
        )
    ),
    SELECT(node("claims.select", "Select claim corners", true)),
    PREVIEW(node("claims.preview", "Preview claim and subzone boundaries", true)),
    INFO(node("claims.info", "Read claim information and inspect current claim", true)),
    LIST(node("claims.list", "List personal claims and public command lists", true)),
    DELETE(node("claims.delete", "Delete owned claims", true)),
    RENAME(node("claims.rename", "Rename owned claims", true)),
    RESIZE(node("claims.resize", "Resize owned claims", true)),
    MERGE(node("claims.merge", "Merge owned claims", true)),
    FLAGS_VIEW(node("flags.view", "View claim flag groups", true)),
    FLAGS_EDIT(node("flags.edit", "Edit allowed claim flags", true)),
    FLAGS_RESET(node("flags.reset", "Restore all claim flag defaults", true)),
    MEMBERS_VIEW(node("members.view", "View owners and members", true)),
    MEMBERS_EDIT(node("members.edit", "Add and remove members", true)),
    OWNERS_EDIT(node("members.owners", "Grant or remove ownership", true)),
    SUBZONES_VIEW(node("subzones.view", "Open subzone menus", true)),
    SUBZONES_CREATE(node("subzones.create", "Create and select 3D subzones", true)),
    SUBZONES_DELETE(node("subzones.delete", "Delete subzones", true)),
    SUBZONES_FLAGS(node("subzones.flags", "Edit subzone flag overrides", true)),
    SUBZONES_OPEN(node("subzones.open", "Enable or disable open protection", true)),
    ADMIN_OTHERS(node("admin.others", "Manage and browse other players' claims", false)),
    ADMIN_CONFIG(node("admin.config", "Edit server claim configuration", false)),
    ADMIN_POLICIES(node("admin.policies", "Edit server flag policies and defaults", false)),
    ADMIN_CREATE(node("admin.create", "Create for another owner or world", false)),
    ADMIN_MESSAGES(node("admin.messages", "Edit claim transition messages", false)),
    BYPASS_COST(node("bypass.cost", "Bypass diamond and cluster charges", false)),
    BYPASS_LIMITS(node("bypass.limits", "Bypass ownership and membership count limits", false)),
    BYPASS_SURVIVAL(node("bypass.survival", "Create outside survival mode", false));

    fun allowed(sender: CommandSender): Boolean =
        sender is ConsoleCommandSender || sender is RemoteConsoleCommandSender ||
                sender.hasPermission(permission.name)

    fun require(sender: CommandSender) {
        if (!allowed(sender)) throw RegionFailure(LangStrings.PERMISSION_DENIED, mapOf("permission" to permission.name))
    }
}

private fun node(suffix: String, description: String, default: Boolean) =
    PermissionManager.YVtilsPermission("${PermissionsData.permissionBase}.$suffix", description, default)
