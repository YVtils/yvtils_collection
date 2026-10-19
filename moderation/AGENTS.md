# Moderation

Follow [root AGENTS.md](../AGENTS.md) for toolchain, dynamic-module publishing, and shared-runtime rules.

- `src/main/kotlin/yv/tils/moderation/ModerationYVtils.kt` registers bans, temporary bans, kicks, mutes, temporary mutes, unmute, warnings and `ModGUICommand`.
- Initialize InvUI in enable before window use. Typed settings/config GUI live in `configs/`; mute and warning persistence live in `configs/saveFile/`, separate from Bukkit's ban state.
- Mutes are keyed by UUID. Existing expiry format uses epoch milliseconds encoded as a string and literal `"null"` for permanent mutes; preserve compatibility when changing persistence or cleanup.
- `listeners/AsyncChat.kt` enforces mutes; `utils/TargetUtils.kt` resolves profiles and removes expired records. Cleanup and resolved profile actions run on the server thread; chat reads a concurrent map and never mutates expiry state.
- Verification from the repository root: `./gradlew :moderation:build`; expiration, offline targets, ban behavior, and chat enforcement require Paper runtime checks.
- ModGUI skin updates use the shared `yv.tils.gui.utils.PlayerHeads` UUID cache; resolve asynchronously, assign skull profiles and notify InvUI on the server thread. `ModerationText` preserves parsed color tags without interactive tags. `listeners/BannedLogin` customizes active profile-ban messages through Paper's connection validation API; never allow denied logins or extend expired bans.
