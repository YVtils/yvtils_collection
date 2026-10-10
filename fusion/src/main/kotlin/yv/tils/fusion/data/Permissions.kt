/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.data

import yv.tils.common.permissions.PermissionManager

class PermissionsData {
    fun getPermissionList() = listOf(
        PermissionManager.YVtilsPermission("yvtils.fusion.use", "Use Fusion crafting", default = true),
        PermissionManager.YVtilsPermission("yvtils.fusion.manage", "Manage Fusion recipes", default = false),
        PermissionManager.YVtilsPermission(
            "yvtils.fusion.flask.use",
            "Feed using XP points with a Nourishing Flask",
            default = true
        ),
        PermissionManager.YVtilsPermission(
            "yvtils.fusion.*", "All Fusion permissions", default = false,
            children = mapOf(
                "yvtils.fusion.use" to true,
                "yvtils.fusion.manage" to true,
                "yvtils.fusion.flask.use" to true
            )
        )
    )
}
