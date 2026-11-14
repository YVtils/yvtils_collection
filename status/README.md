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

## Commands

- `/status` opens your status GUI; `/status <player>` opens another online player's GUI.
- `/status set <text>` and `/status default <preset>` change your own status.
- `/status <player> set <text>` sets another online player's custom status.
- `/status <player> default <preset>` selects a configured default for another player,
  with the same preset suggestions as the self command.
- `/status clear` clears your own status; `/status clear <player>` clears another player's.

Targeted set/default/clear commands also work from console. The player comes
**before** `set`/`default`, avoiding ambiguity with free-form status text. Existing
self command syntax and the `/prefix` and `/role` aliases remain unchanged.
Validation errors go to the command sender, successful changes notify both sender
and target in their own locales, and display/tab/team state is saved for the
target's UUID. Blacklisted input is rejected without clearing the existing status.

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
| `yvtils.status.command.manage.others` | Operators | `/status <player>` GUI and targeted set/default commands |
| `yvtils.status.command.clear.others` | Operators | `/status clear <player>` and clearing others through the GUI |
| `yvtils.status.config` | Operators | Edit module configuration |
| `yvtils.status.*` | Operators | All Status permissions |

GUI actions require their corresponding action permission as well as GUI access.
Clearing someone else's status in the GUI also requires `command.clear.others`.
Targeted set/default commands require `command.manage.others`, their corresponding
`command.set`/`command.default` action, and base `command` access. Clearing remains
separately gated by `command.clear.others` plus `command.clear`. Permissions are
rechecked at execution, and entering an empty status cannot bypass clear permission.

Verification includes authorization checks for self/other/console senders and
safe confirmation rendering. On Paper, test allowed/denied target commands, preset
completion, multi-word formatted text, sender/target locales, reconnect persistence,
and rejection of overlong/blacklisted input without changing the target's status.
