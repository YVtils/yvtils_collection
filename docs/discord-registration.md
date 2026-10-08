# SMP registration and linked-account management (26.10.01)

## Registration panel

Configure the Discord module's `appToken`, `mainGuild`, and `whitelistFeature.roles`
(comma-separated role IDs). The plugin bot needs View Channel and Send Messages in
the panel channel, and Manage Roles with its highest role above **every** configured
whitelist role. Managed/integration roles cannot be assigned. Keep the existing
guild-member intent enabled in the Discord developer portal; the legacy message
flow also needs the message-content intent.

Run `/whitelist setup channel:<text channel>` in `mainGuild`. Runtime authorization
uses `commands.whitelistCommand.permission`, falling back to `MANAGE_CHANNEL` for
an invalid value, just like the whitelist command's default Discord permissions.
The private setup reply reports whether posting actually succeeded.

The panel has a large localized **Registrieren / Register** header and explains
the username modal, account link, whitelist access and role assignment. Supply
optional `tutorial_link` and/or `support_link` HTTP/HTTPS URLs, channel/thread IDs,
or channel mentions with `/whitelist setup`
to include a **Hilfe benötigt? / Help needed?** section. Only supplied links are
shown; when both options are omitted, the entire help section is omitted. For
Discord channel/thread IDs and mentions are displayed as native channel mentions
(`Tutorial: #channel-name`), so the destination is visible. Same-guild Discord
channel/thread URLs are also rendered as mentions; external URLs and links to
individual messages retain their clickable URL destination. Users still need
permission to view the target. The green registration button uses a furnace emoji,
a localized “Jetzt registrieren / Register now” label, and a prominent callout above
it, separated from the help section.

The button uses the bundled animated furnace GIF (`discord/emojis/registration-furnace.gif`)
as application emoji `yvtils_registration_furnace`. The bot uploads it asynchronously
at startup or reuses the existing emoji after a restart. Its namespace is separate
from `yv_` player heads, so player-emoji expiry/cleanup does not remove it. Until
initialization completes, or if upload fails (for example, the application's emoji
capacity is exhausted), new panels use the ⛏️ fallback. Repost a panel after the
emoji becomes available to update an already-posted fallback button. For example:

`/whitelist setup channel:#registration tutorial_link:https://example.org/tutorial support_link:https://discord.com/channels/<guild-id>/<channel-id>`

`/whitelist setup channel:#registration tutorial_link:123456789012345678 support_link:234567890123456789`

Repost the panel with setup to change its links; they are stored in the posted
message and need no in-memory session or extra configuration.

Users click **Registrieren** (Register in English) and enter their Minecraft Java
username in the modal. Names are trimmed and must match `[A-Za-z0-9_]{3,16}`.
The Discord/guild identities come from the interaction. Account existence is
checked when `whitelistFeature.settings.checkMinecraftAccount` is enabled; this
does **not** prove account ownership. Disabling that setting keeps the server's
existing offline-player resolution behavior.

The panel's `whitelist:register` ID is stateless and works after restarts. All
registration is restricted to `mainGuild`. Registration is available only while
the Minecraft server and Discord module/bot are running.

### Outcomes and consistency

- The same saved UUID is already registered, even after a name/case change.
- A linked Discord user can enter a different account through the same registration
  modal. The account is validated first, then a private Components V2 confirmation
  shows the previous and proposed accounts. Confirm within five minutes to replace
  it, or cancel to leave the old account untouched. Confirmation is bound to the
  submitting user/guild and the saved entry snapshot; concurrent changes reject the
  stale request. The proposed UUID is pinned to avoid switching to a different
  account after a name change. Expired/restarted confirmations require resubmitting
  the modal. No account is silently replaced. The legacy message-change prompt and
  administrative replacement commands also remain available.
