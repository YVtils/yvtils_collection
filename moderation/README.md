# Moderation

Bans/tempbans, kicks, mutes/tempmutes, warnings, unban/unmute and `/modgui`.
The GUI supports online/offline targets, reasons, duration input and confirmations.

- `ModerationYVtils.kt`: command/listener/config wiring and main-thread expiry cleanup.
- `commands/`, `logic/`: command registration and action execution.
- `gui/`: target selection and punishment management.
- `configs/saveFile/`: serialized mute/warning persistence.
- `listeners/AsyncChat.kt`: mute enforcement through concurrent read state.
- `utils/`: target/profile resolution, feedback and logging filters.

Enable `moderation`; configuration and saves live below `plugins/yvtils/moderation/`.
Action-specific permissions still apply inside the GUI. See
[administration settings](../docs/moderation-and-essentials.md).
Verify with `./gradlew :moderation:build` and live expiry/offline-target checks.
