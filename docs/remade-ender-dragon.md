# Remade Ender Dragon

## Installation and activation

The module is registered with the collection's dynamic loader and publication
tasks as `remade-ender-dragon` (human-readable name: **Remade Ender Dragon**).
Publish/install the module artifact using the normal
[dynamic-module workflow](migrating-to-dynamic-modules.md), enable
`remade-ender-dragon: true` in `plugins/yvtils/modules.yml`, and restart.

Configuration is generated at `plugins/yvtils/remade-ender-dragon/config.yml`
(or beneath the configured config-v2 base directory). Keys are camelCase.
Edit the YAML and run `/redragon reload`. Reload stops and cleans up active
custom encounters; automatic modes rediscover eligible dragons afterward.
Invalid configuration is rejected before active encounters are stopped.

### In-game configuration (InvUI)

Open the module from `/yvtils config` or use `/redragon config`.
The module uses the collection's shared InvUI bootstrap, styled paged menus,
and `DataClassConfigGui` editor. The menu offers:

- **General settings:** edit scalar and string-list configuration values.
- **Activation:** cycle between EVERY, ADMIN and FIRST.
- **Attacks and support:** edit each attack's full settings, including cooldown,
  use budget, health gates, warning time, damage, radius, and scaling counts.

Enum/map navigation is supplied by the module because the shared generic
editor does not edit those types directly. Settings are edited in detached
drafts, checked against the same validation as YAML, and persisted on closing
an edited screen. Successful saves clean up active encounters before the next
automatic discovery; invalid drafts leave the live configuration unchanged.

The module registers English/German text through `BuildLanguage`, retrieves
messages using the recipient's `LanguageHandler` locale, and uses the shared
`MessageUtils` conversion/placeholder pipeline. Chat uses the collection prefix;
warnings use localized action bars. Attack labels, menus, status output and
command feedback are translated. Shared editor navigation and field controls
use the GUI module's existing translations and annotation conventions.

`enabled: false` disables starting encounters. `activation` selects:

| Value | Behavior |
|---|---|
| `EVERY` (default) | Automatically customize initial and respawned dragon fights. |
| `ADMIN` | Only `/redragon start` starts an encounter. |
| `FIRST` | Automatically customize a world's first dragon fight only. |

`FIRST` checks both vanilla's previous-dragon-kill flag and the module's
persisted `history.json`, keyed by world UUID. It does not treat installing
the module into an already-completed End as a new first fight. Explicit admin
starts can override the activation mode, but not the world allowlist or
`enabled` switch. A killed vanilla dragon also records completion while the
module is loaded. Attack budgets reset on a new encounter or server restart.

`/redragon stop` suppresses automatic restart for that dragon until an explicit
start or server restart. Only one custom encounter runs in each End world.

## Scaling and pacing

Counted fighters are living Survival/Adventure players within `islandRadius`
of `centerX, centerZ` in the encounter world. Spectators, Creative players,
and outer-island explorers do not count or receive custom attack damage.
Players temporarily above the island still count. Zero fighters pauses new
attacks and HP scaling; existing temporary hazards expire normally.

Let `n = clamp(fighters, 1, maxScalingPlayers)`:

- Dragon HP: `min(maxHealth, baseHealth + (n - 1) * healthPerExtraPlayer)`.
- Custom damage multiplier: `min(maxDamageMultiplier, 1 + (n - 1) * damagePerExtraPlayer)`.
- Attack count: `min(maxCount, baseCount + ceil((n - 1) * extraCountPerPlayer))`.
- Attack interval: `max(minimumAttackIntervalSeconds, attackIntervalSeconds - (n - 1) * intervalReductionPerExtraPlayer)`.
- Infestation coverage: `min(0.6, infestationCoverage + (n - 1) * infestationCoveragePerExtraPlayer)`.

