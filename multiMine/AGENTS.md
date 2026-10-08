# MultiMine / vein mining

Follow [root AGENTS.md](../AGENTS.md) for toolchain, dynamic-module publishing, and shared-runtime rules.

- Gradle project, registry key, and package use case-sensitive `multiMine`. Entrypoint: `src/main/kotlin/yv/tils/multiMine/MultiMineYVtils.kt`.
- Current implementation is connected-block vein mining/timber, not cooperative mining despite the registry description. `listeners/BlockBreak.kt` delegates non-cancelled events to `logic/MultiMineHandler.kt`.
- Preserve permission, per-player toggle, block/tool checks, cooldown, sneaking opt-out, and Survival-only gates before starting a run.
- `utils/BlockUtils.kt` schedules chained breaks on Paper and tracks per-player counters; `logic/LeaveDecayHandler.kt` handles affected tree areas. Completion/tool-break cleanup must remain consistent with those counters.
- `BlockUtils` captures config values in companion initialization; avoid touching it before `ConfigFile.loadConfig()` or assuming live config edits refresh captured values.
- Verification from the repository root: `./gradlew :multiMine:build`; chain-breaking, limits, tool durability, and leaf decay need Paper playtests.
