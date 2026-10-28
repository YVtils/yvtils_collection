# Statistics sent to the YVtils API

This document lists the complete **built-in usage-telemetry inventory** currently
collected by the `stats` module and participating YVtils modules, including request
metadata and exact metric keys. Additional metrics registered by addons or custom
API consumers are covered under [Custom metrics](#custom-metrics).

This is separate from the [`metrics` module](metrics.md), which stores gameplay and
server-health statistics locally. Its database, sessions, Favorites and leaderboards
are not uploaded to the YVtils API.

## When information is sent

- Destination: **`POST https://api.yvtils.net/stats`**, using HTTPS and UTF-8 JSON.
- The `stats` module must be enabled and `opt_in` must be explicitly `true`.
  Enabling `stats` in `modules.yml` alone does not accept telemetry.
- Administrators can use `/yvtilsstats`, `/yvtils config stats`, or
  `/yvtilsstats optin accept`. Withdraw with `/yvtilsstats optin decline`.
- The scheduler waits one minute after activation, then pushes hourly after a
  successful request. Failed requests retry after 1, 2, 4, 8, 16, 32, then at most
  60 minutes. A manual API push still requires consent and an enabled stats module.
- Built-in usage events while stats is unavailable, disabled or opted out are
  discarded, not buffered for later upload. Withdrawal stops collection and push,
  disconnects an active request and clears in-memory statistics. Already transmitted
  information cannot be recalled.

See the [stats README](../stats/README.md) for configuration, permissions, API usage
and deployment instructions.

## Request metadata

These fields accompany the metric entries in every push:

| JSON field | Type | Value sent |
| --- | --- | --- |
| `serverId` | String | Random installation UUID, generated on first acceptance and persisted as `server_id`. Reused across restarts, upgrades, renames and withdrawal/re-acceptance; not derived from world seeds, names or IP addresses. |
| `metadata.collectTimestamp` | Integer | Time the push payload was collected, as Unix epoch **milliseconds**. |
| `metadata.playerCount` | Integer or `null` | Most recently sampled online-player count. `null` when `metadata.collect_player_count` is disabled or no sample is available. |
| `metadata.region` | `null` | Reserved compatibility field. The implementation does not perform geolocation. |
| `metadata.serverName` | String | The administrator-configured `metadata.server_name`, sent verbatim. Empty by default. |
| `metadata.serverVersion` | String | Cached Paper/Bukkit version string, or `Unknown` if unavailable. |
| `metadata.yvtilsVersion` | String | YVtils core launcher version. |
| `stats` | Array | Registered metric entries described below. |

The installation UUID is also sent in the **`X-Server-ID`** HTTP header.
The request uses `User-Agent: YVtils-Stats/1.0`,
`Content-Type: application/json; charset=UTF-8` and `Accept: application/json`.
Redirects are not followed.

The receiving service necessarily sees the connection's public IP address even
though no IP field is placed in the JSON payload. A configured server name may
itself be identifying; leave it empty if that is not desired. This inventory does
not make claims about the receiving service's storage or retention policy.

## Fields attached to every metric

Each object in the `stats` array contains:

| Field | Meaning |
| --- | --- |
| `key` | Exact metric identifier, as listed below. |
| `type` | `COUNTER`, `GAUGE`, `STRING`, `LIST`, `TIMESTAMP` or `HISTOGRAM`. Internal `STRING_LIST` is mapped to `LIST` for the API. |
| `help` | Human-readable description of the metric. Built-in descriptions contain no player data. |
| `lastUpdated` | Last-update time for the entry, as Unix epoch milliseconds. For refreshed module metadata, this is not the module's original enable time. |
| `value` | Typed value object. Built-in counters and integer gauges use `longValue`; strings use `stringValue`; lists use `listValue`. |

The value object also supports `doubleValue` and `histogramValue` for custom
metrics. Unused value fields are normally `null`; an unused `doubleValue` is omitted
for compatibility. The built-in inventory below does not create histogram or
decimal-gauge metrics.

## Global statistics

These keys are not module-prefixed:

| Exact key | API type | What is tracked and when |
| --- | --- | --- |
| `server_version` | `STRING` | Paper/Bukkit version, captured when default metrics are registered. Also used for `metadata.serverVersion`. |
| `used_yvtils_plugins` | `LIST` | Sorted names from the loaded-module registry. Refreshed when default metrics are registered and during late enable. Names, not configuration contents, are sent. Core and GUI have separate metadata below and are not necessarily entries in this registry list. |
| `player_count` | `GAUGE` | Online-player count sampled on registration and every 1,200 server ticks, approximately one minute at 20 TPS. Only collected when `metadata.collect_player_count` is enabled. Disabling it also filters this key from the push payload. |
| `active_module` | `STRING` | Compatibility field for API consumers to identify an active/last-used module. It defaults to an empty string; the current built-in implementation registers it but does **not** automatically update it when features are used. |

Disabling player-count collection does not disable aggregate feature-usage counters.

## Metadata for each loaded module

Every registered, currently loaded module publishes these two keys under its prefix:

| Key suffix | API type | Value |
| --- | --- | --- |
| `enabled` | `GAUGE` | Integer `1`, indicating the module is currently registered as loaded/enabled. Unloaded modules have no entries rather than an `enabled: 0` entry. |
| `version` | `STRING` | Loaded module's version string. |

Metadata is refreshed after acceptance, during late enable and before exports.
Modules loaded before stats activation therefore appear without reconstructing any
earlier usage events. Core and the selected GUI library register their metadata
separately from ordinary feature modules.

### Exact module prefixes

Append `enabled`, `version`, or a module's counter suffix to the following prefix:

| Runtime module | Exact prefix |
| --- | --- |
| `core` | `yvtils_module_core__` |
| `common` | `yvtils_module_common__` |
| `config` | `yvtils_module_config__` |
| `utils` | `yvtils_module_utils__` |
| `gui` | `yvtils_module_gui__` |
| `vanish` | `yvtils_module_vanish__` |
| `waypoint` | `yvtils_module_waypoint__` |
| `fusion` | `yvtils_module_fusion__` |
| `discord` | `yvtils_module_discord__` |
| `multiMine` | `yvtils_module_multiMine__` |
| `essentials` | `yvtils_module_essentials__` |
| `sit` | `yvtils_module_sit__` |
| `status` | `yvtils_module_status__` |
| `server` | `yvtils_module_server__` |
| `message` | `yvtils_module_message__` |
| `moderation` | `yvtils_module_moderation__` |
| `regions` | `yvtils_module_regions__` |
| `remade-ender-dragon` | `yvtils_module_remade_hender_hdragon__` |
| `yv_smp` | `yvtils_module_yv_usmp__` |
| `stats` | `yvtils_module_stats__` |
| `metrics` | `yvtils_module_metrics__` |

The `gui` metadata represents the **one** selected `gui-26.1`, `gui-26.2` or
`gui-26.3` build, not three simultaneously active GUI modules. The `yv_smp` runtime
module is published/configured as the `yv-smp` artifact/module key. Preserve the
case of `multiMine`.

The prefix escapes `_` as `_u`, `-` as `_h` and `.` as `_d` in module identifiers,
then adds `__` before the metric suffix. This avoids collisions between module
names such as `a_b`, `a-b` and `a.b`.

## All built-in usage counters

All **27** entries below are `COUNTER` metrics with integer `longValue` values.
Unless stated otherwise, each qualifying event adds **one**. A counter may be
absent until its first event; absence is not evidence that an action occurred.

| Exact metric key | What increments it |
| --- | --- |
| `yvtils_module_core__info_views_total` | Plugin-information requests handled by the core information handler. |
| `yvtils_module_core__config_browser_opens_total` | Successful opens of the general configuration browser. Direct module shortcuts do not open this browser. |
| `yvtils_module_core__module_browser_opens_total` | Successful opens of the module-management browser. |
| `yvtils_module_common__locale_changes_total` | Handled player locale-change events. Neither the player nor the locale value is included. |
| `yvtils_module_config__file_loads_total` | Successful low-level configuration file loads. No path, filename or contents are included. |
| `yvtils_module_config__file_saves_total` | Successful atomic configuration file saves. No path, filename or contents are included. |
| `yvtils_module_utils__tasks_scheduled_total` | Successfully scheduled tasks in the shared coroutine scheduler, including one-shot tasks. No task names are included. |
| `yvtils_module_gui__config_editor_opens_total` | Initial opens of the shared typed configuration editor, not every nested page. This is not a counter for every arbitrary InvUI window. |
| `yvtils_module_vanish__toggles_total` | Successful vanish-state toggles. No identities, selected layers or current vanished-player count are sent. |
| `yvtils_module_waypoint__navigations_started_total` | New waypoint-navigation runs. Toggling the same active destination off does not count as a start. No destination names or coordinates are included. |
| `yvtils_module_fusion__crafts_total` | Committed recipe repetitions. A successful batch adds its repetition count, not just one and not the number of output items. Recipe and item identifiers are excluded. |
| `yvtils_module_discord__discord_to_minecraft_messages_total` | Eligible Discord chat messages forwarded through the Minecraft broadcast path. No message text, author, guild or channel identifiers are included. |
| `yvtils_module_discord__minecraft_to_discord_messages_total` | Synced Minecraft chat messages acknowledged as delivered by Discord's send callback. Failed sends are excluded; no message content or identities are included. |
| `yvtils_module_multiMine__mining_runs_requested_total` | Connected-block mining searches requested after the feature's permission, toggle, tool, block, cooldown and gameplay eligibility checks. This does not claim that extra blocks were successfully broken. |
| `yvtils_module_essentials__inventory_views_total` | Successfully opened inventory-inspection windows. Target identities and inventory contents are excluded. |
| `yvtils_module_sit__sessions_started_total` | Successful player mounts on a seat. Rejected seating attempts are excluded. |
| `yvtils_module_status__status_updates_total` | Successful status assignments through the status-setting handler. User-written status text is excluded; clearing a status is not counted by this hook. |
| `yvtils_module_server__maintenance_changes_total` | Live maintenance-state transitions through the announcing path. Unchanged values and startup state restoration are excluded. The configured maintenance state is not itself uploaded as a metric. |
| `yvtils_module_message__message_deliveries_total` | Private messages delivered to another recipient. Multiple recipients count separately. Self-notes and rejected deliveries are excluded; no text or identities are included. |
| `yvtils_module_moderation__actions_total` | Moderation actions reaching the completed-action notification hook, including silent actions. There is no per-target, reason, duration, case or action-type breakdown. |
| `yvtils_module_regions__claims_created_total` | Successfully committed claim creations, after persistence/payment handling. Failed or rolled-back transactions are excluded. No claim name, owner or geometry is included. |
| `yvtils_module_regions__claims_deleted_total` | Successfully committed claim deletions. Failed or rolled-back deletions are excluded; no claim identifiers are included. |
| `yvtils_module_remade_hender_hdragon__encounters_started_total` | Started custom dragon encounters. Previews and requests returning an already-running encounter are excluded. No world or player information is included. |
| `yvtils_module_remade_hender_hdragon__encounters_completed_total` | Tracked custom encounters finishing with the dragon dead or in its dying phase. Previews, manual stops and non-death cleanup are excluded. No world or player information is included. |
| `yvtils_module_yv_usmp__audio_loads_total` | Successful audio-load callbacks. No source URL, filename or listener identities are included. |
| `yvtils_module_stats__config_opens_total` | Opens of the stats administration menu after its permission check. Opening the nested settings editor is covered separately by the shared GUI editor counter. |
| `yvtils_module_stats__push_successes_total` | Successfully completed, still-consented telemetry pushes. The current successful push becomes visible in the **next** payload because its payload was already built. |

### The metrics module is a deliberate exception

`metrics` contributes only:

- `yvtils_module_metrics__enabled`
- `yvtils_module_metrics__version`

Its gameplay totals, server-health samples, UUIDs/names, sessions, daily history,
Favorites, leaderboards, SQLite contents and local HTTP scrape contents are not
copied into this telemetry stream. Its local operation remains independent of
stats consent. See [the metrics documentation](metrics.md) for its separate data
inventory and privacy controls.

## Counter lifetime and completeness

- Counters are **cumulative in-memory values** during the current consented
  collection period, not per-push deltas or persistent lifetime totals. The receiving
  service must not treat repeated snapshots as additional event counts.
- Exporting or pushing does not reset or increment feature counters. Restarting,
  withdrawing consent, or unloading the owning module clears its tracked values.
- Accepting after startup publishes current module metadata but does not import
  earlier events from gameplay saves, chat history, moderation records or databases.
- Events during backend downtime/disable or opt-out are not retained or replayed.
  Re-acceptance starts new counters.
- Only loaded modules and implemented event hooks contribute entries. This is not
  a record of every command, every Bukkit event or every feature interaction.
- Registration and list limits apply. `max_stats_count` can prevent new metric
  entries from being created; `max_list_size` can truncate list values. Type/name
  conflicts or best-effort publication failures can also omit an entry or update.

## Custom metrics

The inventory above is complete for the built-in collectors, but it is **not a
remote-upload allowlist**. The push payload contains the shared registry's entries,
including additional metrics registered through `ModuleStats`/`StatsApi` or directly
through `StatsRegistry`. The explicit player-count opt-out filter still applies to
the global `player_count` key.

Custom metrics can add the supported types:

| API type | Value field / meaning |
| --- | --- |
| `COUNTER` | `longValue`: non-decreasing integer count. |
| `GAUGE` | `longValue` or `doubleValue`: integer or finite decimal value. |
| `STRING` | `stringValue`: caller-provided string. |
| `LIST` | `listValue`: bounded list of caller-provided strings. |
| `TIMESTAMP` | `longValue`: timestamp, conventionally Unix epoch milliseconds. |
| `HISTOGRAM` | `histogramValue`: observation `count` and `sum`; current registry-generated `buckets` are empty and pushed `min`/`max` are `null`. No finite-bucket distribution or percentile collection is implemented. |

Custom authors are responsible for avoiding personal data and unbounded keys.
The generic publishing API does not redact caller-supplied strings or infer whether
they contain personal information. Built-in hooks instead use a fixed event enum
and numeric amounts, without passing user input to the registry.

## Information not collected by the built-in telemetry

- Player names, player UUIDs or per-player activity histories.
- Command arguments, chat/private-message/status text or moderation reasons.
- Discord user, guild and channel identifiers or message contents.
- Inventory contents, item metadata, recipe identifiers or audio source URLs/paths.
- World names, world UUIDs, world seeds, waypoint destinations or claim geometry.
- The metrics module's stored gameplay statistics and identifying exports.
- Geolocation, arbitrary configuration-file contents or task names.

The random **installation** UUID, optional configured server name, version strings,
aggregate player count when enabled, timestamps and connection IP visibility are
documented above and must not be confused with a guarantee of absolute anonymity.

## Implementation references

- [Built-in event catalog and module metadata](../utils/src/main/java/yv/tils/utils/stats/ModuleTelemetry.java)
- [Default global statistics and consent lifecycle](../stats/src/main/kotlin/yv/tils/stats/StatsYVtils.kt)
- [Module namespace escaping and registry adapter](../stats/src/main/kotlin/yv/tils/stats/api/RegistryStatsBackend.kt)
- [Actual push schema, payload construction and HTTP headers](../stats/src/main/kotlin/yv/tils/stats/logic/StatsPusher.kt)
- [Publishing API and operator setup](../stats/README.md)