Effective HP is updated once per second, preserving the dragon's **health percentage**.
The real attribute stays at or below 1,024 to respect server attribute caps;
incoming dragon damage and healing are converted to effective HP units through
normal events. Status reports effective HP; the boss bar shows health percentage.
Counts are captured at attack launch; damage checks the current party.
Scaling custom damage does not multiply vanilla dragon or monster melee damage.
Damage numbers are health points **before armor**; two points equal one heart.

Scripted attacks partially pierce armor and protection enchantments. With
`customAttackProtectionPiercing: 0.45` (default), 45% of each reduction is
ignored; 55% remains. This makes missed mechanics noticeable in Protection IV
netherite without raising unarmored damage. Set `0` for vanilla mitigation,
or increase toward `1` for full armor/enchantment piercing. Resistance,
absorption hearts, sanctuary, and event cancellation remain effective. Vanilla
dragon/monster melee and magnetism fall damage are not pierced. Damage still
uses normal events, armor durability, hurt feedback, and vanilla invulnerability
timing rather than direct health subtraction.

Default solo effective HP is 1,200, plus 900 per extra fighter, capped at 18,000 and 20
scaling fighters. Damage grows by 2.5% per extra player, capped at 1.4×.
New attacks start every 16 seconds solo, decreasing toward an eleven-second
floor. There is a 30-second initial grace period to map the loaded island.
The director respects cooldowns, stages, compatibility and use budgets. Basic
waves/effect areas/explosives continue on cooldown after their initial budgets.
Major attacks remain finite. Zero budgets and disabled attacks remain disabled.

Vanilla flight, perching, crystal healing, and dragon damage remain. Dragon-source
vanilla breath clouds are suppressed when `replaceVanillaBreath` is true;
falling effect areas supply the replacement custom breath mechanic.

## Attacks and counterplay

Attack announcements, instructional entity names and combat hints are **off by default**. Enable
`announceAttacks` for localized action-bar/chat announcements. Visual warnings
remain active independently: both dragon waves use bright block displays,
explosives use visible primed TNT, and effect areas launch actual dragon
fireball entities. Other circles/clouds still use particles; Reduced or All
particles is recommended for readable healing pools and effect areas.

| Attack ID | Default budget / cooldown | Behavior and counterplay |
|---|---|---|
| `crystals` | 1 / 120 s | Below 50% HP, glowing crystals ride one-hit silverfish toward a fixed convergence point shown by beams. Each arrival grants 5% temporary custom-damage empowerment, capped at 20%. Destroy either half for rewards; carriers expire after 35 seconds. |
| `island-wave` | 4 / 55 s | Orange block-display warning followed by an expanding magma-block ring across the island. Jump over the front; each fighter can be hit once for seven base damage. Displays are visual only, not solid placed blocks. |
| `effect-areas` | 6 / 30 s | After the warning, native dragon fireballs fly toward snapshots of fighter positions and form eight-second dragon-breath clouds where they hit. Clouds apply a random effect and three base damage per second. Move away before impact. |
| `explosives` | 5 / 35 s | Visible glowing primed TNT appears at fighter-position snapshots. After three seconds it detonates for seven base damage in the marked radius. Vanilla TNT explosion is canceled; scripted damage/visual explosion causes no block damage or fire. |
| `monsters` | 4 / 45 s | Scaled roster cycles through aggressive Endermen, blue Rift Endermen, and Shulkers (three base summons). Targets are refreshed toward fighters. Shared population/lifetime caps apply; blue kills grant pools, all player kills grant rally. |
| `dragon-wave` | 6, then sustained / 20 s | A nine-block magma-display wave radiates from the dragon's horizontal cast position after a 1.5-second warning, travelling at 18 blocks/second and dealing seven base damage. Jump over it. |
| `infestation` | 3 / 60 s | Large contiguous dry-grass patches appear under fighter-position snapshots and across the mapped island for seven seconds. Entry deals immediate damage and Slowness III; remaining in a patch causes three base damage every 0.75 seconds. Jump or leave the patches. |
| `magnetism` | 3 / 55 s | Below 75% HP during circling flight, pull **all current fighters**, including those joining the island during the cast. No ground markers. Lift roughly six blocks above the local surface for three seconds; next landing damage is capped at three points and cannot itself be fatal. Water/web catches still work. |
| `blue-endermen` | 5 / 50 s | Blue-glowing Rift Endermen. They telegraph an expanding shockwave in a 90° cone (−45° to +45° from their facing at cast). Sidestep, move behind them, or jump. Player kills create healing pools and team rally buffs. |
| `summon-areas` | 4 / 50 s | Below 75% HP: magenta circles warn for four seconds before a high-damage rift strike (12 base points). Leave them or kill the glowing focus to cancel the entire cast. |
| `marked-hunters` | Unlimited / 65 s | Middle stage onward: one or two fighters carry a purple marker for four seconds. It locks on ground, warns for 2.5 seconds, then forms a six-second patch dealing three base points per second. Bait away from teammates; selection has a 90-second per-fighter cooldown. |
| `rift-anchors` | 6 / 120 s | Middle stage onward: one or two glowing anchors last 25 seconds, increasing newly launched cloud duration by 25%. Player kills remove an anchor and grant ten seconds of 20% increased dragon damage. Expiry grants no reward. |
| `breath-sweep` | Unlimited / 60 s | Middle stage onward: a fixed 70° ground sector within 24 blocks is warned for four seconds, then swept over four seconds. Move out of the sector or behind the cast. The central four blocks are safe from this custom cast. |

