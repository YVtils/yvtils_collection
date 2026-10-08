# Discord integration

Follow [root AGENTS.md](../AGENTS.md) for toolchain, dynamic-module publishing, and local testing.

- `src/main/kotlin/yv/tils/discord/DiscordYVtils.kt` loads configs and starts JDA through `logic/AppLogic`; slash-command registration occurs in late enable, shutdown stops the bot and unregisters `/discordaccounts`.
- This module bridges chat/events, syncs statistics, and manages Minecraft whitelist/account links. Read [registration and account-management semantics](../docs/discord-registration.md) before changing those flows.
- Registration, replacement, and unlink operations share the account transaction lock. Preserve UUID identity, stale-snapshot checks, whitelist rollback, and atomic save replacement; unreadable saves must not be overwritten.
- Network work runs on IO; Bukkit whitelist and inventory mutations run on Paper's server thread. Discord-role failure after an account commit is partial success, not a reason to roll back the saved link.
- Preserve existing `saves` wrapper and account field names in `discord/save.json`. Stateless registration buttons must work after restarts.
- Verification: `./gradlew :discord:build`; focused tests: `./gradlew :discord:test --tests 'yv.tils.discord.logic.whitelist.AccountConsistencyTest'`. Live interaction/role checks require a configured Paper server and Discord guild; see the linked acceptance checklist.
