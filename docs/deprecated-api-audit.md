# Deprecated API usage audit

Audited: **2026-10-19**, against the current working tree.

## Scope and evidence

This inventory covers deprecated APIs referenced by project production/test sources,
including Kotlin property syntax, deprecated classes/constants, the explicitly
suppressed damage-modifier API, and deprecations reported while configuring Gradle.
Locations and ratings describe the working tree at the audit date, not a published release.

Verification used JDK 25, Kotlin 2.4.10, Gradle 9.6.1, Paper API
26.1.2.build.74-stable / 26.2.build.134-stable / 26.3.build.205-stable, and
WorldGuard 7.0.18. Both commands completed successfully:

```sh
./gradlew compileKotlin compileJava compileTestKotlin compileTestJava --rerun-tasks --no-daemon --console=plain
./gradlew help --warning-mode all --stacktrace --no-daemon --console=plain
```

Compiler diagnostics were supplemented by a source search for deprecation
annotations/suppressions and inspection of dependency source Javadocs. Repeated
warnings from the three GUI compatibility builds count as the same source usage.
The compilation command compiles tests; it does not execute them.

This is a compiler/source-backed snapshot, not a guarantee about APIs invoked
dynamically through reflection or all transitive dependency internals. Ordinary
warnings about unchecked casts, redundant expressions, Kotlin boxed types,
reflection classpaths, or Shadow metadata merging are not deprecations.

## Ratings

**Ease** measures implementation and verification effort, with higher being easier:

| Score         | Meaning                                                                             |
|---------------|-------------------------------------------------------------------------------------|
| 5 — Trivial   | Local substitution/removal, little behavioral change.                               |
| 4 — Easy      | Small refactor with focused behavior checks.                                        |
| 3 — Moderate  | Multiple paths or compatibility/semantic decisions.                                 |
| 2 — Difficult | Substantial redesign or version-specific integration.                               |
| 1 — Blocked   | No verified equivalent public replacement; upstream work or a new design is needed. |

**Importance** measures migration priority, with higher being more important:

| Score        | Meaning                                                                           |
|--------------|-----------------------------------------------------------------------------------|
| 5 — Critical | Confirmed current failure or an immediate upgrade blocker.                        |
| 4 — High     | Documented behavioral problems, unsupported behavior, or explicit future removal. |
| 3 — Medium   | Runtime maintenance risk without a confirmed current failure.                     |
| 2 — Low      | Limited cosmetic/test impact or a long-standing compatibility dependency.         |
| 1 — Minimal  | Inactive usage with negligible runtime effect.                                    |

No item is rated critical: the audited sources still compile. High importance
does not imply that the current implementation has been proven broken.

## Migration overview

| ID  | Module / area                  | Deprecated API                            | Replacement possible?                                                                            | Ease          | Importance |
|-----|--------------------------------|-------------------------------------------|--------------------------------------------------------------------------------------------------|---------------|------------|
| D01 | Shared GUI                     | `ItemFlag.HIDE_ADDITIONAL_TOOLTIP`        | Yes: targeted `TooltipDisplay` component hiding, or remove unnecessary flag                      | 4 — Easy      | 3 — Medium |
| D02 | Essentials offline inventories | `Bukkit.getUnsafe()` / `UnsafeValues`     | No verified drop-in public data-version accessor in the audited API                              | 1 — Blocked   | 3 — Medium |
| D03 | Fusion frames                  | `GameRule.DO_ENTITY_DROPS`                | Yes: `org.bukkit.GameRules.ENTITY_DROPS`                                                         | 5 — Trivial   | 4 — High   |
| D04 | Fusion Nourishing Flask        | `ItemStack.setType(Material)`             | Yes: return `base.withType(Material.POTION)`                                                     | 4 — Easy      | 4 — High   |
| D05 | Fusion Nourishing Flask        | `PlayerInteractEvent.isCancelled()`       | Yes: explicit `useInteractedBlock()` / `useItemInHand()` results                                 | 3 — Moderate  | 4 — High   |
| D06 | Server login listener          | `PlayerLoginEvent`                        | Yes: remove empty listener; use connection-validation/full-server events for future logic        | 5 — Trivial   | 4 — High   |
| D07 | Remade Ender Dragon            | `EntityDamageEvent.DamageModifier`        | No drop-in equivalent for current partial-piercing semantics; redesign possible                  | 2 — Difficult | 3 — Medium |
| D08 | Regions tests                  | `Flags.GREET_MESSAGE`                     | Yes for test intent: use a non-deprecated test `StringFlag`; no direct new greeting constant yet | 4 — Easy      | 2 — Low    |
| D09 | Root Gradle tasks              | `val name by tasks.registering { ... }`   | Yes: `tasks.register("name") { ... }`                                                            | 5 — Trivial   | 4 — High   |
| D10 | Paperweight plugin internals   | Typed `registering(Type::class)` delegate | Upstream plugin update required                                                                  | 2 — Difficult | 4 — High   |

