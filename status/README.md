# Player status

Player-written status prefixes displayed in chat names, tab-list names and
scoreboard teams. `/status set`, `/status default`, `/status clear` and the
management GUI update saved UUID-keyed text together.

- `StatusYVtils.kt`: lifecycle and player listeners.
- `commands/`, `logic/`: command validation and management GUI.
- `utils/StatusUtils.kt`: display/tab/team updates.
- `configs/`: typed defaults/limits, GUI and persisted player status.
- `language/`: translated feedback.

Enable `status`; files live below `plugins/yvtils/status/`.
Verify with `./gradlew :status:build`; check reconnects and scoreboard interaction.
The shared connection-message display hook reads saved status text for Server and
Vanish announcements, including before the join listener initializes display names.

## Permissions

Permissions are registered through the shared `PermissionManager`, like other modules.
`/prefix` and `/role` use the same permissions as `/status`.

| Permission | Default | Access |
| --- | --- | --- |
| `yvtils.status.command` | Everyone | `/status` command access |
| `yvtils.status.command.manage.self` | Everyone | Open your status GUI |
| `yvtils.status.command.set` | Everyone | Set custom text through commands or the GUI |
| `yvtils.status.command.default` | Everyone | Select default statuses through commands or the GUI |
| `yvtils.status.command.clear` | Everyone | Clear your status through commands or the GUI |
| `yvtils.status.command.manage.others` | Operators | `/status <player>` GUI |
| `yvtils.status.command.clear.others` | Operators | `/status clear <player>` and clearing others through the GUI |
| `yvtils.status.config` | Operators | Edit module configuration |
| `yvtils.status.*` | Operators | All Status permissions |

GUI actions require their corresponding action permission as well as GUI access.
Clearing someone else's status in the GUI also requires `command.clear.others`.