Infestation never removes or replaces existing terrain. It only places short
dry grass into air above End stone, with physics suppressed while owned. The
original End-stone surface remains intact. Terrain is scanned incrementally
in **loaded chunks only**, so the module never force-loads the entire island.
The configured radius should encompass the main island, not outer islands.
Unloaded terrain joins the scan after loading. Obsidian towers and structures
are deliberately excluded from infestation surface selection.

Combat surface checks resolve loaded ground on demand, including bedrock and
player-built platforms; they no longer depend on the partially mapped
End-stone infestation cache. Fast waves check the full band travelled between
updates, preventing a ring from stepping past a fighter without hitting them.

Custom fireballs use native per-tick movement, normal acceleration and client
prediction rather than repeated teleports. Their built-in trail supplies the
flight visuals; no duplicate scripted trail is emitted. Impact starts the
custom cloud and cancels the vanilla impact damage/cloud. Missed projectiles
are removed after ten seconds; cloud duration starts at actual impact.

Magnetism avoids unmapped/void columns and large ground-height changes. The
landing cap applies to the next natural fall-damage event for at most ten
seconds after the final pull. It does not grant invulnerability to other
attacks. Slow Falling from an updraft or a successful catch may eliminate the
fall damage naturally. Debug can trigger magnetism outside its flight gate.

## Player-positive mechanics (all implemented)

1. **Healing/cleanse pools:** player kills of blue Endermen create green pools.
   Bright lime rings, dense green sparkles, and a vertical particle column make
   the pools visible sooner. Standing within 3.5 blocks restores one health point, one food point, and
   three durability points per damaged inventory item per second for 12 seconds.
   Armor/offhand are included. Selected negative effects are removed each pulse.
   A maximum of three pools prevents unlimited repair/healing stacking.
2. **Crystal-break rewards:** direct player/projectile destruction of vanilla
   or renewed crystals grants nearby fighters six seconds of Regeneration I
   and Absorption I within 16 blocks. Explosion-chain destruction does not
   independently grant rewards.
3. **Sanctuary zones:** marked green zones reduce incoming non-void damage to
   25% and grant brief Resistance I while occupied. They last 12 seconds.
4. **Interrupt objectives:** a stationary, glowing, damageable focus appears
   during high-damage summon areas. Fighters can kill it before the warning
   ends to cancel that cast. No terrain damage, loot, or focus duplication.
