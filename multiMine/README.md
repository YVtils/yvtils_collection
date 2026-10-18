# MultiMine

Connected-block vein mining/timber, per-player toggles, configurable block/tool
gates, break limits, cooldowns and accelerated leaf decay. Sneaking opts out;
normal mining activates only in Survival with the required permission.

- `MultiMineYVtils.kt`: dynamic lifecycle.
- `commands/`, `logic/BlockManage.kt`: `/mm` block/toggle administration.
- `logic/MultiMineHandler.kt`: initial event eligibility and run counters.
- `utils/BlockUtils.kt`, `ChainedBlockBreak.kt`: bounded chain scheduling and
  Paper player-break operations, including protection cancellation and durability.
- `logic/LeaveDecayHandler.kt`: affected-tree leaf decay.
- `configs/`, `language/`: typed configuration, saved toggles and messages.
- `src/test/`: denial/recursion-guard tests.

Project/module key is case-sensitive `multiMine`. Files live below
`plugins/yvtils/multiMine/`. Verify with `./gradlew :multiMine:build`, then test
WorldGuard boundaries, tools, simultaneous runs and disconnects on Paper.
