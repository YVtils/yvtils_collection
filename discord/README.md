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

## Temporary connection failures

JDA 6.5.0 can still report routing/DNS/timeouts and gateway reconnect notices.
`connection/` replaces only recognized transient diagnostics emitted by this
module's JDA classloader with `[Discord] [Connection]` YVtils warnings. Repeated
notices are grouped at most once per minute per REST/gateway category. Gateway
resume/recreation reports recovery; this does not imply failed REST requests were
delivered. JDA's reconnect and request failure callbacks remain unchanged. Failed
REST operations are not blindly replayed, avoiding duplicate sends/account actions.

Unknown errors, SSL failures, authentication/intents errors and terminal gateway
diagnostics retain their original logs. Planned shutdown does not produce the
replacement outage notices. The filter is removed when the module stops.

Console forwarding excludes JDA and connection/console-sync diagnostics to avoid
feeding failures back into Discord. Failed forwarding enters a 60-second cooldown;
the waiting queue is capped at 1000 entries, so some outage-period output may be
dropped. Oversized lines are split to fit Discord's message limit. A send with an
uncertain result is dropped rather than replayed. Resetting history while a request
is in flight cannot restore the old buffer. Shutdown removes the installed appender,
stops forwarding/statistics workers and closes the owned HTTP client resources.

Discord connection announcements and public player counts exclude vanished players.
The configuration GUI edits a detached draft, rechecks management permission and stale
state when saving, and persists before applying changes to runtime configuration.

Network outages still need investigation if persistent: server routing,
firewall, DNS or Discord availability cannot be repaired by changing log wording.