- A Minecraft UUID/name linked to another Discord ID is rejected.
- Already-whitelisted accounts with **no** saved link are rejected. An administrator
  must reconcile that whitelist access before adding a link; this flow does not
  claim ownership of those accounts.
- UUIDs are the identity; saved names are snapshots from the last successful
  operation. Inspect renamed accounts using their UUID or Discord ID.
- All registration, replacement and unlink entry points share one coroutine lock.
  Network work runs on the IO dispatcher; Bukkit mutations and inventory actions
  run on the Paper server thread. This uses the repository's Paper scheduler,
  not a new Folia scheduling integration.
- Whitelist changes finish before persistence, with rollback on whitelist/save
  failure. A complete `saves` snapshot is written to a temporary file and atomically
  replaces `discord/save.json` before the in-memory snapshot changes. The filesystem
  must support atomic replacement. Unreadable save files disable mutation rather
  than overwriting existing data.
- Replacement removes the previous UUID's whitelist access in the same transaction.
  Removal/replacement kick the previous online player after the account commit.
- Discord role operations are awaited. Missing roles, hierarchy/permission failures,
  departed members and unavailable Discord produce **partial success**: the saved
  account/whitelist operation remains committed. The response and logs make this
  clear. Correct the Discord issue and reconcile roles administratively; repeating
  registration is not a role-repair mechanism. Empty configured role lists need no
  role update. Standalone `~name` admin whitelist entries need no Discord roles.
- Rollback errors are logged with the transaction failure. As with any two distinct
  stores, an abrupt process/host crash between whitelist and account persistence
  requires administrative reconciliation from backups.

## Legacy message registration

`whitelistFeature.settings.legacyMessageRegistration` defaults to **true**, including
older configurations missing the key. Set it to `false` to stop processing/deleting
username messages and creating message-based account-change prompts. Modal
registration and administrative commands remain available. Existing Discord
`/whitelist forceadd`, `/whitelist forceremove`, `/whitelist check`, replacement
buttons and removal selections use the same account service.

## Minecraft management

Commands use the repository's CommandAPI command-tree pattern. Text operations
work for both players and console. Completion uses saved names/UUIDs/Discord IDs
and permission checks, with no remote calls on the server thread.

| Command | Purpose |
| --- | --- |
| `/discordaccounts` | Localized help |
| `/discordaccounts list [page]` | List accounts, ten per page |
| `/discordaccounts inspect <entry>` | Show saved Minecraft name, UUID, Discord ID and cached display name if available |
| `/discordaccounts add <name-or-UUID> <Discord-ID>` | Explicit registration; never overwrite an existing link |
| `/discordaccounts remove <entry>` | Remove saved link, whitelist access, and configured Discord roles |
| `/discordaccounts replace <entry> <name-or-UUID>` | Explicitly replace the Minecraft account, retaining the Discord ID |
| `/discordaccounts gui` | Player-only paginated account inventory |
| `/discordaccounts gui <entry>` | Player-only details view |
| `/discordaccounts gui <entry> <replacement-name-or-UUID>` | Details with a replacement candidate; click Replace, then confirm |

`<entry>` accepts a saved Minecraft username (case-insensitive), UUID, or Discord
user ID. A linked entry must exist for removal/replacement. `add` requires a numeric
Discord snowflake ID; existing Discord force-add can still create `~name` entries.
Discord unavailability does not prevent saved-data inspection. Writes that complete
locally but cannot update roles report partial success, including removal.

The inventory follows the Regions module's shared `GuiStyle` palette: MAIN-colored
window titles, SECONDARY item names, TERTIARY wrapped lore, and no italic text.
Account list/details/confirmation items use saved-UUID player heads instead of
paper, preferring the online player's textured profile. Missing skins are resolved
using Paper's asynchronous profile update (15-second timeout) and cached for ten
minutes. Open inventory items refresh when textures arrive; failed lookups retain
a default player head without blocking the server thread.
Navigation uses the shared previous/next heads, hides unavailable controls, and
shows a page indicator. Check-mark/X heads mark confirmation/cancellation; toolbox
heads mark replacement/refresh. Localized lore explains clicks and destructive
actions, and compact list layouts expand with the number of accounts.

