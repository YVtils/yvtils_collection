# Server presentation and maintenance

Follow [root AGENTS.md](../AGENTS.md) for toolchain, dynamic-module publishing, and shared-runtime rules.

- `src/main/kotlin/yv/tils/server/ServerYVtils.kt` wires maintenance commands, join/quit messages, and Paper server-list pings.
- `motd/DisplayMOTD.kt` and `GenerateContent.kt` control server-list presentation; `connect/EventMessages.kt` handles connection messages. Use the typed `configs/ServerConfigState.kt` and existing derived config accessors.
- `maintenance/MaintenanceHandler.kt` coordinates its runtime flag, persisted `maintenance.enabled`, and shared `ServerUtils.serverInMaintenance`; keep all three aligned when changing state transitions.
- `listeners/PlayerLogin.kt` rejects otherwise-allowed logins during maintenance unless the player has `yvtils.bypass.maintenance`; preserve prior ban/whitelist denials. Config load, commands, and GUI saves synchronize runtime and shared maintenance state; only enabling maintenance kicks non-bypass players.
- Verification from the repository root: `./gradlew :server:build`; use a server-list client and Paper players to verify ping rendering, connection messages, and maintenance transitions.
- Shared `ConnectionMessages` hooks allow optional Vanish fake announcements without feature dependencies. The hidden predicate suppresses real connection broadcasts; register/clear the announcer in lifecycle and preserve the configured custom-message enabled switch.
