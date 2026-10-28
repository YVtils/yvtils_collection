# Statistics API

Opt-in **YVtils usage telemetry**, with a thread-safe metric API, Prometheus/JSON
exports and HTTPS push to `https://api.yvtils.net/stats`. This is separate from
[`metrics`](../metrics/README.md), which collects gameplay/server-health statistics
and exposes a local HTTP endpoint. `stats` does **not** start an HTTP server.

## Configuration and consent

Enable `stats` in `plugins/yvtils/modules.yml`, then restart. The generated
configuration lives at `plugins/yvtils/stats/config.yml`:

```yaml
opt_in: null
server_id: ''
metadata:
  server_name: ''
  collect_player_count: true
max_list_size: 1000
max_stats_count: 10000
```

- `opt_in: null` means undecided, `false` declines, and `true` accepts. Default
  metrics and remote push remain off until acceptance. File edits require a restart;
  the config GUI and `StatsYVtils.markOptIn(...)` apply live.
- Acceptance is persisted before telemetry starts. Withdrawal stops the collector
  and push scheduler, disconnects any active HTTP request, and clears in-memory
  metrics. Data already transmitted cannot be recalled. If saving withdrawal fails,
  it still takes effect in memory, but the reported storage error must be fixed
  before restarting to ensure it persists.
- `server_id` is a random installation UUID generated on first acceptance and reused
  across restarts, upgrades, renames and withdrawal/re-acceptance. It is not derived
  from world seeds, IP addresses or server names. Do not copy it between servers.
- Default metrics are server version, loaded YVtils modules, an active-module field
  available to API consumers, and optional player count sampled every 1,200 ticks
  on the server thread (about a minute at 20 TPS). Push metadata includes plugin
  version. Every loaded module also publishes its enabled/version metadata and the
  aggregate usage counters documented below. Disabling player count removes that
  count from push metadata and entries; it does not disable anonymous usage counters.
- Player names, player UUIDs, world names and world seeds are not collected by
  default. The receiver necessarily sees the connection IP. A configured server
  name is sent verbatim. Custom metric authors must not register personal data.
- List and registration limits are enforced, including concurrent registrations.
  Values are clamped to `1..1000` and `1..10000`, respectively. Lowering a list limit
  truncates existing lists; lowering the metric limit does not discard existing
  registrations, but blocks new keys until below the limit.

## In-game administration

- `/yvtilsstats` or `/yvtilsstats config`: opens the InvUI settings/consent menu.
- `/yvtilsstats optin accept`: explicitly accept and persist consent immediately.
- `/yvtilsstats optin decline`: decline or withdraw and persist immediately.
- `/yvtils config`: existing general configuration browser for all loaded modules.
- `/yvtils config stats`: direct shortcut to the same stats menu (the core shortcut
  supports any enabled module with a registered configuration GUI).

Operators can administer stats by default. Delegated admins can use
`yvtils.stats.manage` or the existing `yvtils.stats.optin` permission with
`/yvtilsstats`; the general `/yvtils config` command additionally requires
`yvtils.common.command.yvtils`. Permissions are rechecked on clicks and saves.
The menu has explicit accept and decline/withdraw buttons plus a general settings
editor for metadata and safety limits. Consent is not an implicit boolean toggle
inside that editor, and a stale settings draft cannot overwrite another admin's
consent decision. Changes are saved and applied without restarting.

While consent is undecided, only the first operator joining that boot receives the
localized privacy explanation and clickable accept/decline/settings actions. A
saved acceptance or rejection suppresses this prompt. If no decision is made, it can
appear again on the next boot. Each boot also logs one concise stats status line
with the in-game configuration command; routine setup logging is debug-only.

The dedicated command is `/yvtilsstats`, not `/yvtils stats`. Unsupported old sample
keys `metadata.region` and `auto_save_interval_seconds` have been removed: there is
no geolocation or automatic metric persistence. Consent and installation ID, unlike
metric values, are persisted.