## Detailed inventory

### D01 — Shared GUI tooltip flags

**Locations:**

- [`Heads.kt:155`](../gui-26.1/src/main/kotlin/yv/tils/gui/utils/Heads.kt#L155)
- [`Heads.kt:172`](../gui-26.1/src/main/kotlin/yv/tils/gui/utils/Heads.kt#L172)

Both custom-head factory overloads call
`meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)`. These sources are compiled
by `gui-26.1`, `gui-26.2`, and `gui-26.3`.

**Reason:** Paper's Javadoc says the old flag no longer exists and does not work
properly with individually hidden data components.

**Migration:** Determine which extra lines a custom head actually needs hidden.
Use `DataComponentTypes.TOOLTIP_DISPLAY` with
`TooltipDisplay.tooltipDisplay().addHiddenComponents(...).build()` for those
components, preserving existing tooltip settings. If no extra lines exist, remove
the obsolete flag. Do not replace it with `hideTooltip(true)` indiscriminately:
that hides the custom name/lore too. Apply stack data changes after assigning
`ItemMeta` so a stale meta snapshot cannot overwrite them.

**Verification:** Build all three GUI artifacts and inspect custom-head names,
lore and unwanted tooltip lines on their supported Minecraft versions. The
component API is experimental in the audited 26.1 API, so verify compatibility
across the shared-source builds.

### D02 — Offline inventory data-version check

**Location:** [
`OfflineInventoryStorage.kt:62`](../essentials/src/main/kotlin/yv/tils/essentials/inventory/OfflineInventoryStorage.kt#L62).

The adapter compares player NBT `DataVersion` with
`Bukkit.getUnsafe().dataVersion`. The warning is on `getUnsafe()`; the enclosing
`UnsafeValues` interface is also deprecated. `getDataVersion()` itself is not
individually annotated in the audited API.

**Reason:** `UnsafeValues` explicitly offers no forward-compatibility guarantees.

**Replacement assessment:** No equivalent non-deprecated public accessor was
verified in the audited Paper API. `ServerBuildInfo.minecraftVersionId()` returns
a version string, not the integer NBT data version, and is not a replacement.
An internal server-version accessor could be reached through this adapter's
existing reflection boundary, but that trades a deprecated public API for an
unstable internal dependency; it is not automatically an improvement.

**Recommendation:** Keep the strict same-data-version guard until an equivalent
accessor or a supported offline-inventory API is available. Do not remove the
check merely to silence the warning, and do not guess data versions from release
names. If choosing an internal adapter, verify its mapping for every supported
server version and retain the classloader isolation boundary.

**Verification:** Older/current/newer player-data versions, rejected writes,
online-target exclusion, inventory/equipment round trips, and file-conflict handling.

### D03 — Invisible-frame entity-drop game rule

**Location:** [`FrameListener.kt:43`](../fusion/src/main/kotlin/yv/tils/fusion/listeners/FrameListener.kt#L43).

`world.getGameRuleValue(GameRule.DO_ENTITY_DROPS)` uses a legacy constant marked
`forRemoval = true` since 1.21.11.

**Migration:** Replace the import/reference with `org.bukkit.GameRules.ENTITY_DROPS`.
The old constant already aliases that replacement in the audited API, making this
a behavior-preserving substitution.

**Verification:** Invisible-frame and displayed-item drops with the rule enabled
and disabled, including Creative mode.

### D04 — Changing the Nourishing Flask stack type in place

**Location:** [
`NourishingFlask.kt:56`](../fusion/src/main/kotlin/yv/tils/fusion/logic/items/flask/NourishingFlask.kt#L56).

`type = Material.POTION` invokes deprecated `ItemStack.setType(Material)`.

**Reason:** Paper no longer supports changing a stack's material in place and
documents possible validation bypasses and undefined behavior for event stacks.
This particular stack is a crafting output, not an event-owned stack, but the API
is still deprecated.

**Migration:** Change the output expression from `base.apply { type = ... }` to
`base.withType(Material.POTION).apply { ... }`. `withType` returns a new stack and
preserves count and compatible metadata. Continue editing the returned potion's
`PotionMeta`, writing flask settings, and applying output lore.

**Verification:** Legacy glass-bottle recipes and potion recipes must both produce
the colored, non-stackable potion with the expected name, lore, persistent identity
and flask settings. Check custom/exact-output metadata preservation.

### D05 — Ambiguous interaction cancellation

**Location:** [
`NourishingFlask.kt:85`](../fusion/src/main/kotlin/yv/tils/fusion/logic/items/flask/NourishingFlask.kt#L85).

The right-click-block guard reads `event.isCancelled`, invoking deprecated
`PlayerInteractEvent.isCancelled()`.

**Reason:** The event has separate block-use and item-use results. Its deprecated
getter only reports whether `useInteractedBlock() == Event.Result.DENY`; it does
not describe item-use denial.

**Migration:** Use explicit result checks. A strictly equivalent replacement for
the existing guard is `event.useInteractedBlock() == Event.Result.DENY` when the
action is `RIGHT_CLICK_BLOCK`. Also decide how an explicit item-use denial should
affect the custom flask action; preserving protection-plugin intent may require
checking `useItemInHand()` independently. Keep right-click-air behavior intentional:
vanilla can pre-deny interactions that have no vanilla action.

The assignment `event.isCancelled = true` on the following line calls
`setCancelled(true)`, which is **not** deprecated in the audited API. It can be
expressed as explicit result setters for clarity, but it is not a separate
deprecated usage. Other event classes' `isCancelled` properties are not deprecated
merely because this getter is.

**Verification:** Right-click air/block, protected blocks, separate block/item
denials, both hands, duplicate interactions, cooldowns, and XP/food transactions.

### D06 — Empty legacy login listener

**Locations:**

- [`PlayerLogin.kt:17,21`](../server/src/main/kotlin/yv/tils/server/listeners/PlayerLogin.kt#L17): deprecated
  import/event parameter.
- [`ServerYVtils.kt:77`](../server/src/main/kotlin/yv/tils/server/ServerYVtils.kt#L77): listener registration that
  activates it.

The handler is empty, but registering it still subscribes to `PlayerLoginEvent`.

**Reason:** Paper deprecated the event since 1.21.6. Its Javadoc says listening
creates the player entity too early and skips one of Minecraft's validation checks.

**Migration:** Remove the empty listener and its registration. If login logic is
added later, use `PlayerConnectionValidateLoginEvent` for authentication/ban
validation, `PlayerServerFullCheckEvent` for capacity bypass, or `PlayerJoinEvent`
for work requiring an initialized player entity. These events are selected by
purpose, not interchangeable blanket replacements.

**Verification:** Normal joins plus banned, unwhitelisted and full-server
connections. The existing moderation `listeners/BannedLogin.kt` demonstrates the
connection-validation API.

### D07 — Suppressed damage modifiers in the dragon encounter

**Location:** [
`FightManager.kt:206–244`](../remade-ender-dragon/src/main/kotlin/yv/tils/remadeEnderDragon/logic/FightManager.kt#L206),
`pierceProtection()`, covered by `@Suppress("DEPRECATION")`.

All explicit references to the deprecated `EntityDamageEvent.DamageModifier` enum:

- Line 212: enum class token, used to enumerate modifiers.
- Lines 213–216: `ARMOR`, `MAGIC`, `RESISTANCE`, `ABSORPTION`.
- Lines 220, 230, 236: `HARD_HAT` and `BLOCKING`.

The method uses these values in `isApplicable(modifier)`,
`getDamage(modifier)` and `setDamage(modifier, amount)` calls throughout lines
218–243. Those overloads depend on the deprecated enum; in the audited source
they are not independently annotated deprecated. Count this as one grouped
deprecated-API dependency rather than unrelated warnings for each expression.

**Reason:** Bukkit marks the enum deprecated since 1.12 because the modifier model
is unsustainable to maintain. The suppression makes it invisible in normal compiler
warning output, so it needs explicit tracking.

**Replacement assessment:** There is no verified one-call equivalent for the
current behavior: partially bypass armor/enchantment reductions, rescale resistance,
and account for absorption while retaining the original event, cancellation and
armor-wear pipeline. Simply setting health, adding a second damage event, or
changing raw `event.damage` does not preserve these semantics.

**Migration direction:** Investigate custom registered damage types/`DamageSource`
and damage-type tags, or redesign the piercing mechanic around supported damage
APIs. Bypass tags alone do not express arbitrary fractional piercing and can change
armor wear, invulnerability, attribution and protection-plugin interactions. Keep
the localized suppression documented until an equivalent design is verified.

**Verification:** Armor, enchantments, resistance, absorption, shields, cancellation,
armor durability, attribution, invulnerability frames, and interactions with other
damage listeners. Pure `FightMath` tests alone cannot verify the event pipeline.

### D08 — WorldGuard greeting flag in policy tests

**Locations:** [
`ClaimPoliciesTest.kt:95–100`](../regions/src/test/kotlin/yv/tils/regions/logic/ClaimPoliciesTest.kt#L95):
six references to `Flags.GREET_MESSAGE` (three writes and three queries).

**Reason:** WorldGuard 7.0.18 warns that the flag will change from `StringFlag` to
`ComponentFlag` in a future release. There is no replacement greeting constant
already providing that migration in the audited version.

**Migration:** This test verifies independent role groups for text values, not
greeting delivery. Use a test-local non-deprecated `StringFlag` with an appropriate
unique name if the test helper/policy code accepts arbitrary flags. That preserves
the intent without coupling it to greeting's future value type. If retaining a
real built-in integration case, handle the future component type explicitly when
upgrading WorldGuard instead of hiding the warning with an unchecked cast.

**Verification:** Run `:regions:test`, retaining owner/member/visitor string values
and the adjacent global numeric-flag assertions.

### D09 — Local Gradle task registration delegates

**Locations:**

- [`build.gradle.kts:479`](../build.gradle.kts#L479): `generateLocalMavenChecksums`.
- [`build.gradle.kts:513`](../build.gradle.kts#L513): `publishAllModulesLocally`.

**Reason:** Gradle 9.6.1 reports that delegated `registering` syntax is scheduled
for removal in Gradle 10.

**Migration:** Keep task names, bodies and laziness while changing declarations:

```kotlin
val generateLocalMavenChecksums = tasks.register("generateLocalMavenChecksums") {
    // Existing configuration.
}
val publishAllModulesLocally = tasks.register("publishAllModulesLocally") {
    // Existing configuration.
    finalizedBy(generateLocalMavenChecksums)
}
```

**Verification:** Run `help --warning-mode all`, and inspect
`publishAllModulesLocally --dry-run` for publishing/checksum wiring. D10 may still
produce a warning after these local fixes.

### D10 — Paperweight's internal typed registration delegate

**Location:** Root [`build.gradle.kts:209–214`](../build.gradle.kts#L209)
applies `io.papermc.paperweight.userdev`; the deprecated call is inside the plugin,
not at a local `registering` declaration.

**Evidence:** `help --warning-mode all --stacktrace` attributes the typed delegate
warning to `io.papermc.paperweight.userdev.PaperweightUser.apply(PaperweightUser.kt:395)`
via `NamedDomainObjectContainerExtensionsKt.registering(...:142)`.

**Migration:** Upgrade to a Paperweight version that uses explicit `register`
instead of the typed delegated syntax, once a compatible fixed release is verified.
No specific fixed release was established in this audit. Editing the local task
declarations in D09 does not fix this warning. Patching cached plugin JARs is not
a maintainable replacement.

**Verification:** Repeat `help --warning-mode all --stacktrace` and resolve/build
Paper dev bundles for all three supported GUI versions before adopting Gradle 10.

## Suggested migration order

1. **D06:** Remove the empty legacy login subscription: trivial and behaviorally relevant.
2. **D03 + D09:** Apply direct substitutions, including the Gradle 10 preparation.
3. **D04 + D05:** Migrate flask stack construction and explicitly define interaction denial semantics.
4. **D01:** Replace obsolete tooltip hiding across the three supported GUI builds.
5. **D08:** Decouple policy tests from WorldGuard's future greeting type change.
6. **D10:** Track an upstream Paperweight fix alongside build-tool upgrades.
7. **D02 + D07:** Keep compatibility dependencies visible; research replacements
   without weakening data-version validation or changing combat behavior accidentally.

## Already migrated: player profiles

The earlier player-head work removed direct usages of
`Bukkit.createPlayerProfile`, `SkullMeta.ownerProfile` and
`org.bukkit.profile.PlayerProfile` from project sources. Inventory head loading
now uses shared `yv.tils.gui.utils.PlayerHeads`; Fusion's public profile parameters
use `com.destroystokyo.paper.profile.PlayerProfile`. The conversion fallback and
its deprecation suppression have also been removed. These are completed migrations,
not outstanding items in the overview.

## Keeping the audit current

- Re-run the commands above after dependency upgrades; incremental/up-to-date
  compilation alone can conceal warnings from unchanged files.
- Inspect deprecation suppressions explicitly and review new reflection adapters
  against their target APIs; compiler silence is not evidence of compatibility.
- Update locations, replacement availability and ratings when resolving an item.
- Preserve relevant behavior with focused tests and supported-version runtime checks.
- Follow the repository's module-version policy when implementing migrations.

References: audited dependency source Javadocs for `ItemFlag`, `TooltipDisplay`,
`GameRule`, `ItemStack`, `PlayerInteractEvent`, `PlayerLoginEvent`, `UnsafeValues`,
`EntityDamageEvent.DamageModifier` and WorldGuard `Flags`; and the
[Gradle 9 upgrade guide](https://docs.gradle.org/9.6.1/userguide/upgrading_version_9.html#kotlin_dsl_delegated_properties).
