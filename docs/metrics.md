# Minecraft player and server metrics

## Scope and architecture

`metrics` monitors Minecraft itself. It is independent of `stats`, which retains its
existing developer-telemetry behavior and consent. Enabling either does not enable
the other. Discord's `StatsAsChannels.kt`, channel IDs, public hidden-player filtering
and refresh behavior are unchanged; Discord does not depend on this module.

Implemented deployment: **one Paper server, one local SQLite database**.

```text
Paper server thread -> bounded-category in-memory counters -> background SQLite transactions
       |                                                        |
       +-> numeric observations                  durable totals / optional Top-N
                         \                         /
                          background Prometheus renderer
                                      |
                            cached localhost /metrics
                                      |
                     Prometheus OR Grafana Alloy scraper
                                      |
                      local Prometheus / remote metrics storage
                                      |
                                   Grafana
```

SQLite is the source of truth for player totals. Prometheus (or Cloud Metrics/Mimir)
stores monitoring time series with its own retention. Grafana visualizes that data;
a normal Grafana dashboard URL is **not** a metrics ingestion endpoint.

There is no diamond-balance or Twitch collection, no module/plugin usage analytics,
and no IP, chat, command, coordinate, inventory or world-seed export.

## Installation

1. Install the core launcher as described in the root README.
2. Set `metrics: true` in `plugins/yvtils/modules.yml` and restart.
3. Wait for `[Metrics] Collection active` in the console.
4. On the Minecraft host, request `http://127.0.0.1:9108/metrics`.
5. Choose the Prometheus or Alloy deployment below. The Compose test stack provisions
   the example dashboards automatically. For a separate Grafana instance, import the
   JSON files in [`metrics/grafana/dashboards/`](metrics/grafana/dashboards/).

For unpublished development builds, use:

```sh
./gradlew :metrics:publishToMavenLocal generateLocalMavenChecksums :core:shadowJar
```

Serve the local Maven repository over HTTP and launch core using the root
`AGENTS.md` workflow. Publish the matching GUI artifact and other enabled feature
dependencies as needed. Do not copy the metrics library into `plugins/`.

### Settings

`plugins/yvtils/metrics/config.yml` uses the shared Configurate runtime. All changes
require a restart; malformed/invalid configuration is not replaced with defaults.

| Setting                    | Default     | Meaning                                                      |
|----------------------------|-------------|--------------------------------------------------------------|
| `bindAddress`              | `127.0.0.1` | Private HTTP listen address                                  |
| `port`                     | `9108`      | Dedicated TCP metrics port, not the Minecraft port           |
| `bearerTokenEnvironment`   | empty       | Optional environment variable holding the token              |
| `flushSeconds`             | `20`        | Background commit and database snapshot interval             |
| `sampleTicks`              | `100`       | Player/server sample interval; ticks are observed every tick |
| `collectPlayerStats`       | `true`      | Record UUID-keyed totals and sessions                        |
| `collectGameplay`          | `true`      | Gameplay counters in addition to sessions/playtime           |
| `collectBreakdowns`        | `true`      | Persist per-material/per-entity Favorites in SQLite          |
| `trackAfk`                 | `true`      | Estimate AFK time from Paper's idle-duration signal          |
| `afkThresholdSeconds`      | `300`       | Idle grace period before time counts as AFK                  |
| `importVanillaOnFirstJoin` | `false`     | One-time baseline for a newly recorded player                |
| `publishLeaderboards`      | `false`     | Export identifying Top-N ranking samples                     |
| `leaderboardSize`          | `10`        | Top-N per statistic; 1–100                                   |
| `publishFavorites`         | `false`     | Separately opt in to identifying per-player Favorites       |
| `favoritePlayerLimit`      | `25`        | Maximum recently seen players whose Favorites are exported |
| `favoriteSize`             | `10`        | Maximum entries per player/category; 1–100                  |
| `dailyRetentionDays`       | `365`       | UTC daily history retention; 0 means forever                 |
| `sessionRetentionDays`     | `90`        | Completed/interrupted session retention; 0 means forever     |
| `shutdownTimeoutSeconds`   | `10`        | Maximum graceful shutdown wait                               |
| `maxPendingEntries`        | `100000`    | Aggregated-record limit during storage failures              |

