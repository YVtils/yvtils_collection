# Release-readiness review — 2026-10-19

## Executive verdict: Not ready

**Overall readiness: 64/100.**

The branch builds successfully, all **111 existing tests passed when explicitly
re-executed**, and the launcher/shared-runtime architecture is substantially
improved. However, actionable release blockers remain in authorization, protection
handling, supported-version compatibility, maintenance behavior, and shared task
management.

**Registry correction:** The user subsequently tested Reposilite and confirmed it
is online and working. The review environment's download results are excluded from
the findings, release blockers and readiness score. Registry operation is recorded
as user-verified, not as an independently reproduced check by the reviewer.

**Recommended next milestone:** a controlled beta after the blockers below are
fixed. A stable release requires the live acceptance
matrix; compilation and unit tests do not establish that evidence.

This is a historical snapshot of the reviewed revision. Remote availability and CI
results describe observations during the review, not a continuously updated status.
The registry correction above updates that assessment without asserting that the
remaining source findings have been fixed or re-reviewed.

### Release assumptions

- This is an explicitly breaking, fresh-install release.
- Legacy data migration, old permission compatibility, and old addon API
  compatibility are not release requirements.
- Predecessors are references for useful feature coverage and behavior.
- The active configuration runtime is Configurate-backed `:config`, package
  `yv.tils.config`.
- All custom inventory GUIs must use the shared InvUI runtime.
- Actual source and build configuration take precedence over stale documentation.

### Weighted score

These are evidence-based readiness judgments, not measured code-coverage
percentages. Blockers override the numerical score.

| Area | Weight | Earned | Basis |
|---|---:|---:|---|
| Architecture and lifecycle | 20 | 16 | Correct bootstrap separation and reverse cleanup; task/resource lifecycle weaknesses remain |
| Correctness and data integrity | 25 | 15 | Atomic persistence and transaction planning implemented; compatibility, validation and failure-path gaps |
| Commands, permissions and GUIs | 15 | 9 | Strong newer action checks; older/shared screens retain authorization bypasses |
| High-risk feature interactions | 15 | 8 | Useful safeguards present, but leaf protection, visibility and audio cancellation remain incomplete |
| Release engineering | 15 | 12 | Full-build CI, generated versions and packaging work; registry confirmed working by user; immutable metadata/core-manifest versioning concerns remain |
| Verification evidence | 10 | 4 | 111 passing tests, artifact/dependency inspection and targeted probes; no live integration validation |
| **Total** | **100** | **64** | **Not ready for distribution as stable or RC** |

The registry correction restores three release-engineering points. Other category
scores and the verdict remain unchanged because independent blockers remain.

## 1. Review identity and comparison revisions

Review completed on **2026-10-19**, with remote checks during the review.

| Item | Recorded value |
|---|---|
| Branch | `feat/recode` |
| HEAD | `a78d0f5f925c2121121f8ef8a62122e74d6b8caa` |
| HEAD subject | `chore: fix workflow error` |
| Upstream | `origin/feat/recode`, ahead 0 / behind 0 |
| Initial working tree | Clean: no staged, unstaged or untracked changes reported |
| Final review working tree | Clean; HEAD unchanged |
| Latest collection `main` | `4c758107280396d325ad7912c2a2e2561e2e3cfe` |
| Merge base with `main` | `8fd1b876405a615b57a8e11f41e1eceac432a756` |
| Divergence | 76 branch-only commits; 2 main-only commits |
| Merge-base → branch diff | 557 files, +39,209 / −12,054 lines |
| Latest-main → branch endpoint diff | 556 files, +38,917 / −12,067 lines |

The two main-only commits concern workflows. The branch has its own current
build/publication workflows, which were inspected directly.

### Predecessor revisions

