# Remade Ender Dragon

Follow [root AGENTS.md](../AGENTS.md) for toolchain, dynamic-module publishing, and local testing. Read [the gameplay/configuration guide](../docs/remade-ender-dragon.md) for encounter and recovery semantics.

- `src/main/kotlin/yv/tils/remadeEnderDragon/RemadeEnderDragonYVtils.kt` wires `FightManager`, `TemporaryTerrain`, commands, GUI, and lifecycle cleanup; package is camelCase but artifact/config key is `remade-ender-dragon`.
- Encounters run on a main-thread ticker; keep Bukkit entity/block work synchronous and island mapping bounded per tick. Pure scaling/geometry rules belong in `logic/FightMath.kt`.
- Preserve original dragon HP recovery, tagged-entity cleanup, and journal-before-placement terrain ownership. `terrain-journal.json` must survive while temporary markers exist; recovery removes only matching owned blocks.
- Validate YAML and detached GUI drafts before replacing live configuration or stopping encounters. Reload/disable cleanup must cancel attacks and restore owned world state.
- `FIRST` activation uses both vanilla completion and world-UUID history; live timers/budgets reset after restart while history and terrain recovery persist.
- Verification: `./gradlew :remade-ender-dragon:build :core:compileJava`; focused rules: `./gradlew :remade-ender-dragon:test --tests 'yv.tils.remadeEnderDragon.logic.FightMathTest'`. Use the linked playtest checklist for encounters and recovery.