The inventory uses the existing dynamically selected InvUI GUI module (compile
API InvUI 2.1.1), `PagedGui`, `Gui`, `Item`, `Window`, filler items, and
`InvUIBootstrap`. Details include IDs in item lore. Remove and Replace require a
separate confirmation. Replace opens a Regions-style anvil input: enter a Minecraft
username or UUID, click the check-mark to validate it, then confirm the displayed
replacement. Validation does not change access; confirmation pins the validated
UUID and rechecks the saved entry. Cancel leaves the original account untouched.
The optional replacement command argument prefills the input. Refresh returns a
current snapshot. Entry state and permissions are
rechecked on click and again for the queued operation, so stale inventories cannot
remove or replace a changed account. Empty inventories display a localized empty
item. Player-language resolution and console server-language fallback come from
the existing `LanguageHandler`; all new text is registered in English and German.
Logs use the initiating Minecraft name/UUID or console name, never a fabricated
Discord user.

Minecraft command/GUI feedback uses the shared `<prefix>` and `Colors` palette:
colored headings, highlighted account fields, red errors, green completion, and
yellow partial success. Dedicated `discord.accounts.chat.*` language keys preserve
prefix-free Discord messages and GUI lore. Player languages and console fallback
still resolve through `LanguageHandler`.

Discord names prefer the member's server nickname, with an asynchronous REST lookup
when the member is missing from JDA's cache and a global Discord display-name
fallback for departed members. Requests are shared/cached for five minutes and
time out after fifteen seconds. Open lore refreshes automatically from a localized
loading state to the fetched name or unavailable fallback. `~name` whitelist-only
entries explicitly display that no Discord user is linked.

### Minecraft permissions

| Permission | Purpose | Default |
| --- | --- | --- |
| `yvtils.discord.accounts.read` | List and inspect links | OP |
| `yvtils.discord.accounts.gui` | Open/use inventories; also requires `accounts.read` | OP |
| `yvtils.discord.accounts.add` | Add links | OP |
| `yvtils.discord.accounts.remove` | Remove links, including confirmed GUI removal | OP |
| `yvtils.discord.accounts.replace` | Replace accounts, including confirmed GUI replacement | OP |

These are registered through the existing `PermissionManager` (`default=false`
maps to Bukkit OP). `yvtils.discord.*` includes them. Command branches/completion
and runtime actions enforce operation-specific permissions. Console follows Bukkit
sender permissions and receives a localized player-only response for GUI commands.

## Migrating from a standalone registration bot

1. Stop conflicting registrations and back up the plugin's `discord/save.json`
   under its data directory and Minecraft's `whitelist.json`.
2. Deploy the updated core/plugin and Discord module version **26.10.01**. With
   dynamic modules, make the matching Discord artifact available in the configured
   module repository before deploying the new loader metadata.
3. Configure `mainGuild` and `whitelistFeature.roles`. Include the role previously
   assigned by the standalone bot if appropriate; no extra pre-registration role
   is needed.
4. Grant the plugin bot the channel and role permissions and role hierarchy above.
5. Post a **new** panel with the plugin bot using `/whitelist setup`.
6. Test a fresh registration, saved UUID/Discord link, whitelist access, roles,
   and panel usability after a server restart.
7. Remove the old standalone bot's registration panel and disable its registration
   handler. Its old message cannot be reused: interactions belong to the application
   that posted the message.
8. When ready, disable `whitelistFeature.settings.legacyMessageRegistration`.

Existing saved links need **no migration**: the `saves` wrapper and
`discordUserID`, `minecraftName`, `minecraftUUID` fields are preserved. Retiring
unrelated standalone-bot features is outside this change.

## Manual acceptance checklist