Database location: `plugins/yvtils/metrics/metrics.db`, honoring the shared
Configurate base-directory override. Lifetime totals are not automatically expired.
Disabling player collection suppresses historical player exports but does not erase
existing data. The module does not automatically
send anything outside the host.

### Network and privacy

Keep the loopback default when the scraper runs on the same host. A non-loopback
binding requires `bearerTokenEnvironment` to reference a non-empty environment
variable; the module refuses to start the endpoint without it. Configure the
scraper to send `Authorization: Bearer <token>`.

The embedded endpoint is plain HTTP. Use a private network/VPN or an HTTPS reverse
proxy and firewall rules when accessing it remotely. Never expose the SQLite file
through a web server. Tokens do not belong in source control or dashboard JSON.
The HTTP executor and request queue are bounded; the endpoint serves cached bytes
without scheduling server-thread work or querying SQLite.

Local player history contains UUIDs, names and visit timestamps. Operators should
disclose collection and choose appropriate retention/access policies. The private
online count includes hidden players; it is not the public Discord player count.
Setting `publishLeaderboards: true` explicitly exports names and UUIDs for Top-N
players. It does not enable an unbounded per-player/per-material metric matrix.
Ranking churn and name changes still create historical series until the metrics
backend's retention expires; consider storage costs before enabling it.

`publishFavorites` is a separate opt-in, not implied by `publishLeaderboards`.
It exports up to four categories × `favoriteSize` × `favoritePlayerLimit` series
at once (1,000 with the default limits), using only the most recently seen players.
All stored players remain queryable in SQLite. Turning off publication does not
erase database history or time series already retained by Prometheus.

## Collected statistics and exact semantics

### Server health

- TPS over 1/5/15 minutes; Paper average tick duration. Purpur's additional leading
  5-second TPS average is skipped, preserving the same minute-window labels.
  Unrecognized TPS array layouts omit TPS samples rather than stopping collection.
- A real tick duration histogram, maximum since the last sample, and counts of
  ticks exceeding 50/100/250 ms. Tick metrics reset on module/process restart.
- Last completed tick's timestamp: an external scraper can detect a frozen tick loop.
- JVM uptime/start time, heap used/committed/max, process CPU ratio when available,
  and cumulative garbage collection count/time.
- Loaded chunks, entities and block entities per world using Paper counters, **not**
  full world scans; online players per world and game mode.
- Player capacity and the shared maintenance flag.

Host-wide disk/network metrics should come from a host exporter rather than this
plugin. CPU/heap metrics describe the Java process, not the whole machine.

### Player/gameplay data

