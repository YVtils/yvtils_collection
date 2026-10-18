# Moderation

Bans/tempbans, kicks, mutes/tempmutes, warnings, unban/unmute and `/modgui`.
The GUI supports online/offline targets, reasons, duration input and confirmations.

- `ModerationYVtils.kt`: command/listener/config wiring and main-thread expiry cleanup.
- `commands/`, `logic/`: command registration and action execution.
- `gui/`: target selection and punishment management.
- `gui/PlayerHeads.kt`: asynchronous UUID skin lookup, bounded cache and InvUI refresh.
- `gui/ModerationText.kt`: reason formatting and lore that preserves explicit colors.
- `configs/saveFile/`: serialized mute/warning persistence.
- `listeners/AsyncChat.kt`: mute enforcement through concurrent read state.
- `utils/`: target/profile resolution, feedback and logging filters.

Enable `moderation`; configuration and saves live below `plugins/yvtils/moderation/`.
Action-specific permissions still apply inside the GUI. See
[administration settings](../docs/moderation-and-essentials.md).
Verify with `./gradlew :moderation:build` and live expiry/offline-target checks.

The player browser has a gray-glass header and refresh in the bottom-right corner.
Review and warning reasons parse color/decoration tags such as `<white>` without
enabling interactive click tags. Profile lookup failures leave a usable default head.

Active profile bans use a styled YVtils disconnect message when joining, with reason,
issuer and permanent status or expiration. Customize
`moderation.ban.screen.permanent` / `.temporary` in the shared language files;
placeholders are `<reason>`, `<issuer>` and `<expires>`. Early login uses the server
language fallback. Bukkit remains authoritative for ban identity and expiry; IP bans
and non-ban denials retain their existing messages. Minecraft still uses its normal
disconnect screen container; the server customizes the message, not client screen widgets.
