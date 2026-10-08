# Player status display

Follow [root AGENTS.md](../AGENTS.md) for toolchain, dynamic-module publishing, and shared-runtime rules.

- `src/main/kotlin/yv/tils/status/StatusYVtils.kt` wires `/status`, player join/quit handling, translations, InvUI setup, settings, and saves.
- This is player-written status text displayed with names, not potion effects. `utils/StatusUtils.kt` updates display name, tab-list name, scoreboard team prefix, and UUID-keyed saved status together.
- `logic/StatusHandler.kt` checks visible length after stripping formatting and validates default suggestions. Clearing must also remove the team and persisted status.
- `configs/ConfigFile.kt` keeps typed `StatusConfigState` as the source of truth; GUI changes use `applyState` to resync the flattened compatibility map. `SaveFile.kt` owns player state separately.
- Verification from the repository root: `./gradlew :status:build`; check name/tab/team rendering and reconnect persistence on Paper.
