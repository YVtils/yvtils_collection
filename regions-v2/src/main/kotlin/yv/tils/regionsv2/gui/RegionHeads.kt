/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.regionsv2.gui

import yv.tils.regionsv2.logic.ClaimRole

/** Region-specific textures; shared navigation icons remain in the GUI module. */
enum class RegionHeads(val texture: String) {
    MEMBERS("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjliOTU3ODgyNTA0OGQyZTU0NWM4Y2M4MDQ5NWFjYzJjZGNmOTVlMzY2MjVmNmI1N2FiMTllMWUyYThiOWRhOCJ9fX0="),
    FLAGS("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmJiOThhMjE5YmE0YWY1MTMyMWE4NWRiZjVmZjgzN2M1NjdkODBmMTA2NWE4ZGIxYTJjZWNiMTI1ZTYyMzAyNyJ9fX0="),
    GLOBAL("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmM2MjExMGQ4MTg4NDQxZDIxNzk0NDM0ZjY3ZDEyYTAyMWI3NDAyYzhkYWE0MmQ0ZmVhMzIzZTdlMTllMGJiNyJ9fX0="),
    OWNER("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTFiMGE1YmJjNjk3YzBjNDJhNmNmMWI5YzRjNDQzNWIwNzMyMmZjZTViYjI3ZDgyYjY5MzA4NDNlNWFiN2EwOSJ9fX0="),
    MEMBER("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODg3MGFiOTgwYTgzYTQwNmMwMWJkYTUwNmRkNzlkMjUzYjdlMTkwMzEyYjM0NGQwMTVmNjE3MDg0M2UzOGY0ZCJ9fX0="),
    VISITOR("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjA0MWU0NjVkNWYwYWM4NDFiMWQyNmIwNDY3NGViNDJlMmE2NTkwYWFjZDQzMTc0NjE4NDliMjUyMjQ3NmJhYiJ9fX0=");

    companion object {
        fun role(role: ClaimRole?): RegionHeads = when (role) {
            null -> GLOBAL
            ClaimRole.OWNER -> OWNER
            ClaimRole.MEMBER -> MEMBER
            ClaimRole.VISITOR -> VISITOR
        }
    }
}
