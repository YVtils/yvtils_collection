# Single-server player/server metrics

Follow root `AGENTS.md` for toolchain, versions and dynamic publishing.

- `MetricsYVtils` owns the lifecycle, background SQLite writer, cached exporter and HTTP server. This is separate from the existing developer-telemetry `stats` module; never reuse its consent or endpoint.
- `PlayerCollector` and `VanillaStatistics` access Bukkit only on Paper's server thread. Never enumerate loaded chunks/entities to compute counts: use Paper's world counters.
- `SqliteStore` has one background owner, WAL mode and transactional batch-ID deduplication. Retain the immutable in-flight batch until commit acknowledgement. Import vanilla baselines only on the first insert and never into daily history.
- Keep integer storage units and the `PlayerStat` divisors aligned with SQL/dashboard documentation. Names and UUIDs are private by default; publishing Top-N rankings is explicit opt-in.
- Schema 2 adds lifetime/daily Favorites without rewriting existing totals. `publishFavorites` is a separate, bounded identifying export opt-in; do not expose an unbounded per-player/material matrix. `MOB_KILLS` remains the old all-mob counter; monster/non-enemy splits are separate counters.
- AFK is a conservative estimate from Paper idle duration after a configurable threshold, never a historical import or a server idle-kick change. Native sneaking ticks convert to integer milliseconds at 20 TPS.
- HTTP handlers serve pre-rendered bytes only. Keep the loopback default and mandatory bearer token for non-loopback binds. Do not send data to YVtils telemetry services.
- SQLite is the only implemented backend. Multi-server/PostgreSQL ideas are documentation, not supported configuration.
- Verification: `./gradlew :metrics:test :metrics:build :core:build --no-daemon`. See `docs/metrics.md` for Paper acceptance checks.