5. **Rescue updrafts:** cyan zones gently launch grounded fighters and grant
   eight seconds of Slow Falling. Each fighter gets two launches per encounter.
6. **Team rally:** player kills of custom monsters grant Strength I and Speed I
   for six seconds to fighters within 16 blocks.

Sanctuary and updraft casts alternate after every third successful offensive
cast, each with its own cooldown and unlimited default budget. Kill rewards mix the
remaining support mechanics into combat naturally. Support casts may use two
reserved effect slots beyond the normal attack cap. Pools have a separate cap.

## Global configuration reference

All durations are **seconds** unless otherwise specified. Radii and movement
distances are blocks. Lists use Minecraft registry IDs (e.g. `poison` or
`minecraft:poison`). Missing fields inherit defaults.

| Keys | Defaults | Purpose |
|---|---|---|
| `enabled`, `activation`, `worlds` | `true`, `EVERY`, `[]` | Master switch, start policy, exact End world names; empty list allows all End worlds. |
| `centerX`, `centerZ`, `islandRadius` | `0`, `0`, `96` | Fighter boundary, wave coverage, terrain scan; radius accepts 16–160. |
| `groundMinY`, `groundMaxY` | `40`, `90` | Surface scan height bounds; widen for customized islands. |
| `maxScalingPlayers`, `baseHealth`, `healthPerExtraPlayer`, `maxHealth` | `20`, `1200`, `900`, `18000` | Effective party/HP scaling and hard cap. |
| `damagePerExtraPlayer`, `maxDamageMultiplier` | `0.025`, `1.4` | Gentle custom damage scaling. |
| `customAttackProtectionPiercing` | `0.45` | Fraction of armor/enchantment reduction ignored by scripted attacks; 0 = vanilla, 1 = fully pierced. Does not bypass Resistance, absorption or sanctuary. |
| `attackIntervalSeconds`, `minimumAttackIntervalSeconds`, `intervalReductionPerExtraPlayer` | `16`, `11`, `0.25` | Global launch pacing. |
| `maxConcurrentAttacks` | `2` | Active normal casts; support/debug can use two additional reserved slots. |
| `maxSummons`, `summonLifetimeSeconds` | `24`, `50` | Shared custom-monster population cap and despawn timer. |
| `replaceVanillaBreath` | `true` | Suppress dragon-source vanilla breath clouds. |
| `announceAttacks` | `false` | Opt in to attack announcements and combat hints; command replies remain enabled. |
| `waveDisplaySegments`, `dragonWaveSpeedBlocksPerSecond` | `160`, `18` | Maximum display segments per dragon/island wave, and local wave propagation speed. |
| `crystalCarrierSpeed` | `1.2` | Silverfish ground pathfinding speed toward the dragon's projection. |
| `waveSpeedBlocksPerSecond`, `waveJumpHeight` | `10`, `0.65` | Ring speed and minimum feet height above the surface to dodge. |
| `infestationSpacing`, `infestationCoverage`, `infestationCoveragePerExtraPlayer`, `maxInfestedBlocks` | `4`, `0.22`, `0.01`, `1800` | Clear spacing between distributed patch anchors, density, party coverage increase, and placed-block cap. Fighter patches are seeded first. Coverage is a patch-selection density, not an exact island percentage. |
| `infestationPatchRadius`, `infestationSlowAmplifier`, `infestationPulseSeconds` | `3`, `2`, `0.75` | Contiguous patch radius, zero-based Slowness level (2 = III), and per-player repeat damage interval. First entry hits immediately. |
| `effectPool`, `effectDurationSeconds`, `effectAmplifier` | `[slowness, weakness, poison, blindness]`, `4`, `0` | Random area effect choices; zero-based amplifier (0 = level I). |
| `magnetLiftBlocks`, `magnetPullSpeed`, `magnetFallDamageCap` | `6`, `0.35`, `3` | Lift target, horizontal velocity per server tick, next landing damage cap; lift limited to 1–7. |
| `blueEndermanHealth` | `32` | Health points per special Enderman. |
| `blueEndermanWaveCooldownSeconds`, `blueEndermanWaveWarningSeconds`, `blueEndermanWaveRadius`, `blueEndermanWaveDamage` | `9`, `1.5`, `6`, `4` | Special Enderman frontal-wave tuning. |
| `healingPoolLifetimeSeconds`, `healingPoolRadius`, `maxHealingPools` | `12`, `3.5`, `3` | Green pool duration, radius, concurrency. Zero cap disables pools. |
| `healingPerSecond`, `foodPerSecond`, `durabilityPerSecond` | `1`, `1`, `3` | Pool health/food/repair pulse amounts; zero disables each component. Food also restores half as much saturation. |
| `cleansingEnabled`, `cleanseEffects` | `true`, `[poison, wither, slowness, weakness, blindness]` | Pool cleansing switch and effect list. |
| `crystalRewardsEnabled`, `crystalRewardRadius`, `crystalRewardSeconds` | `true`, `16`, `6` | Crystal-break support reward. |
| `rallyEnabled`, `rallyRadius`, `rallySeconds` | `true`, `16`, `6` | Custom-monster kill reward. |
| `interruptObjectives`, `focusHealth` | `true`, `12` | Enable cancelable strikes and set objective health. |
| `sanctuaryDamageMultiplier` | `0.25` | Sanctuary incoming damage fraction, excluding void damage. |
| `updraftMaxUsesPerPlayer`, `updraftSlowFallingSeconds` | `2`, `8` | Per-fighter launch limit and protection duration. |
| `supportEveryAttacks` | `3` | Successful offensive casts between alternating support casts. |
| `subtleCues` | `true` | Quiet sounds/particles without attack names. |
| `middlePhaseHealth`, `finalPhaseHealth` | `0.70`, `0.35` | Forward-only stage thresholds. |
| `middlePhaseIntervalMultiplier`, `finalPhaseIntervalMultiplier` | `0.90`, `0.80` | Stage pacing, respecting the global interval floor. |
| `recoverySeconds`, `phaseRecoverySeconds`, `customHitRecoverySeconds` | `4`, `8`, `1.25` | Post-cast/transition launch recovery and scripted-hit spacing. |
| `echoWavesEnabled`, `echoWaveGapSeconds`, `echoWaveDamageMultiplier` | `true`, `2`, `0.65` | Every second finale wave gains a weaker echo. |
| `crystalConvergenceEnabled`, `crystalConvergenceLifetimeSeconds`, `crystalConvergenceRadius` | `true`, `35`, `3` | Fixed crystal destination, carrier expiry, arrival distance. |
| `crystalEmpowermentSeconds`, `crystalEmpowermentPerArrival`, `maxCrystalEmpowerment` | `25`, `0.05`, `0.20` | Temporary arrival bonus to custom damage. |
| `markedLockWarningSeconds`, `markedRetargetSeconds` | `2.5`, `90` | Locked-position escape window and individual targeting cooldown. |
| `anchorHealth`, `anchorVulnerabilitySeconds`, `anchorVulnerabilityMultiplier`, `anchorCloudDurationMultiplier` | `24`, `10`, `1.20`, `1.25` | Anchor durability, kill reward and cloud empowerment. |
| `breathSweepDegrees` | `70` | Total warned sector angle. |

