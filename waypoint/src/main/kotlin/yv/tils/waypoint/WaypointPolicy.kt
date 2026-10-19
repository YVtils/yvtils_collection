package yv.tils.waypoint

import java.util.UUID

object WaypointPolicy {
    fun listed(owner: UUID, viewer: UUID, visibility: Visibility, sharedWith: Set<UUID> = emptySet()) =
        owner == viewer || visibility == Visibility.PUBLIC || visibility == Visibility.SHARED && viewer in sharedWith

    fun accessible(owner: UUID, viewer: UUID, visibility: Visibility, sharedWith: Set<UUID> = emptySet()) =
        owner == viewer || visibility in setOf(Visibility.PUBLIC, Visibility.UNLISTED) ||
                visibility == Visibility.SHARED && viewer in sharedWith
}
