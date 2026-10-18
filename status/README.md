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
