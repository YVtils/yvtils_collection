# Code-size, density and redundancy inventory

Analysis of the working source tree on 2026-10-18, after the inventory, waypoint,
Vanish and configuration changes. **Inventory only: the candidates below have not
been refactored as part of this report.**

Subsequent change: the migration module has been removed. References to it below
are historical scan results, not current source paths.

## Method

`python3 scripts/code-inventory.py` reads Kotlin/Java/Gradle Kotlin source, excluding
build output, Gradle/IDE caches, server run directories and Git metadata. The scan
covered 432 files at the initial measurement. Counts are physical lines, including
comments/blank lines. Line references will shift with later edits.

- Large file: at least **400 physical lines**.
- Dense line candidate: at least **180 characters**, or **two semicolons** on a
  nonblank line that does not begin with a comment/import/package marker.
- Duplication candidate: eight consecutive normalized code lines repeated in
  different source files, totaling at least 180 characters. Whitespace is normalized;
  identifier/literal differences are not ignored. Candidate groups are sampled to
  avoid overwhelming overlapping windows. This is not a compiler/AST clone analysis.

Long translation strings, embedded textures, annotations and semicolons inside
strings are **data-heavy false positives for executable complexity**. They are
explicitly separated below. No code-quality score is inferred from line count alone.

## Large files

Paths below are relative to each module's `src/main/kotlin/yv/tils/` unless shown otherwise.

| File | Lines | Longest line | Notes |
|---|---:|---:|---|
| `remade-ender-dragon/.../remadeEnderDragon/logic/DragonFight.kt` | 1333 | 158 | Attack/state orchestration, effects and encounter mechanics |
| `regions/.../regions/gui/ClaimsGui.kt` | 1012 | 134 | Multiple claim/member/flag/subzone workflows in one GUI |
| `yv-smp/.../yv_smp/utils/CinematicCamera.kt` | 738 | 120 | Camera drivers plus spline/easing implementation |
| `yv-smp/.../yv_smp/language/Language.kt` | 731 | 364 | Mostly translation data |
| `gui-26.1/.../gui/logic/DataClassConfigGui.kt` | 717 | 126 | Reflection, input parsing, lists and rendering |
| `stats/.../stats/registry/StatsRegistry.kt` | 629 | 109 | Metric types, registry and export-related support |
| `regions/.../regions/language/LangStrings.kt` | 614 | 220 | Mostly translation data |
| root `build.gradle.kts` | 522 | 129 | Dependency/build policy, publication, checksums, local tooling |
| `discord/.../discord/language/RegisterStrings.kt` | 521 | 221 | Mostly translation registration/data |
| `stats/.../stats/logic/StatsPusher.kt` | 489 | 138 | Push lifecycle, serialization and requests |
| `gui-26.1/.../gui/logic/ConfigGui.kt` | 476 | 120 | Entry-backed editor parallels typed editor |
| `yv-smp/.../yv_smp/gui/YvSmpGui.kt` | 469 | 119 | Setup/music/start management screens |
| `regions/.../regions/logic/ClaimService.kt` | 430 | 179 | Claim mutations, WorldGuard state and rollback |
| `yv-smp/.../yv_smp/utils/ParticleEffects.kt` | 430 | 116 | Multiple scheduled effect families |
| `fusion/.../fusion/gui/FusionGui.kt` | 424 | 131 | Browser, detail and crafting interactions |
| `remade-ender-dragon/.../remadeEnderDragon/configs/DragonConfig.kt` | 412 | 179 | Settings, validation and persistence |
| `remade-ender-dragon/.../remadeEnderDragon/logic/FightManager.kt` | 409 | 184 | Discovery, recovery, lifecycle and commands |

## Dense-line candidates

### Executable or mixed data/control lines

| File | Candidate line numbers | What makes it dense |
|---|---|---|
| `remade-ender-dragon/.../logic/DragonFight.kt` | 195, 724, 1066, 1317–1318 | Multiple mutations/calls separated by semicolons |
| `regions/.../logic/ClaimService.kt` | 271, 303, 353 | Rollback, save attempts and exception construction on one line |
| `fusion/.../gui/FusionGui.kt` | 234, 408 | Multiple UI/state operations on one line |
| `fusion/.../api/FusionRegistry.kt` | 63, 102 | Multiple registry mutations/clear operations |
| `regions/.../logic/ClaimInformation.kt` | 59, 68 | Multi-statement formatting/assembly |
| `regions/.../logic/ClaimOccupancy.kt` | 57 | Multiple lifecycle state changes |
| `regions/.../logic/SubzonePolicies.kt` | 31 | Multiple geometry/state assignments |
| `discord/.../logic/whitelist/AccountService.kt` | 156 | Compact failure/reporting statement; string semicolon contributes |
| `discord/.../utils/emoji/DiscordEmoji.kt` | 216 | Compact expression/string content; inspect before treating as logic duplication |
| `remade-ender-dragon/.../logic/FightManager.kt` | 84 | 184-character initialization/recovery expression |
| `regions/src/test/.../logic/SubzonePoliciesTest.kt` | 120 | Several test setup operations on one line |

