# Statistics API

Opt-in metric registration, Prometheus/JSON export and remote statistics push.
The module provides APIs; advertised `/yvtils stats` commands are not currently
wired into core's command tree.

- `StatsYVtils.kt`: lifecycle, opt-in state and default registrations.
- `registry/StatsRegistry.kt`: registered metrics.
- `logic/StatsService.kt`: export formats.
- `logic/StatsPusher.kt`: scheduled remote requests.
- `configs/`, `language/`: typed settings, opt-in and translations.

Enable `stats`; remote push remains inactive until opt-in acceptance.
Verify with `./gradlew :stats:build`; test acceptance/withdrawal and export consumers.