## Per-attack configuration

`attacks` is keyed by the exact attack IDs in the attack table. `sanctuary`,
`rescue-updraft`, and `healing-pool` are also valid keys. The last is a debug
helper (normal budget zero); kill-created pools use global pool settings.

```yaml
activation: EVERY
worlds: [world_the_end]
attacks:
  island-wave:
    enabled: true
    cooldownSeconds: 55.0
    maxUses: 4
    minHealthFraction: 0.0
    maxHealthFraction: 1.0
    warningSeconds: 3.0
    durationSeconds: 8.0
    damage: 5.0
    radius: 4.0
    baseCount: 2
    extraCountPerPlayer: 0.5
    maxCount: 12
```

| Field | Meaning |
|---|---|
| `enabled` | Normal scheduler switch; debug bypasses it. |
| `cooldownSeconds` | Minimum time between normal launches of this attack. |
| `maxUses` | Encounter budget; `0` disables normal launches, `-1` is unlimited. |
| `sustainAfterBudget` | Continue on cooldown after budget exhaustion. False except basic waves/effect areas/explosives. Does not override enabled, zero budget, health gates or cooldowns. |
| `minHealthFraction`, `maxHealthFraction` | Inclusive HP window for normal launches (0–1). Crystals additionally require strictly below half HP. |
| `warningSeconds` | Delay before hostile effect activation. Support zones are positive and activate immediately. |
| `durationSeconds` | Active effect-area, infestation, magnetism, sanctuary, or updraft lifetime. Instant casts ignore this. Wave lifetime is determined by radius/speed; monsters use global summon lifetime. |
| `damage` | Base custom damage for waves, explosions/strikes, infestation pulses, and optional area pulses. Monster melee and magnet landing have separate settings. |
| `radius` | Effect/strike area, local dragon wave, sanctuary, or updraft radius. Island wave always uses `islandRadius`; infestation uses distributed patterns. |
| `baseCount`, `extraCountPerPlayer`, `maxCount` | Scaled target/zone/entity count for crystals, areas, explosives, monsters, blue Endermen, and support zones. Global population caps also apply. Magnetism always affects all fighters; single-wave and infestation casts also ignore these. Player-targeted areas never choose random spare locations if there are fewer fighters than the count. |