### Mostly data/annotation/string width

| File | Count | Longest line | Candidate lines |
|---|---:|---:|---|
| `gui-26.1/.../utils/Heads.kt` | 25 | 190 | 32, 36, 40, 44, 48, 52, 56, 60, 64, 68, 72, 76, 80, 84, 88, 92, 98, 102, 106, 110, 114, 118, 122, 126, 130 |
| `discord/.../language/AccountText.kt` | 17 | 526 | 15, 16, 22, 25, 28, 40, 45, 46, 49, 51, 52, 57, 72, 74, 78, 79, 88 |
| `common/.../language/RegisterStrings.kt` | 10 | 278 | 47–48, 55–56, 63–64, 127–128, 143–144 |
| `moderation/.../language/RegisterStrings.kt` | 6 | 285 | 72–73, 80–81, 248–249 |
| `remade-ender-dragon/.../language/Messages.kt` | 6 | 282 | 19, 21, 30, 33–34, 43 |
| `discord/.../language/RegisterStrings.kt` | 6 | 221 | 386, 393–394, 401–402, 418 |
| `regions/.../gui/RegionHeads.kt` | 6 | 196 | 9–14; texture constants |
| `yv-smp/.../language/Language.kt` | 4 | 364 | 660–661, 690–691 |
| `fusion/.../language/FusionText.kt` | 3 | 179 | 92, 106–107; semicolons in strings |
| `regions/.../language/LangStrings.kt` | 2 | 220 | 51–52 |
| `message/.../language/RegisterStrings.kt` | 2 | 196 | 40–41 |
| `remade-ender-dragon/.../configs/DragonConfig.kt` | 2 | 179 | 44, 76; annotation/string content |
| `discord/.../configs/DiscordConfigState.kt` | 1 | 195 | 74; description |
| `core/src/main/java/.../loader/ModuleConfig.java` | 1 | 125 | 176; generated-file explanatory string |
| `migration/.../MigrationYVtils.kt` | 1 | 117 | 27; deprecation message |

## Redundancy within modules

1. **GUI editor parallels:** `gui-26.1/.../logic/ConfigGui.kt` and
   `DataClassConfigGui.kt` repeat page/back controls, scalar input parsing, list-item
   editing, save/cancel navigation and value presentation. Exact windows occur around
   `ConfigGui:200/300/356/365/405/425/458` and typed editor
   `374/517/582/591/645/666/699`. Different backing schemas are intentional; the UI
   mechanics are a consolidation candidate.
2. **Moderation command resolution:** Ban, Kick, Mute, TempBan, TempMute, Unban,
   Unmute and Warn repeat asynchronous profile resolution, delayed-action feedback,
   cancellation and exception reporting (`commands/*Command.kt`, notably lines 34–50).
   TempBan/TempMute also repeat duration/unit argument construction.
3. **Moderation action wrappers:** `logic/BanLogic.kt`, `KickLogic.kt`, `MuteLogic.kt`,
   `UnbanLogic.kt`, `UnmuteLogic.kt`, `WarnLogic.kt` repeat parameter maps, profile
   conversion, sender metadata, error handling and broadcast assembly. Temporary
   variants parallel permanent variants with expiry added.
4. **Discord event bridging:** `logic/sync/serverChats/SyncAdvancements.kt:40`,
   `SyncChats.kt:51`, `SyncDeaths.kt:39`, `SyncPlayerConnectionChange.kt:45` share
   channel/enable checks, embed selection and dispatch patterns.
5. **Discord account UI/commands:** `commands/DiscordAccountsCommand.kt`,
   `gui/AccountsGui.kt` and Discord interaction handlers repeat permission gates,
   stale-snapshot checks and error-to-feedback mapping. Core account mutations are
   already centralized in `AccountService`; UI feedback still overlaps.
