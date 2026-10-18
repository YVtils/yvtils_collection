# Waypoints

Follow [root AGENTS.md](../AGENTS.md) for toolchain and new-module wiring.

- `WaypointYVtils` wires lifecycle; `commands/`, `config/`, `language/`, `permissions/`, `listeners/`, `logic/` separate implementation responsibilities.
- Public points are listed globally; private points are owner-only; unlisted points are navigable by name but listed only to their owner.
- `gui/WaypointsGui.kt` uses InvUI pagination, anvil input and delete confirmation. Admin browsing requires `.admin`; config editing requires `.config`. Recheck permission and stale record on every mutation. Config GUI edits a detached draft, validates and atomically saves before applying.
- Names are globally unique and case-insensitive. Preserve world/owner UUID identity. Navigation owns private non-persistent BlockDisplays; never modify real blocks or force-load chunks. Cleanup on stop, arrival, death, world change, quit and disable.
- Verify with `./gradlew :waypoint:build` and the staging checks in `docs/breaking-release.md`.
