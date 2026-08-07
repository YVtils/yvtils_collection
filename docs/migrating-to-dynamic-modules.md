# Migrating modules & cores to the dynamic module system

This guide explains how to migrate an existing **feature module** (`discord`,
`regions`, `sit`, ...) or **core/launcher** (`core`, `discord-core`,
`regions-core`, `multiMine-core`, ...) from the old "shade everything into one
fat jar" pattern to the new dynamic module system, where a launcher fetches
only the feature modules it needs at runtime from our self-hosted Maven
registry (Reposilite), instead of bundling every module directly.

`test-core` is the reference implementation of the new pattern. When in doubt,
read its source - `test-core/build.gradle.kts`,
`test-core/src/main/kotlin/yv/tils/core/YVtils.kt`, and everything under
`test-core/src/main/kotlin/yv/tils/core/loader/`.

## Table of contents

1. [The one rule you must understand first](#the-one-rule-you-must-understand-first)
2. [Current migration status](#current-migration-status)
3. [Part 1 - Migrating a feature module](#part-1---migrating-a-feature-module)
4. [Part 2 - Migrating a core/launcher](#part-2---migrating-a-corelauncher)
5. [Testing your migration locally](#testing-your-migration-locally)
6. [Testing a not-yet-published module](#testing-a-not-yet-published-module-eg-artifactnotfoundexception)
7. [Troubleshooting](#troubleshooting)
8. [Checklists](#checklists)

---

## The one rule you must understand first

Paper's `PluginLoader`/`PluginClasspathBuilder` mechanism builds a "library
tier" classloader for anything you add via `addLibrary(...)` (whether that's
a `MavenLibraryResolver` fetch or a local `JarLibrary`). This tier can be seen
**from** the plugin's own main jar, but code running **inside** the library
tier cannot see back into classes bundled directly in the main jar via a
plain Gradle `implementation` dependency.

Concretely: if `utils`/`config`/`common` are shaded directly into a launcher's
own jar (`implementation`), and a feature module is fetched dynamically at
runtime, that feature module will crash with
`NoClassDefFoundError: yv/tils/utils/modules/Module$YVtilsModule` the moment
it tries to use anything from `utils`. Same story for CommandAPI: if it's
shaded into the main jar **and** transitively pulled in by a fetched module's
own POM, you get two independent, separately-initialized copies, and the
fetched module's copy will blow up with
`IllegalStateException: Tried to access InternalConfig, but it was null!`
because only the main jar's copy ever got `CommandAPI.onLoad(...)` called on
it.

**The fix, and the entire point of this migration:** anything that a
dynamically-fetched module needs to interoperate with - `utils`, `config`,
`common`, CommandAPI, coroutines, serialization - must be added to the
classpath through the *same* `PluginClasspathBuilder` mechanism as the fetched
modules (either via `MavenLibraryResolver` for remote fetches, or via a local
`JarLibrary` for things that are always needed and don't need to come over the
network). Never via `implementation`, once a module is expected to
interoperate dynamically.

If you only remember one thing from this document, remember that.

## Current migration status

| Component | Status |
|---|---|
| `utils`, `config`, `common` | Not published individually. Bundled together via `common`'s own `shadowJar` output and embedded as a `JarLibrary` resource by migrated cores. No change needed to these modules themselves. |
| `discord`, `regions`, `multiMine`, `essentials`, `sit`, `status`, `server`, `message`, `moderation`, `gui-26.1`, `gui-26.2`, `migration`, `stats` | **Gradle-level migration done.** All are in `publishableModules` in the root `build.gradle.kts`, have `compileOnly` on `utils`/`config`/`common`, and get CommandAPI/coroutines/serialization as `compileOnly` automatically. `sit` has been round-tripped through a real publish+fetch+enable test. `gui-<version>` modules are a special case, resolved unconditionally by `core` based on the running server's Minecraft version rather than through `modules.yml` - see `DynamicModuleRegistry.GUI_ARTIFACTS`. The others have not been individually verified yet - do that before relying on them. |
| `test-core` | **Fully migrated.** Reference implementation - has the `PluginLoader`, the embedded runtime bundle, and dynamic module discovery wired up end-to-end. |
| `core`, `discord-core`, `regions-core`, `multiMine-core` | **Not migrated yet.** Still shade every module directly (old pattern). This is the main remaining work - see [Part 2](#part-2---migrating-a-corelauncher). |

## Part 1 - Migrating a feature module

All 12 current feature modules already have this done. This section is for
when you add a **new** feature module, or need to double-check an existing
one. If you're creating a module from scratch (not just wiring up an already-
written one), see [`adding-a-new-module.md`](./adding-a-new-module.md) for
the full anatomy (commands, config, translations, permissions) - this section
only covers the dynamic-loading wiring itself.

### 1. Dependencies: `compileOnly`, not `implementation`

In the module's `build.gradle.kts`, any dependency on `utils`, `config`, or
`common` must be `compileOnly`:

```kotlin
dependencies {
    compileOnly(project(":utils"))
    compileOnly(project(":config"))
    compileOnly(project(":common"))
}
```

If your module depends on **another feature module** that is itself
dynamically fetched, keep that as a real `implementation` dependency - it
needs to show up in the published POM as a proper transitive Maven dependency
so it gets fetched automatically:

```kotlin
dependencies {
    compileOnly(project(":utils"))
    compileOnly(project(":config"))
    compileOnly(project(":common"))
    implementation(project(":some-other-published-module")) // keep this real
}
```

**`gui` is a deliberate exception to the above** - every feature module that
uses GUI types declares it as `compileOnly(project(":gui-26.1"))` instead
(never `implementation`), and it must NOT show up in your module's published
POM at all. `core` resolves exactly one `gui-<version>` artifact itself,
unconditionally, matching the running server's actual Minecraft version (see
`DynamicModuleRegistry.GUI_ARTIFACTS` and `DynamicModuleLoader.addGuiModule`
in `core`) - InvUI (which `gui` wraps) dropped multi-version support in v2, so
each Minecraft version needs a matching `gui-<version>` build, and it keeps
classloader-sensitive global static state that must only ever be resolved
once per server. If every module that uses GUI types also declared it as a
real transitive dependency, it would get pulled in and re-resolved once per
module, each with its own separate copy of that state.

Third-party runtime dependencies (e.g. `discord`'s JDA dependency, `gui`'s
InvUI dependency) also stay as normal `implementation`/`api` on the module
that actually needs them directly (`gui-26.1`/`gui-26.2` themselves, for
InvUI) - you do **not** need to make Reposilite aware of them or proxy their
host. `DynamicModuleLoader` adds a small, fixed list of well-known upstream
repositories (Maven Central, PaperMC, xenondevs - see
`THIRD_PARTY_REPOSITORIES` in `DynamicModuleLoader.kt`) as fallbacks on every
module's resolver, so
transitive third-party dependencies resolve directly from their real host
instead of requiring Reposilite to mirror it. If your module pulls in a
dependency from some other host not already in that list, add it there.

You do **not** need to touch CommandAPI/coroutines/serialization dependencies
yourself - the root `build.gradle.kts` already adds those as `compileOnly`
automatically for anything in `publishableModules`/`dynamicCoreModules` (see
below).

### 2. Register the module in the root `build.gradle.kts`

Add your module's Gradle project name to the `publishableModules` set near
the top of the root `build.gradle.kts`:

```kotlin
val publishableModules = setOf(
    "discord",
    "regions",
    // ...
    "your-new-module",
)
```

This automatically:
- Applies `maven-publish` and configures it to publish to the Reposilite
  `releases` repository under groupId `yv.yvtils`.
- Switches CommandAPI/coroutines/serialization to `compileOnly` for this
  module.

### 3. Have exactly one lifecycle entry-point class

Your module needs one class implementing
`yv.tils.utils.modules.Module.YVtilsModule` that a launcher can instantiate
reflectively (see [`DynamicModuleDriver`](../test-core/src/main/kotlin/yv/tils/core/loader/DynamicModuleDriver.kt)).
This already exists for every module (`DiscordYVtils`, `SitYVtils`,
`EssentialYVtils`, `GUIYVtils`, etc.) - just make sure:

- It has a public, no-arg constructor (it's instantiated via
  `Class.forName(...).getDeclaredConstructor().newInstance()`).
- Its fully-qualified name is registered in
  [`DynamicModuleRegistry.KNOWN_MODULES`](../test-core/src/main/kotlin/yv/tils/core/loader/DynamicModuleRegistry.kt)
  (see step 4).

### 4. Register the module + version in `DynamicModuleRegistry`

Every core using the dynamic loader shares the same
`DynamicModuleRegistry.KNOWN_MODULES` map (currently living in
`test-core/src/main/kotlin/yv/tils/core/loader/DynamicModuleRegistry.kt`, one
copy per migrated core - see Part 2 for why). Add an entry:

```kotlin
"your-new-module" to ModuleArtifact("your-new-module", "26.08.01", "yv.tils.yourmodule.YourModuleYVtils"),
```

Versioning is independent per module - bump the `version` string here
whenever you cut a new release of just that module, no need to touch anything
else.

### 5. Publish it

```bash
./gradlew :your-new-module:publish
```

This requires `REPOSILITE_URL`/`REPOSILITE_USERNAME`/`REPOSILITE_TOKEN` to be
set (see `.env.example`). Never commit real credentials - copy `.env.example`
to `.env`, fill it in, and `source .env` before publishing, or set them as CI
secrets.

### 6. Sanity-check the published POM

Before trusting a publish, check that the generated POM doesn't accidentally
list `utils`/`config`/`common` (or CommandAPI/coroutines/serialization) as
dependencies - if it does, something's still `implementation` that should be
`compileOnly`:

```bash
./gradlew :your-new-module:generatePomFileForMavenPublication
cat your-new-module/build/publications/maven/pom-default.xml
```

A clean module's POM should only list `kotlin-stdlib` plus any genuine
third-party runtime dependencies (JDA, etc.) and any other *published*
feature modules it depends on.

## Part 2 - Migrating a core/launcher

This is the remaining work: `core`, `discord-core`, `regions-core`, and
`multiMine-core` all still use the old pattern (hardcoded module list,
everything shaded via `implementation`). Here's how to bring one over to the
new pattern, using `test-core` as the reference.

### Step 1 - Switch shared dependencies to `compileOnly`

In the core's `build.gradle.kts`, change:

```kotlin
dependencies {
    implementation(project(":common"))
    implementation(project(":utils"))
    implementation(project(":config"))
    implementation(project(":discord"))
    implementation(project(":migration"))
    implementation(project(":stats"))
}
```

to:

```kotlin
dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":utils"))
    compileOnly(project(":config"))
    // Remove the feature module dependencies entirely - they're fetched
    // dynamically now, not shaded in at build time.
}
```

The feature modules (`discord`, `migration`, `stats`, etc.) should be
**removed** from the dependency list entirely - they're no longer compiled
against directly, they get fetched at runtime and driven reflectively (see
step 4).

### Step 2 - Add the embedded runtime bundle task

Copy this block into the core's `build.gradle.kts` (adjust names as needed):

```kotlin
val embeddedResourcesDir = layout.buildDirectory.dir("generated/embeddedResources")

val embedRuntime by tasks.registering(Copy::class) {
    dependsOn(":common:shadowJar")
    from(project(":common").tasks.named("shadowJar"))
    into(embeddedResourcesDir.map { it.dir("embedded") })
    rename { "yvtils-runtime.jar" }
}

sourceSets {
    main {
        resources.srcDir(embeddedResourcesDir)
    }
}

tasks.named("processResources") {
    dependsOn(embedRuntime)
}
```

This embeds `common`'s existing shadowJar output (which already bundles
`utils`+`config`+`common`+CommandAPI+coroutines+serialization together) as
`embedded/yvtils-runtime.jar` inside the core's own jar - see
`test-core/build.gradle.kts` for the exact, already-working version of this.

### Step 3 - Register the core in the root `build.gradle.kts`

Add the core's Gradle project name to `dynamicCoreModules`:

```kotlin
val dynamicCoreModules = setOf(
    "test-core",
    "discord-core", // add the core you're migrating
)
```

This switches its CommandAPI/coroutines/serialization dependencies to
`compileOnly` too (same reasoning as feature modules - the core must not
double-shade these).

### Step 4 - Add the `PluginLoader` + dynamic module wiring

Copy the `loader` package from `test-core` into the core you're migrating:

```
test-core/src/main/kotlin/yv/tils/core/loader/ModuleConfig.kt
test-core/src/main/kotlin/yv/tils/core/loader/DynamicModuleLoader.kt
test-core/src/main/kotlin/yv/tils/core/loader/DynamicModuleRegistry.kt
test-core/src/main/kotlin/yv/tils/core/loader/DynamicModuleDriver.kt
```

In `DynamicModuleRegistry.KNOWN_MODULES`, keep only the modules this specific
product actually ships (e.g. `discord-core` only needs `discord`, `migration`,
`stats` - it doesn't need `sit` or `regions`).

> **Note:** right now each migrated core gets its own copy of this package,
> since it's small and product-specific (different products enable different
> default modules). If this starts drifting or duplicating too much logic,
> consider factoring the generic parts (`ModuleConfig`, the resolver-building
> loop in `DynamicModuleLoader`) into a shared module - just remember that
> shared module would ALSO need to go through the embedded-`JarLibrary`
> mechanism, not a plain `implementation` dependency, for the same classloader
> reasons as everything else in this guide.

### Step 5 - Wire the loader into `paper-plugin.yml`

```yaml
name: YVtils-Discord
version: 4.0.0
main: yv.tils.core.YVtils
loader: yv.tils.core.loader.DynamicModuleLoader
api-version: '1.21'
```

### Step 6 - Update the main `YVtils.kt` class

Replace the hardcoded module list with the static "always needed" modules
only (`ConfigYVtils`, `UtilsYVtils`, `CommonYVtils`), and drive the
dynamically-discovered modules alongside them. `test-core`'s `YVtils.kt` is
the reference for this - the pattern is:

1. Keep a `private val modules: List<Module.YVtilsModule>` with just the
   always-bundled modules.
2. Add `private var dynamicModules: List<Module.YVtilsModule> = listOf()` and
   `private var dynamicModuleDiscovery: DynamicModuleDriver.DiscoveryResult? = null`.
3. In `onLoad()`, after driving the static `modules` list, call
   `DynamicModuleDriver.discover(dataFolder.toPath())`, log successes/failures,
   store the result, and drive `.onLoad()` on the discovered modules too.
4. In `onEnable()` / `onLateEnablePlugin()` / `onDisable()`, drive
   `dynamicModules` alongside `modules` at each lifecycle stage.
5. In `onLateEnablePlugin()`, apply the failure policy: if modules were
   configured but **none** resolved successfully, disable the plugin; if at
   least one resolved, log a warning about the failed ones and continue.

Copy the exact blocks from `test-core/src/main/kotlin/yv/tils/core/YVtils.kt`
- it's written to be copied, not reinvented per core.

### Step 7 - Decide what's enabled by default

`ModuleConfig.readEnabledModules(...)` writes a default `modules.yml` on
first boot if one doesn't exist yet, listing every module in
`DynamicModuleRegistry.KNOWN_MODULES` set to `true`/`false`. Update the
`defaultEnabledModules` argument passed to `readEnabledModules(...)` (and the
`knownModules` argument, if this product ships a trimmed-down registry - see
step 4) to match what this specific product should ship enabled out of the
box (e.g. `discord-core` should default to `discord`, `migration`, `stats`
set to `true`, not `sit`).

### Step 8 - Build, publish, test

Follow [Testing your migration locally](#testing-your-migration-locally)
before trusting a migrated core. Confirm:
- The plugin enables successfully with no `NoClassDefFoundError`.
- No CommandAPI `InternalConfig` error.
- `Module.getModulesString()` (logged on successful enable) lists both the
  static and dynamically-fetched modules together.
- The version check no longer false-positives (this was an existing bug -
  make sure `CheckVersion.serverVersion()` compares against
  `Core.instance.server.minecraftVersion`, not `.version`, which was already
  fixed in all copies of `CheckVersion.kt` as part of this migration).

## Testing your migration locally

You don't need real Reposilite credentials to test a migration - Aether just
needs an HTTP(S) endpoint serving a Maven layout (it does **not** support
`file://` URIs, so `mavenLocal()` alone isn't enough).

The whole "publish + checksum" dance is automated by one root-level task:

1. Publish every feature module to your local Maven cache, with checksums
   generated automatically (Reposilite, and any real Maven repo, publishes
   `.sha1`/`.md5` checksum files alongside artifacts - `publishToMavenLocal`
   alone doesn't, and Aether refuses to resolve an artifact without them):
   ```bash
   ./gradlew publishAllModulesLocally
   ```
   If you only want a single module (and whatever it transitively depends
   on), just publish that one instead and skip straight to checksum
   generation:
   ```bash
   ./gradlew :multiMine:publishToMavenLocal generateLocalMavenChecksums
   ```
2. Serve your local Maven cache over HTTP. `jwebserver` ships with the JDK
   (18+) you already need to build this project, so no extra dependency:
   ```bash
   jwebserver -p 8095 -d "$HOME/.m2/repository"
   ```
3. Point the loader at it instead of the real registry:
   ```bash
   REPOSILITE_URL="http://127.0.0.1:8095/" ./gradlew :test-core:runServer
   ```
4. Set which modules are enabled by editing
   `<core>/run/plugins/<pluginDataFolder>/modules.yml` before starting (or
   let it generate the default on first boot - it lists every known module
   with `true`/`false` - then edit + restart).

Remember a module's dependencies on *other* feature modules are real Maven
dependencies - `publishAllModulesLocally` covers this automatically since it
publishes everything, but if you're only publishing a single module by hand,
publish its dependencies too or you'll just trade one
`ArtifactNotFoundException` for another. (`gui-26.1`/`gui-26.2` are the
exception - `core` resolves those itself, unconditionally; see the note on
`gui` in [Part 1](#part-1---migrating-a-feature-module).)

If you don't want to bother with checksums at all while rapidly iterating,
set `YVTILS_DEV_SKIP_CHECKSUMS=true` alongside `REPOSILITE_URL` - this is an
explicit, opt-in escape hatch in `DynamicModuleLoader` that disables checksum
validation entirely. It only ever applies when you set it yourself; the real
registry's checksums are always validated normally.

**There are two separate local caches, not one.** Beyond your own
`~/.m2/repository` (populated by `publishAllModulesLocally`, served by
`jwebserver`), Paper's `MavenLibraryResolver` maintains its **own** resolved-
library cache under `<core>/run/libraries/` (e.g.
`test-core/run/libraries/yv/yvtils/<module>/<version>/`), completely separate
from `~/.m2`. Once a module+version has been resolved once, Paper reuses that
cached copy on subsequent server starts and only re-validates it against the
checksum it cached the *first* time - it does NOT go back to your `jwebserver`
for that exact coordinate again.

If you republish a module under the **same version** with different content
(very easy to do while iterating without bumping the version each time), you
will get a confusing `ChecksumFailureException: Checksum validation failed,
expected '<old-hash>' ... but is actually '<new-hash>'` - the mismatch is
between Paper's cached copy in `run/libraries/` and whatever your `~/.m2`/
`jwebserver` is serving now, not a corruption of either individual cache.

The reliable fix: **bump the version string whenever you republish while
iterating**, so each attempt gets its own coordinate and there's nothing to
collide with. If you deliberately want to reuse a version, delete the stale
entry from `<core>/run/libraries/yv/yvtils/<module>/<version>/` (not just
`~/.m2/repository/yv`) before restarting the server.

Remember to stop the local HTTP server and remove the local `~/.m2/repository/yv`
artifacts afterward so you don't accidentally test against stale local copies
later.

## Testing a not-yet-published module (e.g. `ArtifactNotFoundException`)

If you enable a module in `modules.yml` and see something like:

```
Caused by: org.eclipse.aether.transfer.ArtifactNotFoundException: Could not find artifact yv.yvtils:multiMine:jar:26.08.01 in yvtils-registry (https://reposilite-registry.yvtils.net/releases)
```

that module simply hasn't been published to the real registry yet (per the
[migration status table](#current-migration-status), only `sit` has been
round-tripped through a real publish). This is expected for anything that
hasn't gone through [Part 1's](#part-1---migrating-a-feature-module) publish
step for real. Follow [Testing your migration locally](#testing-your-migration-locally)
to test it against a throwaway local repository instead of the real registry.

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `NoClassDefFoundError: yv/tils/utils/modules/Module$YVtilsModule` (or any other utils/config/common class) thrown from a fetched module | `utils`/`config`/`common` are still shaded into the core via `implementation` instead of being provided through the embedded `JarLibrary` | Switch to `compileOnly` + add the `embedRuntime` task (Part 2, steps 1-2) |
| `IllegalStateException: Tried to access InternalConfig, but it was null! Are you using CommandAPI features before calling CommandAPI#onLoad?` thrown from a fetched module's command registration | CommandAPI got resolved twice - once shaded into the core, once transitively via the fetched module's own POM | Make sure the core is in `dynamicCoreModules` (root `build.gradle.kts`) and the feature module is in `publishableModules`, so both get CommandAPI as `compileOnly` |
| `NoTransporterException: Unsupported transport protocol file` | Pointed `MavenLibraryResolver` at a `file://` URI | Use an HTTP(S) endpoint - see [local testing](#testing-your-migration-locally) |
| `ChecksumFailureException: Checksum validation failed, no checksums available` | Testing against `~/.m2/repository` directly, which has no `.sha1`/`.md5` files (a real Reposilite deploy always includes them) | Run `./gradlew publishAllModulesLocally` (or `generateLocalMavenChecksums` after a manual `publishToMavenLocal`), or set `YVTILS_DEV_SKIP_CHECKSUMS=true` for quick iteration |
| `ChecksumFailureException: Checksum validation failed, expected '<hash1>' ... but is actually '<hash2>'` | A module was republished under the **same version** with different content while iterating - Paper's own `<core>/run/libraries/` cache still has the old checksum from a previous resolve and doesn't match what `~/.m2`/`jwebserver` serves now | Bump the version before republishing, or delete the stale `<core>/run/libraries/yv/yvtils/<module>/<version>/` entry (in addition to `~/.m2/repository/yv`) before restarting |
| `ArtifactNotFoundException: Could not find artifact yv.yvtils:<module>:jar:<version>` | The module hasn't been published to whichever repository you're pointed at | Against the real registry: it genuinely isn't published yet (check the [migration status table](#current-migration-status)). Locally: publish it first - see [testing a not-yet-published module](#testing-a-not-yet-published-module-eg-artifactnotfoundexception) |
| `ArtifactNotFoundException: Could not find artifact <group>:<third-party-lib>:jar:<version>` (group is NOT `yv.yvtils`) | A module's transitive third-party dependency (JDA, InvUI, ...) is hosted somewhere not in `DynamicModuleLoader`'s `THIRD_PARTY_REPOSITORIES` fallback list | Add `"<id>" to "<repo-url>"` to `THIRD_PARTY_REPOSITORIES` in `DynamicModuleLoader.kt` |
| A module's published POM lists `utils`/`config`/`common`/CommandAPI/coroutines/serialization as dependencies | Something is still `implementation` that should be `compileOnly` | Check the module's `build.gradle.kts` and confirm it's listed in `publishableModules` |
| Plugin logs "does not support the current server version" even on a version that should be supported | `CheckVersion.serverVersion()` was comparing against the full build string (`1.21.10-130-...`) instead of the clean version | Already fixed in all `CheckVersion.kt` copies - if you copied an older version, make sure it uses `Core.instance.server.minecraftVersion` |
| Static state set in `DynamicModuleLoader` (a `PluginLoader`) isn't visible from `YVtils.onLoad()` | Paper loads `PluginLoader` classes through a separate/isolated classloader from the actual plugin instance | Don't rely on shared mutable state between the two - only stateless, file-based communication (like `ModuleConfig` re-reading the same config file in both places) works reliably |

## Checklists

### Feature module migration checklist

- [ ] `utils`/`config`/`common` dependencies are `compileOnly`
- [ ] Dependencies on other *published* feature modules stay `implementation`
- [ ] Module name added to `publishableModules` in root `build.gradle.kts`
- [ ] Module has exactly one public, no-arg-constructor `Module.YVtilsModule` entry point
- [ ] Entry point + version registered in `DynamicModuleRegistry.KNOWN_MODULES`
- [ ] Published POM checked for stray `utils`/`config`/`common`/CommandAPI/coroutines/serialization dependencies
- [ ] Published (`./gradlew :module:publish`) and fetch-tested locally

### Core/launcher migration checklist

- [ ] `utils`/`config`/`common` switched to `compileOnly`; feature module dependencies removed entirely
- [ ] `embedRuntime` task added, embedding `common`'s shadowJar as `embedded/yvtils-runtime.jar`
- [ ] Core name added to `dynamicCoreModules` in root `build.gradle.kts`
- [ ] `loader` package (`ModuleConfig`, `DynamicModuleLoader`, `DynamicModuleRegistry`, `DynamicModuleDriver`) copied in, registry trimmed to this product's modules
- [ ] `paper-plugin.yml` has `loader: yv.tils.core.loader.DynamicModuleLoader`
- [ ] `YVtils.kt` drives both static and dynamic modules through every lifecycle stage, with the failure policy applied in `onLateEnablePlugin()`
- [ ] Default `modules.yml` content (which modules default to `true`) matches this product's intended out-of-the-box modules
- [ ] Tested locally end-to-end (see [Testing your migration locally](#testing-your-migration-locally)) - confirms clean enable, no `NoClassDefFoundError`, no CommandAPI error, correct module list logged
