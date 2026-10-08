# Server presentation and maintenance

Follow [root AGENTS.md](../AGENTS.md) for toolchain, dynamic-module publishing, and shared-runtime rules.

- `src/main/kotlin/yv/tils/server/ServerYVtils.kt` wires maintenance commands, join/quit messages, and Paper server-list pings.
- `motd/DisplayMOTD.kt` and `GenerateContent.kt` control server-list presentation; `connect/EventMessages.kt` handles connection messages. Use the typed `configs/ServerConfigState.kt` and existing derived config accessors.
- `maintenance/MaintenanceHandler.kt` coordinates its runtime flag, persisted `maintenance.enabled`, and shared `ServerUtils.serverInMaintenance`; keep all three aligned when changing state transitions.
- `listeners/PlayerLogin.kt` is currently empty; do not assume the registered login listener enforces maintenance admission.
- Verification from the repository root: `./gradlew :server:build`; use a server-list client and Paper players to verify ping rendering, connection messages, and maintenance transitions.