Most attacks use defaults of a three-second warning, eight-second active
duration, four-block radius, two base targets, +0.5 targets per extra fighter,
and 12 targets maximum. Overrides are in the attack table and
[`defaultAttacks()`](../remade-ender-dragon/src/main/kotlin/yv/tils/remadeEnderDragon/configs/DragonConfig.kt).
Use `/redragon status` to see consumed budgets. Invalid IDs, negative damage,
unsafe dimensions/counts, and unknown effect IDs are rejected.

## Admin commands and preview

Permission: `yvtils.remade-ender-dragon.admin`, default **OP**.
Alias: `/remadeenderdragon`.

Permissions follow the shared `PermissionsData`/enum registration pattern:

| Permission | Default | Grants |
|---|---|---|
| `yvtils.remade-ender-dragon.admin` | OP | Command management, previews, and the config permission as a child. |
| `yvtils.remade-ender-dragon.config` | OP | Module configuration GUI access and saves, including via `/yvtils config` (the launcher also applies its own config permission). |
| `yvtils.remade-ender-dragon.*` | OP | All registered module permissions. |

`/redragon` management commands require admin permission. Config-only users
can reach the editor through the collection's configuration menu.

| Command | Purpose |
|---|---|
| `/redragon` | Command help. |
| `/redragon start` | Start/resume a custom encounter around the living dragon in your configured End world. |
| `/redragon stop` | Clean up hazards, summons, crystals, teams, and vegetation; restore original dragon max HP proportionally. Suppress automatic restart for this dragon. |
| `/redragon status` | Fighter count, dragon HP, active effects/summons, consumed budgets. |
| `/redragon reload` | Reload/validate config. Available from console too. |
| `/redragon config` | Open the InvUI module configuration menu. |
| `/redragon trigger <attack-id>` | Trigger one attack with tab completion, bypassing enabled/HP/cooldown/use gates. Physical population, effect, and pool caps still apply. |

Preview requires a living dragon and mapped End-stone ground in a configured
End world. If no encounter exists, trigger creates a **manual preview encounter**
without automatic attack selection or HP scaling. In `EVERY`/`FIRST`, use stop
first if you want an isolated preview. Preview attacks still damage Survival/
Adventure players; use a test world. `/redragon start` converts preview into a
normal encounter. Wait for the terrain scan before island-wide previewing.