| Statistic / SQLite `stat` | Integer storage unit     | Source and limitations                                                                                                                         |
|---------------------------|--------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| `PLAYTIME`                | milliseconds             | Monotonic elapsed online time, including AFK; split at UTC midnight                                                                            |
| `JOINS`                   | sessions                 | Sessions opened while the module is active, including already-online players at activation                                                     |
| `DEATHS`                  | deaths                   | Sampled vanilla counter                                                                                                                        |
| `MOB_KILLS`               | kills                    | Vanilla non-player mob kills, not hostile mobs only                                                                                            |
| `MONSTER_KILLS`          | kills                    | Vanilla entity-kill increments for Bukkit `Enemy` classes, including neutral monster types |
| `ANIMAL_KILLS`           | kills                    | Other `Mob` classes: animals, aquatic mobs, golems, villagers, etc.; excludes players/armor stands |
| `PLAYER_KILLS`            | kills                    | Sampled vanilla counter                                                                                                                        |
| `BLOCKS_BROKEN`           | blocks                   | Non-cancelled vanilla `MINE_BLOCK` increments                                                                                                  |
| `BLOCKS_PLACED`           | blocks                   | Non-cancelled, build-allowed placement; a multi-block placement counts its block states                                                        |
| `DISTANCE`                | centimetres              | Sum of selected vanilla walking/swimming/flight/vehicle/climb categories; excludes teleport displacement and separately reported fall distance |
| `*_DISTANCE`              | centimetres              | Walking, swimming, flight, vehicles, climbing and separate vertical fall-distance breakdowns                                                   |
| `TOTEMS_USED`             | activations              | Vanilla `USE_ITEM` for totems, not holding/right-clicking a totem                                                                              |
| `DAMAGE_DEALT`            | tenths of a health point | Vanilla damage-dealt counter, not independently attributed PvP/PvE events                                                                      |
| `DAMAGE_TAKEN`           | tenths of a health point | Sampled vanilla damage-taken counter |
| `FOOD_EATEN`              | items                    | Vanilla uses of edible materials; cake slices excluded                                                                                         |
| `ITEMS_CRAFTED`           | items                    | Vanilla craft-item increments; output item count, not recipe clicks                                                                            |
| `FISH_CAUGHT`             | fish                     | Sampled vanilla counter                                                                                                                        |
| `JUMPS`                 | jumps                    | Sampled vanilla jump counter |
| `ITEMS_PICKED_UP`        | items                    | Non-cancelled vanilla `PICKUP` increments; a stack of 64 counts as 64 |
| `ITEMS_DROPPED`          | items                    | Non-cancelled vanilla `DROP` increments; counts items, not drop events |
| `RAIDS_WON`             | victories                | Sampled vanilla `RAID_WIN` counter |
| `ANIMALS_BRED`          | breeding actions         | Sampled vanilla breeding counter |
| `VILLAGER_TRADES`       | trades                   | Sampled vanilla trade counter |
| `CHESTS_OPENED`         | opens                    | Vanilla chest opens only; no barrel or ender-chest opens |
| `ITEMS_ENCHANTED`       | items                    | Sampled vanilla enchantment counter |
| `BELLS_RUNG`            | rings                    | Sampled vanilla bell counter |
| `SNEAK_TIME`            | milliseconds             | Vanilla sneaking ticks × 50; exported in seconds |
| `AFK_TIME`              | milliseconds             | Estimated observed idle time after the AFK threshold; not historically importable |
| `ADVANCEMENTS`            | completions              | Non-recipe advancement completion events after collection starts                                                                               |
| `DIMENSION_CHANGES`       | changes                  | Changes between Bukkit worlds, including custom worlds                                                                                         |

Damage is exported in health points, distance in metres, and playtime in seconds.
Display damage in hearts by dividing by 2, kilometres by dividing metres by 1000,
and hours by dividing seconds by 3600. No floating-point accumulation is stored.

Vanilla movement has no reliable per-increment event, so selected counters are
sampled and sampled once more on departure/shutdown. Vanilla counter resets are
rebased without decreasing module totals. Programmatic actions that do not award
vanilla statistics or fire the corresponding Bukkit events cannot be inferred.
Cancellation is respected at MONITOR priority; plugins must not mutate events later
at MONITOR. Each statistic has one collection source to avoid double-counting.

Player activity also includes a session-duration histogram, departures, kicks by
Paper's finite cause enum, sampled ping distribution, high-ping count (>250 ms),
and unique players for all time/current UTC day/current UTC calendar month.
Ping observations are sampled player observations, not distinct players/sessions.
Unique players means players seen by this module, not offline player files scanned
from disk. Daily/monthly values are calendar periods, not rolling 24-hour/30-day counts.

Cohort retention analytics, damage-type breakdowns and native weekly/monthly
ranking controls remain later extensions. Daily SQL buckets already allow
calendar-period queries.

### Additional profile statistics and Favorites