Use a staging guild/server and fresh Java accounts. Restore backups after failure
injection. These checks need live Discord/Paper and are not covered by unit tests.

- [ ] `/whitelist setup` succeeds for the configured permission and acknowledges
  privately. Denied users and commands in another guild cannot post panels.
- [ ] Remove Send Messages/View Channel; setup reports posting failure accurately.
- [ ] Button opens a modal immediately; submission is deferred privately. Test
  short/long names, invalid characters, surrounding whitespace and case changes.
- [ ] Valid account gets exactly one saved UUID/Discord link, actual whitelist
  access and every configured role before complete success is reported.
- [ ] Nonexistent account is rejected with validation enabled. Test enabled and
  disabled account validation against the server's supported online/offline mode.
- [ ] Same linked UUID reports already registered, including after a name change.
  Different account for that Discord ID gives a private confirmation. Verify cancel,
  confirm, five-minute expiry, duplicate clicks and confirming after another action
  changes the link. Only the submitting user can execute the confirmation, and
  old whitelist access is removed only after confirmation succeeds.
- [ ] Account linked to another Discord ID is rejected without modifying either
  entry. Unlinked preexisting whitelist access is explicitly rejected.
- [ ] Race two modal submissions for the same Minecraft account; race registration
  against Minecraft add/remove/replace and existing Discord administrative actions.
  Confirm no overwrites and only one owner; later requests still work after errors.
- [ ] Inject whitelist setter failure and unwritable save-directory/atomic-move
  failure. No false success; previous account/access retained after rollback.
  A malformed existing save disables writes without replacing that file.
- [ ] Remove Manage Roles, move a role above the bot, configure a missing/managed
  role, and make a member leave during registration. Each yields explicit partial
  success, retained saved link/whitelist, and logs identifying the actor.
- [ ] Restart and use the existing plugin panel; existing links persist and duplicate
  registrations are still rejected.
- [ ] Enable/disable legacy message registration; only the enabled mode consumes
  usernames/prompts changes. Modal/admin entry points work in both modes.
- [ ] Check Minecraft command registration, help, argument usage, cached completion
  and all text operations from console and players; GUI from console is player-only.
- [ ] Independently deny/readmit each Minecraft permission. Verify command branches,
  completion, GUI entry and action permissions, including permission revocation
  between opening a confirmation and clicking it.
- [ ] List multiple pages, inspect by saved name/UUID/Discord ID, add, remove and
  replace from Minecraft. Replacement removes old whitelist access and removal
  removes roles. Existing links cannot be silently overwritten.
- [ ] GUI: empty list, pagination, details/ID lore, refresh, cancel and confirmed
  removal/replacement. Modify an entry while its details/confirmation is open;
  stale actions must fail and leave the changed link intact.
- [ ] Replacement opens an anvil without requiring a command. Test username/UUID,
  invalid/nonexistent/taken accounts, validation failure retry, cancel, permissions
  revoked during lookup, and changing the entry before confirmation. Check offline
  player skins load and refresh after lookup; skin-service failure leaves a usable
  default head. Check prefixed/color-coded player and console feedback.
- [ ] Stop/disconnect Discord: saved-data list/inspect remains usable with fallback
  names, and locally successful writes report role failure rather than total success.
- [ ] Set players to German and English; verify commands, titles, item lore and
  confirmations follow player language, and console uses the server-language fallback.
- [ ] Confirm core plugin metadata and Discord module version both report `26.10.01`.

## Automated checks

`./gradlew :discord:build :core:build check` runs module/core builds and repository
checks. Focused Discord tests cover UUID/name duplicate decisions, stale and
conflicting replacement, concurrent ownership, rollback/released locks after
persistence failure, and preservation of committed links after role failure.
Plugin metadata is expanded by core resource processing; module versions and loader
coordinates are generated from `gradle/module-versions.properties`. Other modules
keep their independent release versions.