## Built-in collection across modules

With `stats` enabled **and** consent accepted, every loaded module publishes
`yvtils_module_<escaped-module>__enabled` (`1`) and `...__version` (string).
Inactive/unloaded modules have no entries, rather than a misleading enabled flag.
The core launcher and the one selected GUI runtime are included even though they
are not ordinary dynamically toggled modules. Metadata is refreshed on acceptance,
late enable and exports, so enabling stats last or accepting after startup works.

Feature actions additionally publish the following fixed counters. In this table,
names are the suffix after `yvtils_module_<escaped-module>__`:

| Module | Counters / meaning |
| --- | --- |
| `core` | `info_views_total`, `config_browser_opens_total`, `module_browser_opens_total`: information requests and opened management browsers |
| `common` | `locale_changes_total`: locale-change events handled, never the locale value or player identity |
| `config` | `file_loads_total`, `file_saves_total`: successful low-level file loads/atomic saves, never paths or contents |
| `utils` | `tasks_scheduled_total`: successfully scheduled shared coroutine tasks, never task names |
| `gui` | `config_editor_opens_total`: initial typed configuration editor opens, not each nested page |
| `vanish` | `toggles_total`: successful vanish toggles, not player identities/layers or current vanished-player counts |
| `waypoint` | `navigations_started_total`: navigation starts, never waypoint names, worlds or coordinates |
| `fusion` | `crafts_total`: committed recipe repetitions (batch size), never recipe IDs or item data |
| `discord` | `minecraft_to_discord_messages_total`, `discord_to_minecraft_messages_total`: delivered outbound / forwarded inbound chat messages, never text, users, guilds or channels |
| `multiMine` | `mining_runs_requested_total`: connected-block searches requested after feature eligibility checks; not a claim that extra blocks were broken |
| `essentials` | `inventory_views_total`: successfully opened inspection windows, never target identities or inventory contents |
| `sit` | `sessions_started_total`: successful seat mounts, not rejected attempts |
| `status` | `status_updates_total`: successful status assignments, never user-written text |
| `server` | `maintenance_changes_total`: live announced maintenance transitions, excluding startup restoration |
| `message` | `message_deliveries_total`: successful deliveries to another recipient; self-notes and rejected deliveries are excluded; no message text or identities |
| `moderation` | `actions_total`: actions reaching the completed-action notification, including silent actions; no targets, reasons, durations or case IDs |
| `regions` | `claims_created_total`, `claims_deleted_total`: successfully committed operations, not failed/rolled-back transactions; no geometry, claim names or owners |
| `remade-ender-dragon` | `encounters_started_total`, `encounters_completed_total`: custom encounters started / ending in dragon death; previews are excluded; no worlds or player data |
| `yv_smp` (`yv-smp` artifact) | `audio_loads_total`: successful audio-load callbacks, never source URLs, filenames or listeners |
| `stats` | `config_opens_total`, `push_successes_total`: administration menu opens and completed successful telemetry pushes |
| `metrics` | Enabled/version metadata only. Its database, gameplay totals, sessions, Favorites, leaderboards and HTTP scrape contents are **never** forwarded to YVtils. Its local operation remains independent of stats consent. |

These are **cumulative usage counters since the current consented in-memory
collection began**, not per-player statistics, historical imports, per-request
deltas or persistent lifetime totals. A counter may be absent until its first event.
Repeated exports/pushes do not increment or reset feature counters. Restarting,
withdrawing consent, or unloading the owning module clears its values. Events while
stats/consent is inactive are discarded, never reconstructed from gameplay saves.
Re-acceptance starts new counters without replaying those events.

All collection uses the existing `ModuleStats` bridge and the single stats registry.
The shared module registry owns built-in publisher lifecycle; `core` and GUI register
their library/runtime lifecycle separately. The fixed `ModuleTelemetry.Event`
catalog accepts only an event and an optional numeric amount, so instrumentation
cannot pass command arguments, chat/status text or player identifiers by accident.
It is thread-safe, performs no HTTP or Bukkit reads, and treats publication failures
as best-effort rather than failing the feature operation.

