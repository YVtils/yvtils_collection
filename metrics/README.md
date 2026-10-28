# Metrics

Single-server Minecraft health, player activity and persistent gameplay leaderboards.
This is **not** YVtils plugin-usage telemetry; the existing `stats` module is separate.

- Local SQLite lifetime totals, UTC daily buckets and session checkpoints.
- TPS, tick histograms, JVM/process health and per-world load.
- Player playtime, deaths, kills, blocks, distance, totems, damage and food statistics.
- Jumps, pickup/drop counts, raids, breeding/trades, chest opens, enchanting, bells,
  sneaking/AFK time and optional per-block/per-entity Favorites.
- Private-by-default Prometheus endpoint; optional bounded, identifying leaderboards.
- Prometheus/Grafana setup or local Alloy forwarding to Grafana Cloud/Mimir.

Enable `metrics` in `plugins/yvtils/modules.yml` and restart. Configuration is generated
at `plugins/yvtils/metrics/config.yml`; the database is `metrics/metrics.db` alongside it.
Defaults expose `http://127.0.0.1:9108/metrics` without contacting any external service.
Configuration changes require a restart. No custom GUI or commands are registered.

See [setup, storage semantics, privacy, backups and testing](../docs/metrics.md).
Example [dashboard and collector configurations](../docs/metrics/) are included.

Verify: `./gradlew :metrics:test :metrics:build :core:build --no-daemon`.
Publish dynamically using the repository's local-Maven workflow; this module's JAR
is a library, not an independently installable Bukkit plugin.
