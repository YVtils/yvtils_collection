# Sitting

Follow [root AGENTS.md](../AGENTS.md) for toolchain, dynamic-module publishing, and local Paper testing.

- `src/main/kotlin/yv/tils/sit/SitYVtils.kt` wires `/sit` and dismount/quit listeners; there is no config or language-registration lifecycle here.
- `logic/SitManager.kt` mounts players on invisible, invulnerable, gravity-free armor stands and tracks sitting UUIDs. Standing removes the stand, adjusts position, and clears the tracked UUID.
- `listeners/EntityDismount.kt` and `PlayerQuit.kt` drive cleanup. Keep entity ownership/tracking and seat-height offsets consistent; stale UUIDs can prevent later sitting.
- Verification from the repository root: `./gradlew :sit:build`; seating, dismounting, and quitting require Paper entity checks.