The existing scheduler includes these values in JSON POSTs to
`https://api.yvtils.net/stats`: first after one minute, then hourly after success,
with the documented retry backoff. Features do not create their own HTTP clients.
`push_successes_total` naturally includes the current success in the **next** payload.
No additional endpoint, automatic opt-in, unbounded labels or per-module network
requests are introduced. Deploy the updated core, matching GUI artifact and edited
feature libraries together; previously published feature versions contain no new
event hooks.

## Publishing statistics from another module

The interface lives in the **shared `utils` runtime**, not the optional stats JAR:
`yv.tils.utils.stats.ModuleStats` and `yv.tils.utils.stats.StatsApi`. Existing feature
modules already depend on `utils` with `compileOnly`; do not add/shade a stats or
private utils dependency. Java interfaces, JDK types and a shared enum are used at
this classloader boundary, not Kotlin function types.

```kotlin
import yv.tils.utils.stats.ModuleStats
import yv.tils.utils.stats.StatsApi

private var usage: ModuleStats? = null

// In your module's enablePlugin():
// usage = StatsApi.forModule("regions")

fun recordClaimCreated() {
    usage?.incrementCounter("claims_created", "Claims created while telemetry is enabled", 1)
}

// Other supported publications:
// usage?.setGauge("active_claims", "Active claims", claimCount.toLong())
// usage?.setGauge("processing_load", "Processing load", 0.75)
// usage?.observeHistogram("claim_duration_milliseconds", "Claim operation duration", 1.25)
// usage?.setString(...), setList(...), setTimestamp(...)

// In disablePlugin(): usage?.close(); usage = null
```

- Updates are event-driven and thread-safe. Every operation returns whether it was
  accepted. If stats is absent/disabled or consent is off, it returns `false` and
  discards the update: no values are registered, accumulated or replayed later.
  Check `usage?.isEnabled == true` before doing expensive optional collection;
  keep Bukkit reads on the server thread.
- Publishers can be obtained before stats enables and survive its disable/re-enable
  or consent changes. Close one publisher per module on module disable; it removes
  that module's series and permanently invalidates that publisher. Create a fresh
  publisher when the module enables again.
- Keys are automatically namespaced, e.g.
  `yvtils_module_regions__claims_created`. Module separators `_`, `-` and `.` are
  escaped as `_u`, `_h` and `_d` so names cannot alias another module. Keys must match
  `[A-Za-z_][A-Za-z0-9_]{0,127}` and module IDs
  `[A-Za-z][A-Za-z0-9_.-]{0,63}`. Use fixed, low-cardinality metric keys, not player IDs.
- Publication obeys the same type checks and capacity limits as the registry.
  Successful values automatically appear in local exports and consented remote
  pushes; custom modules do not send HTTP requests themselves. `enabled`, `version`
  and that module's built-in event keys are reserved; use distinct keys for additions.
- This is a publishing API, not an automatic Bukkit polling scheduler. Feature
  modules publish the non-identifying usage events listed above. Additional API
  consumers decide which non-identifying events to publish. Never pass player
  names, player UUIDs, IP addresses, world seeds or other personal data.

Deploy the updated **core launcher as well as stats**: the interface is embedded
with its updated shared utils runtime. An older core does not contain this API.

## API and export behavior

- `StatsRegistry` registers counters, integer/decimal gauges, strings, string lists,
  millisecond timestamps, and non-negative histograms. Re-registering the same key
  and type returns a handle without resetting the value; a conflicting type fails.
- Counters cannot decrease or wrap on overflow. Gauges retain fractional values;
  `GaugeHandle.getDouble()` reads them without truncation (`get()` returns a long).
  NaN/infinite gauge values and invalid histogram observations are rejected.