| Repository | Reviewed branch | Revision |
|---|---|---|
| [YVtils-MultiMine](https://github.com/YVtils/YVtils-MultiMine) | `master` / default HEAD | `609d87e5168209273f48c678ab82744eafe7f7f5` |
| [YVtils-DC](https://github.com/YVtils/YVtils-DC) | `master` / default HEAD | `1015d50f2235a163341c8d2cfb0c826b127df097` |
| [YVtils-SMP](https://github.com/YVtils/YVtils-SMP) | `master` / default HEAD | `8935ce3b099bc8bf8f240e3b3b8481d2aa535796` |
| [YVtils-SMP_CH1](https://github.com/YVtils/YVtils-SMP_CH1) | `master` | `0d36be62d5afb99d0292b4cea65269145d6ad52a` |
| YVtils-SMP_CH1 | `dev` | `f76e0cab7d3b83d331523150ed1c2ebcb9a38273` |
| YVtils-SMP_CH1 | `dev-Chapter2` | `aac4b9b76dddb65c3f58ad96bbc4552437266f9d` |

Comparison repositories were cloned under `/tmp/opencode`. All **23 root/module
`AGENTS.md` files** were read before reviewing implementation.

No application source, documentation, runtime data, index or commits were changed
during the review. Gradle generated ordinary ignored build outputs and POMs. This
Markdown report was added afterward at the user's request; the clean-tree records
above describe the review itself, before this file was added.

## 2. Predecessor feature coverage

“Implemented” means present in current source, not live-validated.

| Predecessor | Useful behavior | Current coverage and disposition |
|---|---|---|
| MultiMine | Connected mining/timber, block-list administration, personal toggle, cooldown and durability | Retained and expanded with leaf decay, stronger chain limits and Paper player-break operations. Protection fix is incomplete for leaf decay. |
| DC | Chat bridge, console bridge, statistics, whitelist/account administration | Retained. Added modal registration, confirmations and Minecraft account-management GUI. Old `/help` and whitelist-import surfaces are absent; import is not a fresh-install requirement. `/help` retirement is not clearly documented. |
| SMP | Status, Sit, Vanish, moderation, Fusion, MultiMine, server presentation, waypoints, utility commands, Spawn Elytra | Broad feature coverage restored. Newer implementations add sharing, offline editing, warnings, recipe editing and richer configuration. Maintenance remains accidentally incomplete. |
| CH1 `master` | Start/reset/border/whitelist sequence, inventory inspection, status, Discord, custom crafting | Most general features have replacements. New SMP start is a redesigned cinematic reset sequence, not equivalent to the old one-shot, configurable whitelist/start workflow. |
| CH1 `dev` | Expanded administration, logging, timber/vein mining, inventory editing | Administration largely covered. General-purpose event logging is explicitly retired in `docs/breaking-release.md:8–10`; no blocker assigned for its absence. |
| CH1 `dev-Chapter2` | Maintenance admission enforcement, Vanish settings, inventory sync, custom recipes | New InvUI editing/settings replace old GUI mechanisms. Maintenance admission is a regression. The old glowing/unbreakable Netherite Elytra recipe is absent from current defaults; retirement intent needs clarification. |
| Collection `main` | Multiple launchers, older configuration/GUI architecture, earlier Regions | Replaced by one launcher, active Configurate `:config`, shared InvUI variants and WorldGuard-backed claims. Standalone launchers and migration are intentionally retired. |

Legacy data migration, old permissions and old addon API compatibility were not
scored as requirements.

## 3. What is implemented and supported by evidence

### Architecture and packaging

The intended classloader boundaries are reflected in source and artifacts:

- Bootstrap loader/config/registry remain Java.
- Core embeds the common/config/utils runtime as `embedded/yvtils-runtime.jar`.
- Regions is in the main plugin JAR; WorldGuard is not shaded.
- Feature publication uses ordinary Maven JARs with shared project dependencies
  excluded from POMs.
- Exactly one GUI variant is selected by server version.
- Core tracks initialized modules and attempts reverse-order shutdown, continuing
  past individual lifecycle exceptions.

Implementation:
`core/src/main/kotlin/yv/tils/core/YVtils.kt:58–70,113–134,140–173,233–238`.

Artifact inspection found:

- No top-level Kotlin, coroutines, CommandAPI, common/config/utils, InvUI or
  WorldGuard implementation classes in the deployable core.
- Shared classes present in the embedded runtime.
- No duplicate ZIP entry names.
- No shared-runtime implementation classes inside the 17 ordinary published
  feature/GUI JARs.
- Main/runtime overlap limited to 32 annotation classes, not shared mutable runtime
  implementations.

All generated POMs still declare Kotlin stdlib `2.4.10`. This is redundant with the
embedded runtime, but both resolve in the intended library tier; that alone is not
evidence of a second Kotlin class identity.

### High-risk safeguards genuinely present

| Area | Source-supported implementation | Evidence limit |
|---|---|---|
| Configuration | Same-directory temporary writes and atomic replacement; malformed typed reads throw | Persistence tests pass; not every feature validates semantic values |
| Inventory editing | Clone-based planning, target locks, stale-slot checks, target-first commit, deferred drag checks, pre-login handoff | Layout/transfer tests pass; real InvUI/NBT/login conservation remains unverified |
| Vanish | Layers, capability permission + global switch + preference, collision restoration, real-quit suppression | Policy/persistence tests pass; cross-feature leaks remain |
| Silent containers | Read-only InvUI items, main-thread polling, distance/chunk/capability closure, no copy-back | Strong source evidence; real double-chest/hopper behavior unverified |
| Waypoints | PUBLIC/PRIVATE/UNLISTED/SHARED rules, access-checked clickable list, stale mutation checks | Policy/sharing tests pass |
| Trails | Private nonpersistent displays; bounded terrain search with loaded-chunk checks | Direction tests pass; client visibility/cleanup unverified |
| Moderation | Save-before-map mute/warn updates, main-thread expiry cleanup, concurrent chat reads, action-specific GUI checks | Formatting/login tests pass; live punishments unverified |
| Discord accounts | Shared mutex, stale snapshots, whitelist rollback, atomic account commit, role failures reported as partial success | Policy/transaction tests pass; real Bukkit/JDA transaction behavior unverified |
| Discord logging | Narrow transient classification, deferred log emission, classloader ownership check, filter removal | Logging tests pass; outage/reconnect behavior unverified |
| Fusion | Max-flow ingredient allocation, cloned plans, permission/stale checks, XP/inventory rollback, strict recipe storage | Allocation/pricing tests pass; full crafting event/GUI transaction unverified |
| Dragon | Bounded ticker/scanning, terrain journal before placement, owned-marker recovery, original-health marker | Math/director tests pass; crash recovery and encounter behavior unverified |
| SMP cinematic | Generation-scoped callbacks, snapshots, phase-failure cancellation, quit-triggered stop | Only ambient interval tests exist; state restoration needs live checks |

## 4. Prioritized findings

### Evidence labels

- **Confirmed:** directly observed by build, artifact inspection, dependency
  inspection or executable probe.
- **Source-supported:** a concrete failure path is established by source; not
  reproduced on a live server.
- **Unverified risk:** remaining integration uncertainty, listed separately rather
  than treated as a proven defect.

Line references apply to the recorded review HEAD.

### F1 — Registry-access finding withdrawn

**Status:** Excluded from defects and scoring. **Release blocker:** No.
**Evidence:** User reports successful testing and confirms Reposilite is online and
working. The review environment's unsuccessful downloads are not used to infer a
registry outage, unavailable release artifacts or a deployment failure.

The identifier is retained so references to F2–F17 remain stable. Clean-cache Paper
startup and matching artifact/version checks remain normal release acceptance
steps, not remediation for an asserted registry defect.

### F2 — Offline inventory access is incompatible with advertised Paper 26.2 and 26.3

**Severity:** High. **Release blocker:** Yes.
**Evidence:** Confirmed dependency incompatibility.

Reference:
`essentials/src/main/kotlin/yv/tils/essentials/inventory/OfflineInventoryStorage.kt:34–38`.

The adapter uses:

```kotlin
server.javaClass.getField("playerDataStorage")
```

`Class.getField` finds public fields only. Inspection of resolved mapped server
JARs showed:

| Paper target | Field visibility |
|---|---|
| 26.1.2 | `public final` |
| 26.2 | `protected final` |
| 26.3 | `protected final` |

**Scenario:** Offline `/invsee` or `/ecsee` on 26.2/26.3 fails with a reflected field
lookup error. The exception is caught, appearing as inventory load failure rather
than compilation failure.

**Fix:** Use a supported accessor or a version-aware, tested reflection adapter
that handles superclass-declared fields.

**Verify:** Offline inventory/ender-chest load, edit and relogin on all three Paper
versions, including equipment and custom components.

### F3 — Administrative config/module GUIs remain usable after permission revocation

**Severity:** High. **Release blocker:** Yes. **Evidence:** Source-supported.

References:

- `core/src/main/kotlin/yv/tils/core/commands/gui/YVtilsModulesGui.kt:249–255,276–285`
- `core/src/main/kotlin/yv/tils/core/commands/gui/YVtilsConfigGui.kt:96–100`
- `gui-26.1/src/main/kotlin/yv/tils/gui/logic/DataClassConfigGui.kt:392–431,498–506`
- `multiMine/src/main/kotlin/yv/tils/multiMine/logic/ManageGUI.kt:16–23`
- `server/src/main/kotlin/yv/tils/server/configs/ManageGUI.kt:15–22`
- `discord/src/main/kotlin/yv/tils/discord/configs/ManageGUI.kt:15–22`
- `status/src/main/kotlin/yv/tils/status/configs/ManageGUI.kt:15–22`
- `gui-26.1/src/main/kotlin/yv/tils/gui/logic/CommonConfigGui.kt:46–53`

Command entry is permission-gated, but these click/save paths do not recheck
authority. Several editors receive the **live mutable config object**, so changes
reach runtime state before saving. A failed save does not undo those mutations.

**Scenario:** Staff opens management, loses permission, then continues changing
settings or enabling modules for the next restart. An invalid/unsaved edit can
remain active in memory.

**Fix:** Authorization at mutation/commit boundaries; detached drafts, validation,
stale-state checks and save-before-apply. Essentials provides a useful local pattern.

**Verify:** Revoke permission while each screen/input is open; attempt mutation and
close/save. Inject write failure and verify live state and file bytes are unchanged.

### F4 — Leaf decay bypasses protection and has unbounded spatial work

**Severity:** High. **Release blocker:** Yes.
**Evidence:** Source-supported, corroborated by Paper implementation inspection.

Reference:
`multiMine/src/main/kotlin/yv/tils/multiMine/logic/LeaveDecayHandler.kt:112–128,158–180`.

Leaf removal calls `breakNaturally`, without player protection checks or a
cancellable leaves-decay event. Paper's inspected implementation drops/removes the
block directly; it does not enter the player `BlockBreakEvent` path.

Initial/rescan work traverses the entire affected bounding volume synchronously;
neighbor expansion has no explicit area/chunk/work cap.

**Scenario:** Mining an allowed log beside a protected area removes eligible
nonpersistent leaves inside that area despite denied leaf decay. A long sparse
connected run produces a large bounding volume and main-thread scan spikes.

**Fix:** Respect the intended leaf/protection event policy; restrict processing to
loaded chunks and explicit spatial/work budgets. Make discovery incremental.

**Verify:** WorldGuard boundaries with `leaf-decay` denied, sparse long/diagonal log
structures, and tick-time measurements.

The player-break fix for chained ore/logs is real; it does not complete protection
coverage for the whole feature.

### F5 — Shared coroutine IDs collide, and cancellation retains task records

**Severity:** High. **Release blocker:** Yes. **Evidence:** Confirmed executable probe.

Reference:
`utils/src/main/kotlin/yv/tils/utils/coroutine/CoroutineHandler.kt:21,29–30,74–100,111–138,145–159`.

Task IDs use millisecond timestamps. Registries are unsynchronized mutable maps
accessed from caller and IO threads. Completion cleanup is not in `finally`;
cancellation does not remove the name record.

Probe against the built embedded runtime:

```text
launched=100 distinct_ids=9 task_records=100
cancelled_task_record_retained=true
records_after_cancel_all=100
```

**Scenario:** Account/emoji/save jobs launched in one millisecond overwrite the
ID-to-job entry. Cancellation targets the wrong/latest job, completed/failed jobs
leave stale names, and named tasks cannot restart.

**Fix:** Unique IDs, synchronized/concurrent registration, registration before
execution, completion cleanup, and consistent removal from both registries.

**Verify:** Same-millisecond stress, concurrent launch/complete/cancel, throwing
tasks, named restart, and shutdown with zero retained records.

### F6 — Maintenance does not enforce admission and kicks players when disabled

**Severity:** High. **Release blocker:** Yes.
**Evidence:** Source-supported; missing load call independently checked.

References:

- `server/src/main/kotlin/yv/tils/server/listeners/PlayerLogin.kt:19–23`
- `server/src/main/kotlin/yv/tils/server/maintenance/MaintenanceHandler.kt:38–57,60–75,114–121`
- `server/src/main/kotlin/yv/tils/server/ServerYVtils.kt:89–93`

Three problems:

1. Login listener is empty.
2. `globalAnnouncement()` kicks non-bypass players on either transition, including
   turning maintenance off.
3. `loadState()` has no callers; startup loads config without initializing the
   handler/shared flag.

**Scenario:** Enabling maintenance kicks players, but they reconnect immediately.
Disabling it kicks them again. Restart leaves persisted/runtime state inconsistent.

**Fix:** One validated transition service: persist first, initialize from disk,
update shared status, kick only when enabling, enforce admission without overriding
existing denials.

**Verify:** Enable/disable/restart; OP, bypass-granted and ordinary players;
failed-save rollback; Discord status agreement.

This is an accidental omission: predecessor `dev-Chapter2` enforced admission.

### F7 — Inventory read permission revocation does not stop live observation

**Severity:** High. **Release blocker:** Yes, for the promised permission model.
**Evidence:** Source-supported.

Reference:
`essentials/src/main/kotlin/yv/tils/essentials/inventory/InventoryEditor.kt:35–45,116–118`.

Transactions recheck inspection/edit permissions, but the live-refresh ticker does
not check inspection permission. Offline snapshots also remain open.

**Scenario:** A moderator loses `/invsee` permission. Edits stop, but live changes
remain visible.

**Fix:** Close sessions when the viewer disconnects or loses read permission;
authorize before refresh.

**Verify:** Revoke read/edit independently during online/offline player and
ender-chest sessions.

### F8 — Command permission declarations overwrite one another

**Severity:** High authorization defect. **Release blocker:** Yes.
**Evidence:** Confirmed CommandAPI semantics; source-supported command impact.

References:

- `status/src/main/kotlin/yv/tils/status/commands/StatusCommand.kt:27–34,44–46,69–74`
- `sit/src/main/kotlin/yv/tils/sit/commands/SitCommand.kt:21–23`
- `message/src/main/kotlin/yv/tils/message/commands/MSGCommand.kt:21–23`
- `message/src/main/kotlin/yv/tils/message/commands/ReplyCommand.kt:25–27`
- `server/src/main/kotlin/yv/tils/server/maintenance/MaintenanceCMD.kt:24–26`

CommandAPI 12.1.0 `withPermission` methods assign one permission field; they do not
combine requirements.

**Scenario:** Denying a documented Status/Sit/Message node does not deny the command
after `CommandPermission.NONE` replaces it. A non-OP moderator granted maintenance
remains blocked by the subsequent OP requirement.

**Fix:** Register permission defaults through `PermissionManager`, then attach the
named node once. Use explicit requirements where conjunction is intended.

**Verify:** OP/non-OP, grant/deny, module wildcard and `yvtils.*` matrix using the
actual command dispatcher.

### F9 — Status GUI bypasses command validation and retained authorization

**Severity:** Medium. **Release blocker:** Yes for stable Status administration.
**Evidence:** Source-supported.

References:

- `status/src/main/kotlin/yv/tils/status/logic/StatusManager.kt:39–43,65–67,145–147,205–207`
- `status/src/main/kotlin/yv/tils/status/logic/StatusHandler.kt:25–42`
- `status/src/main/kotlin/yv/tils/status/utils/StatusUtils.kt:73–77`

GUI changes call `setStatusDisplay` directly, bypassing visible-length validation.
No action-time permission check protects an open other-player editor.
Display/tab/team changes precede persistence.

**Scenario:** Revoked staff continues editing another player; anvil input exceeds
`maxLength`; a failed save leaves displayed/persisted statuses different.

**Fix:** Centralize actor authorization, validation and transactional persistence/
display application.

**Verify:** GUI/command parity, revoked permission, stale/disconnected targets and
failed saves.

### F10 — Audio stop during loading does not close resources or reject late completion

**Severity:** High. **Release blocker:** Yes if `yv-smp` music is included.
**Evidence:** Source-supported.

References:

- `yv-smp/src/main/kotlin/yv/tils/yv_smp/logic/music/VoiceChatAudioPlayer.kt:108–126,169–174,200–215,308–345`
- `yv-smp/src/main/kotlin/yv/tils/yv_smp/logic/music/MusicHandler.kt:117–126,155–180`
- `yv-smp/src/main/kotlin/yv/tils/yv_smp/logic/music/VoiceChatAudioPlayer.kt:96–105`

Resources/periodic processing are allocated before `running=true`. `stop()` returns
without cleanup while loading. A later callback can start playback after removal
from `activePlayers`. The shared LavaPlayer manager is not shut down by `shutdown()`.

**Scenario:** Start a slow URL, then stop, replace, disconnect or disable. The old
callback starts untracked playback, or periodic tasks/codecs remain allocated.

**Fix:** Separate loading/running/closed states; unconditional idempotent close;
reject stale callbacks; conditionally remove the same instance; close the manager.

**Verify:** Delayed loading with stop/replacement/disconnect/shutdown before
completion; zero active tasks/codecs and no late audio.

### F11 — Vanish is exposed through server presentation and Discord disconnect sync

**Severity:** Medium. **Release blocker:** Yes for coherent staff visibility.
**Evidence:** Source-supported.

References:

- `utils/src/main/kotlin/yv/tils/utils/player/PlayerUtils.kt:56–75`
- `server/src/main/kotlin/yv/tils/server/motd/DisplayMOTD.kt:21–23`
- `server/src/main/kotlin/yv/tils/server/motd/GenerateContent.kt:73–80`
- `discord/src/main/kotlin/yv/tils/discord/logic/sync/serverChats/SyncPlayerConnectionChange.kt:33–38`
- `vanish/src/main/kotlin/yv/tils/vanish/listeners/VanishListener.kt:35–39`

Server name/count helpers include hidden players. Discord quit sync does not
consult the hidden predicate or suppressed quit message; sync permission defaults
true.

**Scenario:** A hidden moderator appears in a configured MOTD, or their real
disconnect is announced to Discord after an earlier fake departure.

**Fix:** Define public/administrative player views and apply the hidden predicate
consistently to public presentation/event bridges.

**Verify:** Two clients, server-list ping and Discord with Server/Status/Vanish
independently enabled/disabled.

### F12 — Core-only first use registers GUI translations too late

**Severity:** Medium. **Release blocker:** No; fix before stable polish.
**Evidence:** Source-supported.

References:

- `config/src/main/kotlin/yv/tils/config/ConfigYVtils.kt:52–54`
- `config/src/main/kotlin/yv/tils/config/language/Language.kt:91–103`
- `gui-26.1/src/main/kotlin/yv/tils/gui/core/InvUIBootstrap.kt:93–107`
- `core/src/main/kotlin/yv/tils/core/YVtils.kt:47–51,159–164`

On fresh core-only boot, no feature necessarily calls GUI bootstrap before
language files are built. Opening config later registers strings but does not
rebuild/load them.

**Scenario:** First-use controls display raw `action.gui.*` keys.

**Fix:** Register GUI translations before language loading, independently of window
initialization.

**Verify:** Fresh data directory, optional modules disabled, root/module GUIs in
English/German.

### F13 — Module configuration is neither atomic nor duplicate-safe

**Severity:** Medium. **Release blocker:** No for RC; resolve for stable.
**Evidence:** Source-supported.

References:

- `core/src/main/java/yv/tils/core/loader/ModuleConfig.java:86–94,114–119,204–224`
- `core/src/main/kotlin/yv/tils/core/loader/DynamicModuleDriver.kt:44–62`

`modules.yml` is truncated/replaced directly. Duplicate true entries instantiate a
module repeatedly.

**Scenario:** Interrupted write leaves a partial configuration; duplicated lines
register duplicate listeners/tasks or cause command-registration failure.

**Fix:** Atomic write, unique validated keys and useful malformed-input diagnostics.

**Verify:** Duplicate keys, write-failure simulation and preservation of old bytes.

### F14 — Console-sync state has races and an unbounded history list

**Severity:** Medium. **Release blocker:** No; fix before sustained production use.
**Evidence:** Source-supported.

References:

- `discord/src/main/kotlin/yv/tils/discord/logic/sync/serverConsole/GetConsole.kt:32–43,59–79,98–110,159–164`
- `discord/src/main/kotlin/yv/tils/discord/logic/sync/serverConsole/SendCMD.kt:49–54`
- `discord/src/main/kotlin/yv/tils/discord/logic/AppLogic.kt:108–115,236–242`

The producer queue has a rough limit, but `messageIDs` grows until a command clears
history. `clearHistory()` mutates shared state without coordinating with processing.
Shutdown stops a newly constructed appender without removing the installed one.

**Scenario:** Long-running forwarding retains every sent ID; simultaneous reset and
forwarding corrupt buffer state or lose output.

**Fix:** Serialize state ownership, bound/remove history, retain/remove the actual
installed appender.

**Verify:** Concurrent reset/logging stress, long-run memory and root-logger
attachment checks after shutdown.

### F15 — Configuration validation is inconsistent across modules

**Severity:** Medium. **Release blocker:** No single global blocker; affected
failures must be addressed before stable. **Evidence:** Source-supported.

Examples:

- `server/src/main/kotlin/yv/tils/server/motd/GenerateContent.kt:30–42`: editable
  empty MOTD lists reach `.random()`.
- `multiMine/src/main/kotlin/yv/tils/multiMine/configs/MultiMineConfigState.kt:39–52`:
  unrestricted numeric values.
- `multiMine/src/main/kotlin/yv/tils/multiMine/utils/CooldownUtils.kt:40–52`: negative
  cooldown never reaches zero.
- `moderation/src/main/kotlin/yv/tils/moderation/configs/saveFile/MuteSaveFile.kt:25–34`
  and `moderation/src/main/kotlin/yv/tils/moderation/utils/TargetUtils.kt:79–85,133–136`:
  expiry strings loaded without numeric validation, parsed during chat/cleanup.

**Scenario:** Removing the last MOTD entry breaks ping rendering; negative cooldown
disables later mining; malformed expiry throws during enforcement instead of load.

**Fix:** Validate complete candidates on load/save; reject invalid records before
publishing state.

**Verify:** Boundary/empty/invalid-value tests through disk load and GUI commit.

### F16 — Some cleanup sequences stop at the first internal failure

**Severity:** Medium. **Release blocker:** Conditional on failure-injection results.
**Evidence:** Source-supported.

References:

- `remade-ender-dragon/src/main/kotlin/yv/tils/remadeEnderDragon/logic/DragonFight.kt:1310–1327`
- `remade-ender-dragon/src/main/kotlin/yv/tils/remadeEnderDragon/RemadeEnderDragonYVtils.kt:57–69`
- `sit/src/main/kotlin/yv/tils/sit/SitYVtils.kt:45–47`
- `sit/src/main/kotlin/yv/tils/sit/logic/SitManager.kt:46–61`

Dragon cleanup restores terrain before original HP; a terrain-save failure can
skip HP restoration and subsequent listener cleanup. Sit has an empty disable
method and persistent, untagged seat entities.

**Scenario:** Shutdown with a failing journal write is partial. Disabling while
players sit can leave seats without an owned cleanup pass.

**Fix:** Independent cleanup with aggregated errors/finally; restore HP regardless
of terrain save; track/tag/remove seats explicitly.

**Verify:** Journal-save failure during disable; disable while seated; inspect
entities/listeners and dragon attributes.

### F17 — Existing unrelated command/session defects remain

**Severity:** Low–Medium. **Release blocker:** No individually.

| Defect | Reference | Scenario, fix and verification |
|---|---|---|
| `/speed reset <player>` uses `args[1]` after reading named target | `essentials/src/main/kotlin/yv/tils/essentials/commands/register/SpeedCMD.kt:71–83` | Target may be ignored or invalid positional access reached. Use named target; test console/player reset-other. Source-supported. |
| Reply cleanup assumes symmetric sessions | `message/src/main/kotlin/yv/tils/message/logic/MessageHandler.kt:26–29,75–80`; `message/src/main/kotlin/yv/tils/message/commands/ReplyCommand.kt:38–53` | A→B then A→C leaves B→A; A quits, B replies to null. Remove all references and null-check target; test three-player sequences. Source-supported. |
| Mojang filter reflects fields from wrong class | `moderation/src/main/kotlin/yv/tils/moderation/utils/MojangProfileLogFilter.kt:39–46` | Fields are on `Logger`, not `Logger.Companion`; confirmed by compiled-class inspection. Expose a normal query, test matching WARN events and unregister on disable. |
| Stats advertises unreachable acceptance commands | `stats/src/main/kotlin/yv/tils/stats/StatsYVtils.kt:150–209,319–322`; `core/src/main/kotlin/yv/tils/core/commands/register/YVtilsCommand.kt:25–45` | Prompt has no callers; commands absent. Wire current feature or remove broken promises; test opt-in/out. Source-supported. |

## 5. Separate disposition lists

### Confirmed defects / release conditions

Directly established:

1. Offline NBT field lookup conflicts with protected fields in 26.2/26.3.
2. Coroutine ID collisions/retained records reproduced against built runtime.
3. CommandAPI permission replacement verified from actual dependency.
4. Mojang filter reflection target mismatch confirmed by compiled-class inspection.
5. Branch-diff hygiene check fails on whitespace/EOF issues.

Reposilite operation is user-verified and is not a defect or release blocker (F1
withdrawn).

Concrete source-established defects requiring runtime/regression verification are
F3–F4, F6–F16 and remaining F17 entries. They are not live reproductions.

### Verification gaps

Explicitly **unverified**:

- Fresh Paper boot on 26.1.2, 26.2, 26.3 with clean library caches.
- Runtime identity/linkage for enabled-module combinations.
- Feature bytecode compiled against InvUI 2.1.1 running with 2.3.2/2.5.1. Compiling
  shared GUI sources three times does not validate every feature's binary calls.
- Item conservation across real clicks/drags, failed writes, disconnects/pre-login.
- External-file-change races between fingerprint check and atomic replacement.
- Real protection/durability/XP behavior for mining and leaf decay.
- Permission-plugin wildcard/negative-grant behavior.
- Multi-client Vanish, silent containers, waypoint privacy/display cleanup.
- Discord modal/role/REST/reconnect/outage/shutdown behavior.
- Voice-chat playback, codec format, cancellation and native cleanup.
- Dragon balance, crash recovery, loaded/unloaded chunk cleanup and journal failure.
- Cinematic cancellation in every phase with Vanish and Status active.

**Specific interaction risk:** SMP uses the same `Core.instance` hide/show ownership
as Vanish and restores captured visibility:
`yv-smp/src/main/kotlin/yv/tils/yv_smp/logic/start/CinematicRuntime.kt:29–55`.
A Vanish change during a cinematic can conflict with restoration until the next
Vanish refresh. The exact visible outcome needs a live two-client test.

No live Paper/client, Discord or voice-chat validation was performed.

### Maintainability and documentation concerns

- Stale contributor guide recommends stale generator/removed APIs:
  `docs/adding-a-new-module.md:40–49,103–112,256–296,379–382`.
- Inventory README contradicts shift/drag and login handoff:
  `essentials/README.md:17–25`.
- Root README gives wrong Waypoint defaults and omits SHARED in summary:
  `README.md:189–203`; actual defaults true at
  `waypoint/src/main/kotlin/yv/tils/waypoint/permissions/Permissions.kt:51–64`.
- Removed local head helper documented at `moderation/README.md:9`.
- Stats/Server/Status schemas contain stale GUI/prompt claims.
- Version-edit instructions stale at
  `utils/src/main/kotlin/yv/tils/utils/modules/Module.kt:24–34`.
- Both comparators sort prereleases lexically (`dev.10` before `dev.9`):
  `common/src/main/kotlin/yv/tils/common/updateChecker/PluginVersion.kt:242–247`;
  `core/src/main/kotlin/yv/tils/core/CheckVersion.kt:219–224`.
- Publication skip verifies JAR equality, not existing POM/module metadata equality:
  `build.gradle.kts:398–430`. Dependency-only changes need immutable-metadata checks.
- Core embeds the generated version map; feature-version changes alter core bytes
  even without a `core=` bump. Modrinth skips an existing core version:
  `core/build.gradle.kts:60–107`;
  `.github/workflows/publish-maven.yml:120–133`. Coordinate core version bumps with
  every changed deployable manifest.
- Paper dev bundles use `build.+`: `build.gradle.kts:230–231`; exact builds drift.
- Local `core/build/libs` contains older JARs. Select the exact current artifact.
- Shadow metadata warnings and Gradle-10 deprecations remain; no runtime defect
  inferred solely from them.

## 6. Code-size, density and redundancy inventory

Fresh measurements:

- **398 main Kotlin/Java files; 45,686 physical lines**.
- **31 test files; 1,864 physical lines**.
- Test/main line ratio **4.08%**, not coverage.
- Inventory scanned **453 source/build files**.
- **18 files ≥400 lines**, including root build.
- **126 main-source dense-line candidates** using width/semicolon heuristic. Most
  are translations, textures or annotations.

### Module size

| Module | Main files | Main lines | Test lines |
|---|---:|---:|---:|
| yv-smp | 34 | 7,505 | 37 |
| discord | 53 | 5,538 | 288 |
| regions | 30 | 4,425 | 652 |
| essentials | 44 | 3,622 | 172 |
| moderation | 36 | 2,959 | 86 |
| remade-ender-dragon | 11 | 2,728 | 143 |
| fusion | 27 | 2,379 | 108 |
| stats | 10 | 2,178 | 0 |
| gui-26.1 shared source | 13 | 2,023 | 80 |
| core | 12 | 1,873 | 0 |
| multiMine | 18 | 1,791 | 37 |
| config | 19 | 1,738 | 106 |
| utils | 14 | 1,279 | 0 |
| waypoint | 14 | 1,159 | 96 |
| common | 11 | 1,137 | 0 |
| status | 14 | 1,116 | 0 |
| vanish | 12 | 831 | 59 |
| server | 14 | 799 | 0 |
| message | 6 | 334 | 0 |
| sit | 6 | 272 | 0 |

GUI 26.2/26.3 reuse one source tree, not duplicate implementations.

### Largest behavioral files

| File | Lines |
|---|---:|
| `remade-ender-dragon/src/main/kotlin/yv/tils/remadeEnderDragon/logic/DragonFight.kt` | 1,334 |
| `regions/src/main/kotlin/yv/tils/regions/gui/ClaimsGui.kt` | 1,015 |
| `yv-smp/src/main/kotlin/yv/tils/yv_smp/utils/CinematicCamera.kt` | 741 |
| `gui-26.1/src/main/kotlin/yv/tils/gui/logic/DataClassConfigGui.kt` | 718 |
| `stats/src/main/kotlin/yv/tils/stats/registry/StatsRegistry.kt` | 631 |
| `stats/src/main/kotlin/yv/tils/stats/logic/StatsPusher.kt` | 492 |
| `gui-26.1/src/main/kotlin/yv/tils/gui/logic/ConfigGui.kt` | 477 |
| `yv-smp/src/main/kotlin/yv/tils/yv_smp/gui/YvSmpGui.kt` | 463 |

Executable density candidates: `DragonFight.kt:196,725,1067,1318–1319`,
`ClaimService.kt:273,305,355`, `FusionGui.kt:243,417`.

### Within-module consolidation candidates

- GUI: parallel navigation/input/list/save mechanics in `ConfigGui.kt` and
  `DataClassConfigGui.kt`.
- Moderation: repeated async profile wrappers in `commands/*Command.kt`; repeated
  conversion/action metadata/broadcast handling in `logic/*Logic.kt`.
- Discord: repeated event bridges and account UI/command feedback. Keep mutations
  centralized in `AccountService`.
- SMP: repeated phase scan/effect setup and camera scheduling/cancellation.
- Dragon: shared targeting, cues, tagging/cleanup scaffolding across attacks.
- Regions: repeated snapshot/mutate/save/rollback across claims/flags/subzones.

### Cross-module consolidation candidates

- Common/Core remote-version checkers.
- Permission-list/wildcard boilerplate across eight modules.
- Typed config load/flatten/getter facades.
- UUID-indexed list save wrappers.
- Pagination/back/confirmation/anvil controls across Core, Status, Claims, Fusion,
  Discord.
- Translation-registration scaffolding.
- HTTP connection/timeout/response cleanup helpers.
- Shared-dependency/test Gradle declarations.

These are candidates, not recommendations for broad pre-release refactoring. Fix
correctness contracts first.

## 7. Exact verification performed

### Toolchain and build

```sh
java -version
./gradlew --version
./gradlew build --no-daemon
```

Results:

- OpenJDK **25.0.4.1**.
- Wrapper **9.6.1**.
- Full build **SUCCESS**: 159 actionable tasks, 16 executed, 143 up-to-date.
- Shadow Kotlin metadata duplicate-strategy and Gradle deprecation warnings.

Because the full build reused tests, all existing test tasks were re-executed:

```sh
./gradlew :config:test --rerun :discord:test --rerun :essentials:test --rerun :fusion:test --rerun :gui-26.1:test --rerun :moderation:test --rerun :multiMine:test --rerun :regions:test --rerun :remade-ender-dragon:test --rerun :vanish:test --rerun :waypoint:test --rerun :yv-smp:test --rerun --no-daemon
```

**SUCCESS — 111 tests, 0 failures, 0 errors, 0 skipped.**

| Suite | Tests |
|---|---:|
| config | 6 |
| discord | 12 |
| essentials | 11 |
| fusion | 11 |
| gui-26.1 | 4 |
| moderation | 4 |
| multiMine | 2 |
| regions | 37 |
| remade-ender-dragon | 12 |
| vanish | 3 |
| waypoint | 6 |
| yv-smp | 3 |

These mostly cover policies, algorithms, formatting, persistence helpers and mocked
transactions, not live acceptance.

### Publication/artifact checks

Generated all 17 POMs, without publishing:

```sh
./gradlew :vanish:generatePomFileForMavenPublication :waypoint:generatePomFileForMavenPublication :fusion:generatePomFileForMavenPublication :discord:generatePomFileForMavenPublication :multiMine:generatePomFileForMavenPublication :essentials:generatePomFileForMavenPublication :sit:generatePomFileForMavenPublication :status:generatePomFileForMavenPublication :server:generatePomFileForMavenPublication :message:generatePomFileForMavenPublication :moderation:generatePomFileForMavenPublication :stats:generatePomFileForMavenPublication :gui-26.1:generatePomFileForMavenPublication :gui-26.2:generatePomFileForMavenPublication :gui-26.3:generatePomFileForMavenPublication :yv-smp:generatePomFileForMavenPublication :remade-ender-dragon:generatePomFileForMavenPublication --no-daemon
```

**Result:** Success; no publication tasks invoked.

Inspected all POM dependency lists, 17 ordinary publication JARs, generated version
metadata/Java registry, deployable core/nested runtime, class-file targets and
relevant Paper/NMS/CommandAPI implementations. Public registry download attempts
from the review environment are excluded from the readiness assessment following
the user's successful registry test. No independent successful registry download
is claimed by the reviewer.

Reviewed deployable artifact:

```text
core/build/libs/YVtils_v26.10.01-dev.6.jar
Size: 8,832,058 bytes
SHA-256:
13bf3e1110595241e66446bd0d205bdf5a281f122088dd46565cf5a6c91c09ce
```

### Supported versions

Source permits exactly:

| Paper Minecraft version | GUI artifact | InvUI |
|---|---|---|
| 26.1.2 | `gui-26.1` | 2.1.1 |
| 26.2 | `gui-26.2` | 2.3.2 |
| 26.3 | `gui-26.3` | 2.5.1 |

Java **25** is required. Loader rejects other exact Minecraft IDs, including
unlisted patches. GUI documentation's “x” wording does not broaden launcher support.

### Other checks

- Read-only coroutine probe reproduced collisions/retention.
- `javap`/decompilation confirmed field incompatibility/logger reflection mismatch.
- `python3 scripts/code-inventory.py` completed.
- `git diff --check`: working-tree diff clean.
- `git diff --check origin/main HEAD`: failed on branch whitespace/EOF issues.
- Remote `main` checked again; revision unchanged.
- HEAD GitHub build success:
  [run 39289468868](https://github.com/YVtils/yvtils_collection/actions/runs/39289468868).
- `gh` unavailable; CI status read through public GitHub API.
- No server, Discord bot, voice-chat session or publication started.

## 8. Ordered checklist to reach stable

1. **Fix shared correctness/authorization blockers.**
   Coroutine identity/synchronization/cleanup; runtime GUI authorization and
   detached config transactions; command permissions; inventory read revocation;
   Status validation/transaction parity.
2. **Fix protection and advertised compatibility.**
   Leaf-decay events/budgets; offline adapter on 26.2/26.3; maintenance admission,
   transition direction and startup state.
3. **Close resource/interaction gaps.**
   Audio loading cancellation; public Vanish filtering; best-effort Dragon cleanup;
   Sit cleanup; console-sync ownership/bounds/appender removal.
4. **Add focused regressions.**
   Exercise real adapter/API boundaries where mocks hid incompatibilities. Include
   task stress, revoked-GUI access, invalid values and save failures. Test behavior,
   not implementation structure.
5. **Resolve release semantics/documentation.**
   Decide CH1 recipe/start retirements. Correct inventory behavior, Waypoint defaults,
   Stats interactions and contributor guides. Version core as a feature manifest.
6. **Prepare fresh immutable versions/artifacts.**
   Update affected modules/core; compare generated POM/module metadata; build the
   exact intended launcher.
7. **Verify the final release artifact set.**
   With registry operation confirmed by the user, follow normal release ordering
   and compare final POM/JAR/checksums with the tested build. This is an artifact
   acceptance step, not a registry-availability blocker.
8. **Run clean-cache Paper matrix.**
   All three versions; core-only, individual/all normal/manual modules; WorldGuard
   absent/present, voice chat absent/present, Discord unavailable/configured. Inject
   partial startup/shutdown failures.
9. **Complete multi-client/data-integrity acceptance.**
   Inventory conservation/login races; mining protection/durability/limits;
   Vanish/Status/Server/Discord combinations; waypoint sharing/privacy/chunks;
   punishment expiry/login screens; Fusion XP/recipes; Dragon recovery; every
   cinematic cancellation phase.
10. **Promote beta → RC → stable using recorded outcomes.**
    Re-run full build after fixes. Record server builds, hashes and acceptance
    results. Require zero unresolved blockers and a passing live matrix.

**Bottom line:** substantial feature coverage and credible architecture are present,
but the passing build is ahead of release evidence. Registry operation is confirmed
by the user and excluded as a blocker. Fix the remaining concrete blockers and
validate Paper/JDA/InvUI/voice-chat boundaries before stable.