6. **SMP phase orchestration:** `logic/start/HealPhase.kt`, `ItemClearPhase.kt`,
   `FinalPhase.kt` share camera-stop, spawn placement and sound/effect setup
   (`FinalPhase:61`, `HealPhase:62`, `ItemClearPhase:61` exact windows). Other phases
   repeat scan/radius computation and delayed particle/audio loops.
7. **Camera drivers:** `utils/CinematicCamera.kt` driveOrbit, driveDollyIn,
   driveSpiral, drivePendulum and drivePath repeat elapsed/task cancellation,
   display lookup and smooth-move scheduling. Path mathematics differs.
8. **Dragon attack utilities:** the large `DragonFight.kt` repeats fighter iteration,
   warning scheduling, entity tagging/cleanup and particle/sound patterns across
   attacks. This is a semantic candidate, not claimed as an exact clone.
9. **Claim transactions:** `regions/.../logic/ClaimService.kt` and `ClaimFlags.kt`
   repeatedly snapshot WorldGuard state, mutate, save, update metadata and roll back.
   Operation-specific domain/geometry handling must remain distinct.

## Redundancy across modules

1. **Update checkers:** `common/.../updateChecker/PluginVersion.kt` versus
   `core/.../CheckVersion.kt` duplicate remote version lookup/parsing and version
   comparison (`53↔76`, `132↔110`, `180↔159`, `213↔191`). Core also includes the
   separate server compatibility check. Common is the actively launched checker;
   the second implementation remains an overlap candidate.
2. **Permission boilerplate:** `common/permissions/Permissions.kt`,
   `discord/data/Permissions.kt`, `essentials/permissions/Permissions.kt`,
   `moderation/data/Permissions.kt`, `multiMine/data/Permissions.kt`,
   `regions/data/Permissions.kt`, `yv-smp/permissions/Permissions.kt` repeat enum
   collection, wildcard child maps and permission-list assembly (exact windows
   around lines 26–46). Vanish/Waypoint use smaller explicit lists.
3. **Typed-config facades:** common, Discord, MultiMine, Moderation, Regions,
   Server, Stats and Status `ConfigFile.kt` implementations repeat load/save,
   flatten-to-dotted-map synchronization and typed getter adapters. They differ in
   validation and runtime application. `config/data/EntryBackedConfig.kt` covers
   the older entry schema; it does not remove the typed facade repetition.
4. **Save wrappers:** Status, MultiMine and Moderation use similar list load,
   UUID indexing, immutable-next-state planning and atomic save wrappers. Discord
   accounts add a separate consistency/transaction contract and should not be
   flattened into a generic implementation without preserving it.
5. **GUI controls:** core module/config GUIs, shared config editors, StatusManager,
   ClaimsGui, FusionGui and AccountsGui each build pagination/back/confirmation,
   anvil input and item-styling wrappers. Exact page controls repeat across core,
   shared GUI and Status around `YVtilsConfigGui:87`, `YVtilsModulesGui:162`,
   `ConfigGui:113`, `DataClassConfigGui:250`, `StatusManager:224`.
6. **Translation registration:** message/moderation/MultiMine/server/stats/status
   `RegisterStrings.kt` files duplicate enum iteration and English/German
   registration scaffolding. Newer modules also use paired-language map helpers.
   Most text values themselves are not redundant code.
7. **HTTP requests:** update checkers, MojangAPI, DiscordAccountNames/account
   lookup, emoji downloads and StatsPusher repeat URL connection/timeouts,
   response checking and stream cleanup. Authentication, rate limits and threading
   differ. `common PluginVersion:150`, `core CheckVersion:126`,
   `utils MojangAPI:90/101` include exact response-read windows.
8. **Migration defaults:** retired `migration/modules/MultiMineMigrator.kt:154–175`
   repeats block defaults in `multiMine/configs/MultiMineConfigState.kt:70–91`.
   Historical migration data may intentionally freeze old defaults.
9. **Gradle module dependencies:** feature build files repeat shared `compileOnly`
   dependencies and test setup. GUI 26.2/26.3 repeat source-set wiring intentionally
   to compile a shared source tree against different dependency versions.

## Intentional sharing and exclusions

- `gui-26.2` and `gui-26.3` compile `gui-26.1` sources; this is **one source tree**,
  not three duplicated implementations.
- Generated module versions, shaded classes, test reports and cached server sources
  are excluded. The removed legacy configuration implementation is not counted.
- Similar policy tests are evidence of covered cases, not automatically redundant
  tests. Binary textures and translation content should be reviewed as data.
- The requested Vanish/Waypoint restructuring is implemented; the independent
  consolidation candidates above are deliberately left unchanged.
