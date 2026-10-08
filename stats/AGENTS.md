# Statistics API

Follow [root AGENTS.md](../AGENTS.md) for toolchain, dynamic-module publishing, and shared-runtime rules.

- `src/main/kotlin/yv/tils/stats/StatsYVtils.kt` exposes the feature API; `registry/StatsRegistry.kt` tracks metrics, `logic/StatsService.kt` exports Prometheus/JSON, and `logic/StatsPusher.kt` handles remote push.
- The module provides APIs rather than its own command registration. Opt-in messages advertise `/yvtils stats`, but current core's command tree only implements `info`, `modules`, and `config`; do not assume those advertised stats commands exist.
- Preserve persisted opt-in behavior: default metrics and scheduled remote push start only after acceptance; opting out stops push. Late enable refreshes the loaded-module list.
- Disable stops remote push and clears the registry. Account for shared coroutine scheduling when changing push or metric collection.
- Verification from the repository root: `./gradlew :stats:build :core:build`. There are currently no stats test sources despite JUnit-platform task configuration.
