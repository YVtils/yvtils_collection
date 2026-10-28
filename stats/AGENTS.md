# Statistics API

Follow [root AGENTS.md](../AGENTS.md) for toolchain, dynamic-module publishing, and shared-runtime rules.

- `src/main/kotlin/yv/tils/stats/StatsYVtils.kt` exposes the feature API; `registry/StatsRegistry.kt` tracks metrics, `logic/StatsService.kt` exports Prometheus/JSON, and `logic/StatsPusher.kt` handles remote push.
- In-game administration uses `/yvtilsstats [config/optin <accept/decline>]` and the shared `/yvtils config stats` shortcut. Use the existing InvUI-based menu/editor; consent buttons are explicit and a general-settings draft must not overwrite live consent. Check OP/`yvtils.stats.manage`/`yvtils.stats.optin` on every action/save.
- Only the first operator joining while consent is undecided gets the clickable localized prompt each boot. Recorded acceptance/decline suppresses it. Keep the startup stats status notice to one INFO line; routine setup is debug-only.
- Module publishers use `utils`' shared Java `StatsApi`/`ModuleStats` interface, not a feature-to-stats dependency. Updates are namespaced and discarded without buffering while stats/consent is inactive; close publishers on module disable. Keep the bridge attached only during stats enable and clear metrics under the backend lock on withdrawal.
- Built-in collection uses shared `ModuleTelemetry.Event` constants at feature commit points. Every loaded module contributes enabled/version metadata; core and selected GUI register separately. Refresh metadata after consent changes and before exports without Bukkit reads. Do not copy metrics' gameplay database, sessions, Favorites or leaderboards into telemetry. Keep the exact counter semantics/catalog documented in README.
- Preserve persisted opt-in behavior: default metrics and scheduled remote push start only after acceptance; opting out stops push. Late enable refreshes the loaded-module list.
- Disable/withdrawal stop remote push and player sampling and clear the registry. Push owns cancellable IO jobs (shared coroutine task IDs are unsafe here); Bukkit player-count sampling stays on the server thread. Exports/payload construction use snapshots, not worker-thread Bukkit collection.
- Persist the random installation ID; never derive it from world seeds or other identifying server properties. Keep this telemetry separate from `metrics` and its HTTP endpoint/consent model.
- Verification from the repository root: `./gradlew :stats:test :stats:build :core:build --no-daemon`. Tests use a loopback fake API, never the production endpoint. See README for Paper acceptance checks.
