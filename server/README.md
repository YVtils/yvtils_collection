# Server presentation

Maintenance state/commands, configurable MOTD and server-list information,
displayed capacity, join/quit messages and shared maintenance status.

- `ServerYVtils.kt`: dynamic lifecycle and listener registration.
- `motd/`: server-list content generation/rendering.
- `connect/`, `listeners/`: connection messages and Paper ping events.
- `maintenance/`: runtime/persisted maintenance transitions.
- `configs/`, `language/`: typed settings, GUI and translations.

Enable `server`; configure `plugins/yvtils/server/config.yml`.
Verify with `./gradlew :server:build` and server-list/connection checks.

## Maintenance

- `/maintenance` or `/maintenance toggle` toggles maintenance mode;
  `/maintenance true` enables it and `/maintenance false` disables it.
- `yvtils.command.maintenance` grants command access;
  `yvtils.bypass.maintenance` allows joining and remaining online during maintenance.
  Both permissions default to operators.
- Enabling maintenance disconnects online players without bypass permission and
  rejects their subsequent logins with the translated maintenance message.
  Disabling it does not disconnect players. Existing ban/whitelist denials are preserved.
- `maintenance.enabled` in `plugins/yvtils/server/config.yml` is saved by commands
  and the configuration GUI, restored on startup, and shared with Discord server stats.
  GUI changes take effect immediately.

Verify on Paper with a bypass player and a regular player: enable maintenance,
attempt both logins, disable it, and restart once with maintenance enabled to
check persistence.

Real and Vanish-generated connection messages resolve `<player>` through the
optional Status module, preserving its configured status/name formatting. Without
Status or a saved status, the plain account name is used. `<playername>` always
provides the unformatted account name for custom templates. MiniMessage tag names
are case-insensitive, so existing `<playerName>` templates remain supported.