Debug examples:

```text
/redragon stop
/redragon trigger island-wave
/redragon trigger infestation
/redragon trigger blue-endermen
/redragon trigger summon-areas
/redragon trigger healing-pool
/redragon trigger sanctuary
/redragon trigger rescue-updraft
/redragon stop
```

## Lifecycle and implementation notes

- One main-thread ticker advances encounters every two ticks. No asynchronous
  Bukkit entity/block operations. Island mapping has a bounded per-tick scan.
- Blue glow uses isolated per-encounter scoreboard teams on the viewers'
  existing scoreboards, with entry/team cleanup. Teleports/block carrying are
  disabled on custom Endermen so the frontal warning stays meaningful.
- Summons/focuses drop no loot/XP. All owned entities carry persistent tags;
  stale objects are removed when chunks load after a restart.
- A persistent original-HP tag prevents crashes from making the dragon's
  scaled HP permanent. Closing restores HP percentage against original max HP.
- Added grass is journaled before placement. Expiry, chunk unload, stop,
  dragon death, world unload, and disable remove owned markers. Restart
  recovery handles loaded chunks immediately and other chunks when loaded.
  The journal includes each marker's material and recovers legacy short-grass
  records as well as new dry grass. Only still-matching grass is removed; unrelated replacement blocks
  are preserved. Do not delete `terrain-journal.json` while markers exist.
- Server restarts reset attack cooldown/use budgets. Completion history and
  terrain recovery persist; live encounter timers do not.
- Announcements/command messages are registered with config-v2 language files.
  English and German messages are provided; attack labels are editable per locale.
- Shared `Logger` records module lifecycle, encounter starts/stops, configuration
  edits/reload failures, debug-preview requests and terrain recovery. Per-attack
  and marker-placement diagnostics use `DEBUG_LEVEL.DETAILED` and follow the
  collection's central debug setting rather than a separate module log toggle.

## Verification and playtest checklist

Configuration schema 2 upgrades values that still exactly match the earlier
beta defaults: island/effect/local-wave/infestation damage, local-wave warning
and cooldown, base monster count, and the infestation block cap. Other custom
values are retained. An intentional custom value equal to an old default is
indistinguishable from an untouched value and receives this one-time upgrade;
it can be restored afterward in YAML or InvUI. `schemaVersion` is internal and
hidden from the GUI. New announcement, display, carrier, and patch options
are automatically generated on reload. Existing configs need no deletion.

Automated checks:

```bash
./gradlew :remade-ender-dragon:build :core:compileJava
```

Pure-rule tests cover party/count caps, nonfatal-but-damaging magnet landings,
±45° cone boundaries, breath angle wrapping, forward-only stages, disabled and
sustained budgets, attack compatibility and follow-up selection. Compilation validates the Paper and CommandAPI calls.
The module uses the repository's default Paper target (26.1.2).

In-game tuning still requires a Paper server and players:

1. Compare one, two, and larger fighter groups; move a spectator/Creative player
   and an outer-island explorer in/out and inspect HP without healing percentage.
2. Trigger each attack; jump over both rings and sidestep special Enderman cones.
3. Preview infestation after mapping the island; verify distributed patterns,
   no removed End stone, expiration, and restart/unload recovery.
4. Preview magnetism unarmored; verify modest landing damage, water/web catches,
   low-health nonfatal landings, and avoidance of void columns.
5. Kill blue Endermen, inspect blue glow, pool health/food/armor/offhand repairs
   and cleansing; kill ordinary custom monsters to verify nearby rally buffs.
6. Destroy a focus before its timer ends, then allow another cast to complete.
7. Test crystals below half HP and crystal-break buffs; hit both the glowing
   crystal and silverfish separately to verify one-hit pair destruction,
   ground following, and renewal budget.
   Renewal pairs ignore dragon, suffocation, fire, and explosion-chain damage;
   only accepted fighter melee/projectile hits destroy them. Neither half
   produces a vanilla explosion when hit.