- Prometheus output escapes help and comment values to prevent line injection.
  Histograms expose count, sum and the required `+Inf` bucket only; finite buckets
  and percentiles are not implemented. Generated histogram series names are reserved
  against conflicting registrations. Timestamp values remain milliseconds for
  compatibility; use a `_milliseconds` suffix in new metric names.
- JSON and Prometheus exports use copied, sorted snapshots and do not query Bukkit.
  Local exports are available to API consumers independently of remote consent.
  Consumers should check consent before collecting custom telemetry themselves.
- Call `StatsPusher.pushOnIo()` from a coroutine. Legacy `push()`, `pushAsync()` and
  `forcePush()` are **synchronous**, and reject Paper's server thread rather than
  blocking it. Forced requests bypass retry backoff, never consent or module state.
- Push starts after one minute, then runs hourly after success. Failures retry after
  1, 2, 4, 8, 16, 32, then 60 minutes, using a monotonic deadline. One request runs
  at a time; repeated scheduler starts are idempotent. Owned IO jobs avoid collisions
  with shared coroutine task IDs and are canceled on withdrawal/disable.
- HTTP redirects are not followed. Responses are bounded to 16 KiB, all 2xx statuses
  are supported, and explicit API rejection or malformed non-empty responses are
  errors. Payloads are not written to diagnostic logs.
- The push wire format keeps `STRING_LIST` mapped to `LIST`; decimal gauges use the
  optional `value.doubleValue` field, consistent with local JSON values.

## Verification

```sh
./gradlew :stats:test :stats:build :utils:build :core:build --no-daemon
```

Tests cover consent persistence/failure, admin permissions, first-operator prompt
selection, one-line boot notices, optional module publishing/namespaces/cleanup,
all built-in event types, late acceptance/metadata refresh, absence of buffered
history, best-effort backend failure handling, configured limits, concurrent updates,
decimals, exact histogram restoration, safe exports, retry backoff and real serialized
module-usage HTTP payloads against a loopback-only fake API. Tests never contact
YVtils services. For collection changes spanning modules, run `./gradlew build --no-daemon`.

Paper acceptance checks (not replaced by unit tests):

1. Enable with `opt_in: null`/`false`; verify no collection or remote requests start.
2. Accept through the in-game menu/command/API; verify one collector and one push scheduler, even
   after accepting twice. Verify the late-loaded module list is refreshed.
3. Toggle player-count collection through the shared config GUI; verify its metric
   disappears/reappears and only one collector remains.
4. Withdraw consent; verify registry cleanup, no further samples/pushes, and persisted
   rejection after restart. Disable/re-enable and verify collection restarts only
   with persisted acceptance.
5. Join first as a non-op, then as an op: only the op should receive the clickable
   prompt while undecided; another op that boot must not receive it. Test all three
   buttons, persistence after restart, and explicit rejection suppressing prompts.
6. Verify ordinary players cannot open/save the stats GUI or use its commands.
   Revoke a delegated admin's permission while their editor is open and verify a
   later click/save is denied. Ensure editing settings does not change consent.
7. Verify `/yvtils config`, `/yvtils config stats`, and `/yvtilsstats` open the correct
   menus. Verify exactly one stats informational status line during each boot.
8. Perform one action from each enabled feature in the table; inspect its namespaced
   counter in an API export or a consented push. Rejected/failed actions must not
   increase success counters. Verify batched fusion crafting counts repetitions,
   Discord delivery failures do not count as deliveries, and dragon previews do not
   count as encounters. Confirm no names, arguments, URLs, text, worlds or geometry
   have entered metric keys/values.
9. Accept after modules have already loaded: current module metadata must appear
   immediately, but earlier actions must not. Withdraw and exercise features again;
   the registry must remain empty until re-acceptance, when counters start afresh.
10. With `metrics` enabled, verify its local exporter/database continue working when
    stats is off and its gameplay/leaderboard contents do not enter telemetry.

For deployment, publish the dynamic library using the root repository's local-Maven
workflow. Its JAR is not an independently installable Bukkit plugin.
