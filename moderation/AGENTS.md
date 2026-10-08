# Moderation

Follow [root AGENTS.md](../AGENTS.md) for toolchain, dynamic-module publishing, and shared-runtime rules.

- `src/main/kotlin/yv/tils/moderation/ModerationYVtils.kt` registers bans, temporary bans, kicks, mutes, temporary mutes, unmute, and warnings. `ModGUICommand` is currently commented out.
- Initialize InvUI in enable before window use. Typed settings/config GUI live in `configs/`; mute and warning persistence live in `configs/saveFile/`, separate from Bukkit's ban state.
- Mutes are keyed by UUID. Existing expiry format uses epoch milliseconds encoded as a string and literal `"null"` for permanent mutes; preserve compatibility when changing persistence or cleanup.
- `listeners/AsyncChat.kt` enforces mutes; `utils/TargetUtils.kt` resolves profiles and removes expired records. The cleanup scheduler and save jobs use shared IO coroutines, so account for their interaction with chat access.
- Verification from the repository root: `./gradlew :moderation:build`; expiration, offline targets, ban behavior, and chat enforcement require Paper runtime checks.
