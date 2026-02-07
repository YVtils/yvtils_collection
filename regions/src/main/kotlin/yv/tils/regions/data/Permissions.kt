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

enum class Permissions(val permission: String) {
    REGION_CREATE("yvtils.command.regions.create"),
    REGION_DELETE("yvtils.command.regions.delete"),
    REGION_INFO("yvtils.command.regions.info"),
    REGION_LIST("yvtils.command.regions.list.generic"),
    REGION_LIST_ROLE("yvtils.command.regions.list.role"),
    REGION_LIST_OTHER("yvtils.command.regions.list.other"),
    REGION_MEMBER("yvtils.command.regions.members.generic"),
    REGION_MEMBER_ROLE("yvtils.command.regions.members.role"),
    REGION_MEMBER_ADD("yvtils.command.regions.members.add"),
    REGION_MEMBER_REMOVE("yvtils.command.regions.members.remove"),
    REGION_FLAGS("yvtils.command.regions.flags.generic"),
    REGION_FLAGS_GLOBAL("yvtils.command.regions.flags.global"),
    REGION_FLAGS_ROLE("yvtils.command.regions.flags.role"),
    REGION_FLAGS_LOCKED("yvtils.command.regions.flags.locked"),
}