The numeric fields from [the example player profile](https://craftattack.one/spieler/caravas/)
are covered, excluding Diamondbank. No Twitch/clips integration or copy of the
website's event feed/UI is included. `MOB_KILLS` retains its existing **all-mob**
meaning for compatibility; use the new `monster_kills` metric for the monster-only
card. The animal list uses a documented broad non-enemy-mob classification, not
an assertion that the source website uses identical categorization.

With `collectBreakdowns: true`, SQLite records per-player lifetime and UTC daily
counts for mined block materials, placed block materials, monster entity types,
and other mob entity types. Keys are stable namespaced registry IDs such as
`minecraft:stone` and `minecraft:zombie`; no inventory contents are inspected.
Multi-block placement uses each placed block's actual material, not the held item.
These are separate breakdowns of the same actions, **not additional counts to add
to the aggregate counters**. Persisting them increases SQLite row/storage usage;
daily breakdown retention follows `dailyRetentionDays` and lifetime breakdowns
remain available until explicitly removed offline.

To show the Favorites panel in the bundled Grafana dashboard, add:

```yaml
collectBreakdowns: true
publishFavorites: true
favoritePlayerLimit: 25
favoriteSize: 10
```

Restart the server, refresh the provisioned dashboard or re-import
[`rankings-favorites.json`](metrics/grafana/dashboards/rankings-favorites.json),
and select a Favorites player and category. Publication is intentionally bounded; full individual totals and
daily breakdowns can be queried locally even if the player is not in the published
recent-player window. Existing `publishLeaderboards: true` continues to publish
Top-N player totals for all the newly added numeric statistics without enabling
Favorites implicitly.

### AFK semantics

`trackAfk: true` samples Paper's `Player.getIdleDuration()` and counts only the
portion of an observed interval beyond `afkThresholdSeconds` (default five minutes).
The grace period itself is not counted. This uses Paper's action-packet idle
signal; no chat/command contents or additional movement coordinates are stored.
Other plugins that reset Paper idle time can affect the estimate. The module does
not alter the server's idle kick timeout, so a shorter idle kick may prevent the
AFK threshold from being reached.

The estimate is conservative: activity resuming between samples can discard up to
one sample interval of preceding idle time. It never imports pre-collection idle
time, counts offline time, or adds more AFK time than observed online time. Daily
AFK intervals split at UTC midnight. `yvtils_players_afk` is the current sampled
count and `yvtils_players_afk_seconds_total` is the durable accumulated duration.
Normal playtime continues to include AFK; historical AFK cannot be reconstructed
from an imported vanilla playtime total.

### Optional vanilla baseline import

With `importVanillaOnFirstJoin: true`, a player's first module record can include
existing vanilla playtime, deaths, kills, movement, damage dealt/taken, mined blocks,
crafted/picked-up/dropped items, food, totems, fishing, jumps, sneaking, raids,
breeding, trades, chest opens, enchanting and bell rings. Available mined-block and
entity-kill Favorites are imported when breakdown collection is enabled.
Import is performed only on joining/activation,
not by scanning all offline players. Imported playtime uses vanilla ticks × 50 ms,
so historical imported time can differ from wall-clock time on a laggy server.

The insert/import and new deltas commit together. Import cannot run again for an
existing UUID, even after a rename/restart or if the setting is enabled later.
Imports never enter daily buckets or current-session duration. Block placement,
AFK, historical advancement completions and past world changes cannot be reconstructed.
Without import, totals mean **since this module started observing the player**.

**Upgrade from the first metrics version:** schema 1 is upgraded transactionally
to schema 2 to add breakdown tables. Take a SQLite backup before upgrading.
Existing totals, identities, sessions and
daily history are preserved. Already-recorded players do **not** receive another
baseline, even with `importVanillaOnFirstJoin: true`: their newly introduced fields
and Favorites begin at this update. Do not delete the database to force an import;
that would discard history, including non-importable placement and session data.

## Storage, failures and backups

SQLite uses WAL, foreign keys, FULL synchronous commits, indexed leaderboard
queries, and a single background writer. Schema versions use `PRAGMA user_version`;
unsupported newer schemas fail without recreating the database. SQL identifiers
are fixed and values are parameterized.

The main thread maintains aggregated deltas (not an event log). Each batch carries
a unique ID committed in the same transaction as its changes. Failed/ambiguous
commits retain that immutable batch for safe retry while new changes accumulate
separately. Applied batch IDs are retained for seven days; only the current batch
is retried by the ordered writer.

The writer flushes periodically and on departures. Graceful shutdown checkpoints
sessions, drains pending writes and waits for at most the configured timeout.
An abrupt kill can lose uncommitted changes, normally up to one flush interval
while storage is healthy. During an outage the loss window can be longer. Health
metrics and rate-limited console errors indicate failures. Backlog grows with dirty
players/days/sessions, not the number of blocks broken. The pending-record cap bounds
the buffer (plus at most one equally bounded in-flight batch). Reaching the cap
logs an explicit error, sets collection readiness to zero, and pauses further player
collection until restart; server-health monitoring continues. Some player activity
will not be recorded after this safety stop. Restore disk availability before
restarting rather than silently dropping an unbounded backlog.

Session duration is checkpointed. On reopening after a crash, unfinished sessions
are marked interrupted and end at their last committed checkpoint; server downtime
is never added as playtime. Daily/session retention is pruned once per UTC day and
never deletes lifetime totals or player identities.

### Backup

For a live backup, use SQLite's backup API/CLI, for example on the Minecraft host:

```sh
sqlite3 plugins/yvtils/metrics/metrics.db ".backup 'metrics-backup.db'"
```

Do not copy only `metrics.db` while the server is running: committed data may still
be in `metrics.db-wal`. Alternatively stop the server cleanly, then back up the
database and configuration. Store backups outside publicly served directories.
To restore, stop the server, preserve the current database **and its WAL/SHM files**
in a recovery directory, then put the chosen backup at `metrics.db` before starting.
Never leave an old WAL beside a restored database.

### Local SQL leaderboards and exports

Tables: `players`, `player_totals`, `player_daily_stats`, `player_sessions`, and
`applied_batches`, plus `player_breakdown_totals` and `player_breakdown_daily`.
Use a read-only connection for reports; do not edit counters
while the module runs. For example:

```sql
SELECT p.name, t.value / 1000.0 AS seconds
FROM player_totals t JOIN players p ON p.uuid = t.uuid
WHERE t.stat = 'PLAYTIME'
ORDER BY t.value DESC, t.uuid
LIMIT 10;

SELECT p.name, SUM(d.value) AS deaths
FROM player_daily_stats d JOIN players p ON p.uuid = d.uuid
WHERE d.stat = 'DEATHS' AND d.day BETWEEN '2026-10-01' AND '2026-10-31'
GROUP BY d.uuid ORDER BY deaths DESC LIMIT 10;

SELECT subject, value AS blocks
FROM player_breakdown_totals
WHERE uuid = '<player-uuid>' AND category = 'BLOCKS_PLACED'
ORDER BY value DESC, subject LIMIT 10;
```

SQLite CLI supports JSON/CSV export for these queries. Grafana's built-in
Prometheus data source can display the opt-in Top-N metrics without a SQLite
plugin. Arbitrary individual histories or SQL period rankings need an explicitly
installed SQLite-compatible data source or another read-only integration. Do not
assume Grafana has built-in SQLite support. Provision a separate read-only backup
snapshot for a dashboard integration when practical rather than sharing a writable
live database. The included dashboard's leaderboard table is empty until opt-in.

## Prometheus / Grafana

Use [`metrics/prometheus.yml`](metrics/prometheus.yml) on the same host as Paper.
It scrapes every 15 seconds and loads [`metrics/alerts.yml`](metrics/alerts.yml).
Configure Prometheus retention and disk limits for your deployment. Add its URL
as a Prometheus data source in Grafana, then import the included dashboard.

For a small local testing instance on Linux, the included Compose stack runs
Prometheus and Grafana with persistent named volumes, a seven-day/1-GB Prometheus
retention limit, and provisions the datasource and dashboard automatically:

```sh
cd docs/metrics
cp .env.example .env
# Set GRAFANA_ADMIN_PASSWORD in .env to a local password.
docker compose up -d
```

Enable the `metrics` module and restart Paper first. Open Grafana at
`http://127.0.0.1:3000` (user `admin`, password from `.env`); the dashboard is in
the **YVtils** folder. Prometheus scrapes `127.0.0.1:9108` using host networking
and serves its UI/API on `127.0.0.1:19090` to avoid the common local Prometheus
port. Grafana's provisioned datasource points to that API. This test stack is
Linux-specific and keeps the metrics endpoint on the module's loopback default.
Grafana and Prometheus web interfaces also bind to loopback.
Stop it with `docker compose down`; `docker compose down -v` additionally removes
the test time series and Grafana state.

If endpoint authentication is enabled, configure Prometheus `authorization` with a
`credentials_file` readable only by the Prometheus service. Do not expect environment
variables to be interpolated automatically in Prometheus YAML.

The test stack provisions separate **Minecraft Overview**, **Server Health**,
**Player Activity**, **Gameplay**, and **Rankings and Favorites** dashboards in the
YVtils folder. Overview combines current health and cross-server comparison charts;
the other dashboards group the more detailed time series and totals. Leaderboard
samples are present only after publication is enabled; Favorites has its own
explicit publication flag.
Lifetime counters refresh after database commits; instantaneous server gauges are
sampled every `sampleTicks`, and pre-rendered HTTP bytes update about once a second.
Raw metrics remain available independently of the bundled dashboard.

Availability comes from Prometheus `up`, not a final 'offline' push from Minecraft.
Tick heartbeat alerts exclude empty servers to avoid treating Paper's empty-server
pause as a freeze. Configure an external dead-man alert if the scraper itself can
disappear. Tune storage freshness alerts if changing `flushSeconds`.

## Outbound forwarding with Alloy

[`metrics/config.alloy`](metrics/config.alloy) scrapes localhost and remote-writes
to a configured compatible backend. Run Alloy on the Minecraft host and set:

- `YVTILS_REMOTE_WRITE_URL`: Cloud Metrics/Mimir remote-write ingestion URL.
- `YVTILS_REMOTE_WRITE_USER` / `YVTILS_REMOTE_WRITE_TOKEN`: backend credentials.
- `YVTILS_METRICS_TOKEN`: endpoint bearer token, or empty when loopback auth is off.

Grafana Cloud supplies its ingestion URL and credentials. Self-hosted Grafana still
needs Prometheus-compatible storage, for example Mimir; do not use Grafana's UI URL.
Alloy handles forwarding queues/retries/WAL. Configure its persistent storage path
and retention/buffering for the outage window you need. This module itself has no
direct remote-write client and no Pushgateway dependency.

Container note: `127.0.0.1` means the current network namespace. A scraper in another
container cannot reach the host's loopback endpoint automatically. Use host
networking on Linux or a secured private bind with token/TLS and explicit routing;
do not blindly change the bind to `0.0.0.0` without authentication.

## Future multi-server design — NOT IMPLEMENTED

A future PostgreSQL backend could replace the local SQL store and introduce stable
server IDs, per-server counters, shared UUID identity and cross-server ranking
queries. It would also require migration tooling, connection pooling, independent
server batch namespaces, and defined network-session/unique-player semantics.
Grafana could query it with a read-only PostgreSQL data-source account.

There is currently **no** PostgreSQL driver, backend switch, cross-server database
support or data synchronization. Never mount the SQLite file on shared network
storage or let multiple Minecraft instances write it. Multiple Prometheus targets
can already be visualized together by their `instance` labels, but that does not
create a shared player-statistics database or deduplicated network-wide player counts.

## Verification

```sh
./gradlew :metrics:test :metrics:build :core:build --no-daemon
```

Tests cover durable/replayed transactions, rollback, one-time import, rename
identity, session crash recovery, retention, unsupported schemas, UTC splitting,
writer retries, metric units/histogram buckets, opt-in label escaping and HTTP auth.
They also cover the schema-1 upgrade, independent Favorites opt-in/limits,
per-category breakdowns, new vanilla mappings, sneaking units and AFK threshold bounds.
TPS export tests cover Paper's three-value and Purpur's four-value layouts and
omission of unknown layouts without retaining stale window samples.

Paper acceptance checklist (requires a running server; not covered by unit tests):

1. Enable only `metrics` plus required runtime; verify `stats` is not enabled and no
   outbound request occurs without a configured external scraper/Alloy.
2. Join/quit/restart, change a name, and verify SQLite totals/sessions and cached counters.
3. Mine/place, kill mobs, die, craft, eat, consume a totem, travel via different modes
   and teleport. Verify units, no teleport-distance inflation and no double-counting.
4. Cancel statistic/placement actions using protection plugins; verify excluded actions.
   Check block-breaking behavior with MultiMine as vanilla statistics depend on how
   that feature awards them.
5. Verify imported baselines do not repeat or appear in daily history; compare playtime
   and distance against Minecraft's statistics screen.
6. Confirm the default endpoint contains no UUID/name labels. Enable Top-N publication
   deliberately and verify rankings and the Grafana table.
7. Freeze the tick thread in an isolated test environment: cached HTTP remains responsive
   and the tick heartbeat becomes stale. Stop the process: Prometheus `up` becomes zero.
8. Test unavailable/read-only/full storage on a disposable deployment: health drops,
   errors are rate-limited, restored storage retries pending batches without duplication.
9. Test graceful shutdown and abrupt kill; verify documented loss window and interrupted
   session recovery, then exercise backup/restore on a copy.
10. Check bearer auth/firewall rules and Alloy forwarding with your actual backend.
11. Start on Purpur 26.3 and verify `[Metrics] Collection active`, readiness of `1`,
    and correctly labelled `1m`/`5m`/`15m` TPS samples without an index error.