8. Verify sanctuary mitigation and per-player updraft use limits.
9. Stop/reload during overlapping attacks; inspect entities, grass and HP.
10. Test `ADMIN`, `EVERY`, and `FIRST`, including restart and dragon respawn.
11. Cross 70%/35%, heal the dragon and change the party: stages must not reverse.
    Verify quiet transitions and recovery windows, including Rift Enderman waves.
12. Preview `marked-hunters`: bait and escape; try death/world departure, island
    edges, sanctuary/anchor/convergence proximity. No delayed lock or stale marker.
13. Preview `rift-anchors`, then `effect-areas`; compare cloud duration. Kill an
    anchor and inspect effective dragon damage for ten seconds; let another expire.
14. Preview `breath-sweep` near fighters; compare displays with hit geometry on
    platforms and across ±180° facing. Preview both wave rings and jump twice.
15. Exhaust basics above 70% HP: offense should continue on cooldown. Inspect
    compatibility exclusions and healing-pool independence during automatic casts.
16. Let carriers arrive, destroy others, and let one expire. Check fixed beams,
    capped empowerment expiry and cleanup after stop/reload/restart.
17. Play with 5/10/15 mixed-gear fighters. Record duration/deaths and damage
    windows. Verify bed/melee/projectile damage and crystal healing against
    effective HP. Tune HP before damage to approach the 30-minute goal.

## Longer encounter design (schema 3)

The goal is roughly 30 minutes for 5–15 mixed diamond/netherite fighters, with
first-try completion and few deaths. Five fighters have 4,800 effective HP;
fifteen have 13,800. Duration needs multiplayer tuning: there is no hard enrage,
forced wipe or timed invulnerability to enforce it.

Stages advance at 70%/35% and never reverse. Opening introduces basic waves,
projectiles, explosives and monsters. Middle unlocks advanced attacks. Finale
adds an echo to every second dragon/island wave, two seconds behind the first
with a separate warning and 65% damage. Debug wave previews include the echo.
Stage transitions pause new offense for eight seconds; completed hostile effects
give four seconds of recovery. Existing effects finish normally.

The director favors least-recently-used attacks and avoids repeats. After effect
areas expire and recovery ends, it favors an island wave. Waves, forced movement,
infestation and breath sweeps cannot overlap other hostile effects. Anchors may
coexist with offense, but magnetism waits until anchors/crystal carriers are gone.
Rift Enderman waves honor the same movement exclusions; summons still melee.
Local waves require nearby fighters; breath sweeps require loaded ground and a
nearby target. Healing pools/support zones do not consume offensive slots.
Scripted hits have a 1.25-second per-fighter gap to limit burst damage; vanilla
combat is unaffected. Essential warnings remain; subtle cues add no names or
instructions. Instructional entity names are hidden unless announcements are enabled.

Marks cancel on death/departure, invalid ground, near the edge or near sanctuary,
anchors/convergence. Locked positions never chase players. Anchor empowerment
and vulnerability do not stack per anchor; cloud duration is captured at launch.
Crystal convergence grants capped temporary empowerment rather than repeated
healing. Disable convergence for projection-following carriers, still with expiry.

Schema 3 upgrades numeric values still equal to old defaults for HP, piercing,
pacing and local-wave warning. It adds new attacks, enables sustained basics and
upgrades untouched four-use support budgets to unlimited. Enabled flags and zero
budgets remain intact. Intentional values equal to old defaults are upgraded too;
restore them afterward if desired. Other custom numbers remain. Set
`sustainAfterBudget: false` after migration for finite basics. Live stages/marks/
empowerment/vulnerability reset on stop/reload/restart; owned objects use existing
cleanup and recovery mechanisms.

The shipped values are conservative starting values, not a claim of completed
multiplayer balance testing. Tune cooldowns/budgets/coverage before damage:
readable space and recovery opportunities are the primary balance levers.
