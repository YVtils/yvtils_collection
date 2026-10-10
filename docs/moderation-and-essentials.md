# Essentials and moderation utilities

Enable `essentials` and/or `moderation` in `plugins/yvtils/modules.yml`, publish
the matching module artifacts, and restart using the normal collection workflow.

## Anvils and chat

`plugins/yvtils/essentials/config.yml` includes:

```yaml
allowChatColors: true
disableTooExpensive: true
showAnvilCost: true
```

All three settings default to true, including existing configurations missing these
keys. Change the file and restart to apply it.

Alternatively, open `/yvtils config` and select **essentials**. The in-game editor
includes chat colors, anvil bypass/cost messages, and the nested Spawn Elytra
settings (enabled, worlds, radius, boost strength). It requires
`yvtils.essentials.config` (OP by default, included in `yvtils.essentials.*`) plus
access to the launcher's configuration command. Changes save when an edited screen
closes and take effect from the live configuration without a restart. Reopen an
already-open anvil after changing the bypass setting. Invalid radius/boost values,
revoked permissions and conflicting edits are rejected without changing live state.

- `disableTooExpensive` removes the anvil's maximum XP-cost cutoff using Paper's
  anvil-view API. It does not make operations free, change the calculated cost,
  permit incompatible enchantments, or raise enchantment-level limits.
- `showAnvilCost` reports real costs of 40+ levels and your current levels in chat
  while the bypass is enabled. Repeated identical updates in one open anvil are
  deduplicated. The vanilla client's anvil screen may still say Too Expensive;
  server-internal maximum-cost changes cannot change that client text. Creative
  players do not receive these messages.
- `allowChatColors` enables the existing MiniMessage chat formatting. False leaves
  messages to Paper's normal renderer, so typed tags remain literal. Enabled
  formatting also uses Paper's renderer and respects the event's recipients and
  subsequent cancellation by moderation or other plugins.

## Moderation inventory

Use `/modgui` for a paginated list of known players, with online players first.
`/modgui <player-name-or-UUID>` opens a player directly. Search accepts cached
player names and UUIDs; it does not perform synchronous remote name lookups.
Muted/warned offline players are included in the list.

The player details screen displays online state, ban/mute state, warning count,
and current mute reason/expiry. Refresh rebuilds the view from current data.
Warning history shows saved reasons, warning IDs, issuers and formatted timestamps.

Available actions are ban, temporary ban, kick, mute, temporary mute, warn,
unban and unmute. Each uses the existing moderation logic and announcement rules.
The GUI asks for a reason in an anvil input. Temporary actions also ask for a
positive duration such as `30 m`, `2 h` or `7 d`; units are `s`, `m`, `h`, `d`, `w`.
All actions have a final review/confirmation screen. Cancel returns to details.
Confirmation is single-use and rejects changed ban/mute state.

Permissions default to OP:

- `yvtils.moderation.command.modgui` permits the inventory and history.
- Each action also requires its existing `yvtils.moderation.command.<action>`
  permission. Unauthorized action buttons are omitted. Permissions are checked
  again when navigating, submitting input and confirming.
- `yvtils.moderation.*` includes the GUI and action permissions.

GUI messages are registered in English and German and follow the player's language.

## Runtime verification

After publishing locally and restarting a Paper test server:

1. Combine items costing at least 40 levels with enough XP: enabled bypass should
   allow the result and charge the displayed cost. Repeat with insufficient XP.
2. Disable the bypass and restart: the vanilla cutoff should return.
3. Send `<red>hello` with chat colors enabled and disabled. Check rendered colors
   versus literal tags, custom audiences, and that muted players are never broadcast.
4. Open `/modgui`, search a cached offline player and a UUID, inspect warning
   history, and navigate enough entries to exercise pagination.
5. Try each action against a test account, including `30 m` temporary actions;
   verify persisted mute/warning data and actual ban/kick/chat behavior.
6. Cancel at each input/confirmation stage; no action should be applied.
7. Revoke an action or GUI permission after opening its menu. Confirming must fail.
8. Change a target's ban/mute state externally while confirmation is open;
   confirmation must require another review. Double-click confirmation to check
   that a warning or punishment is applied only once.
9. Check English/German menus and a console attempt to use the player-only command.
