# Discord integration

Chat/console bridging, server statistics, whitelist registration, linked-account
management, Discord forms/buttons and Minecraft account-management GUIs.

- `DiscordYVtils.kt`: dynamic module lifecycle.
- `logic/AppLogic.kt`: asynchronous JDA connection and shutdown.
- `actions/`: slash commands, buttons, modals and select interactions.
- `logic/whitelist/`: UUID identity, serialized transactions and rollback.
- `logic/sync/`: chat, console and server-statistics synchronization.
- `commands/`, `gui/`: `/discordaccounts` and inventory management.
- `configs/`, `language/`, `utils/`: typed state, translations and external services.

Enable `discord` and configure `plugins/yvtils/discord/config.yml`. Setup, bot
permissions and acceptance checks: [Discord registration](../docs/discord-registration.md).
Verify with `./gradlew :discord:build`; live operations require a staging guild.
