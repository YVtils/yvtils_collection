# Server presentation

Maintenance state/commands, configurable MOTD and server-list information,
displayed capacity, join/quit messages and shared maintenance status.

- `ServerYVtils.kt`: dynamic lifecycle and listener registration.
- `motd/`: server-list content generation/rendering.
- `connect/`, `listeners/`: connection messages and Paper ping events.
- `maintenance/`: runtime/persisted maintenance transitions.
- `configs/`, `language/`: typed settings, GUI and translations.

Enable `server`; configure `plugins/yvtils/server/config.yml`.
`listeners/PlayerLogin.kt` currently does not enforce maintenance admission.
Verify with `./gradlew :server:build` and server-list/connection checks.
