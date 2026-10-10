# Repository guidance

## Build and verification
- Use JDK 25 and `./gradlew` (Gradle 9.6.1); Kotlin targets JVM 25. Paper dev bundles are resolved during builds, including focused module builds.
- Full verification: `./gradlew build --no-daemon`. Focused module verification: `./gradlew :regions:build` (substitute the exact project name from `settings.gradle.kts`; `multiMine` is case-sensitive).
- Existing test suites: `./gradlew :regions:test :discord:test :remade-ender-dragon:test`. Single class: `./gradlew :regions:test --tests 'yv.tils.regions.logic.ClaimPricingTest'`. No repository-configured lint/formatter task exists.
- Build the deployable launcher with `./gradlew :core:shadowJar`; output is `core/build/libs/YVtils_v<core-version>.jar`. Feature-module JARs are Maven libraries, not independently installable Bukkit plugins.

## Runtime boundaries
- The real plugin entrypoint is `core/src/main/kotlin/yv/tils/core/YVtils.kt`, wired through `core/src/main/resources/paper-plugin.yml`. It drives embedded `config-v2`, `utils`, `common` plus modules discovered by `DynamicModuleDriver`.
- `core` embeds `common:shadowJar` as `embedded/yvtils-runtime.jar`; this contains the shared runtime, including `config-v2` and `utils`. Feature modules use `compileOnly` for these projects and for GUI types (`project(":gui-26.1")`). Root `usesSharedRuntimeTier` supplies CommandAPI/coroutines/serialization as `compileOnly`.
- Paper's library tier cannot see classes in the launcher's main JAR. Do not shade duplicate shared runtime/Kotlin/CommandAPI copies into core or feature modules: classloader identity and initialized global state must stay shared.
- `core/src/main/java/yv/tils/core/loader/{DynamicModuleLoader,DynamicModuleRegistry,ModuleConfig}.java` must remain Java with no Kotlin/shared-runtime dependency: Paper loads them before that runtime exists. Loader and plugin instances use isolated classloaders; communicate through config files rather than shared mutable static state.
- `regions` is the static-bundling exception: root `staticBundledModules`, core's `implementation` dependency, and registry `isStatic` flag keep it in the main plugin classloader for WorldGuard access. Keep WorldGuard `compileOnly` and core's `join-classpath: true` dependency.
- `gui-26.2` and `gui-26.3` compile the Kotlin sources in `gui-26.1/src/main/kotlin`; edit shared GUI code there. The three builds provide the same `yv.tils.gui` API for different Minecraft/InvUI versions. Core resolves exactly one unconditionally; keep GUI dependencies `compileOnly`. Coordinate version support changes across GUI builds, root Paper API overrides, and `DynamicModuleRegistry.GUI_ARTIFACTS`.

## Changing modules
- Version format: `<year>.<month>.<iteration>[-<beta|alpha|dev|deprecated>.<iteration>]`, using a two-digit year and month (e.g. `26.10.01` or `26.10.01-beta.2`). The third component is the release iteration within that month, not the day of the month; the suffix iteration counts revisions for that release stage. Stable releases omit the suffix.
- `gradle/module-versions.properties` is the module-version source of truth. Gradle generates `module-version-<project>.properties` and `GeneratedModuleVersions.java`; the misleadingly named `generateModuleVersionsKotlin` task generates Java. Edit the properties file, not generated outputs or registry version literals. Published release coordinates are immutable.
- Whenever changing a module, update its version tag in `gradle/module-versions.properties`. Compare against the branch's merge base with `main` (or `origin/main`): bump the base release version once after that merge, then retain that base and use `-dev.1`, `-dev.2`, etc. for each subsequent logical change (not each individual file edit or build). If the base is already bumped, add `-dev.1` or increment its existing dev revision; use the documented `-dev.<iteration>` format, not `.dev`. Remove the dev suffix only when preparing a stable release; published coordinates remain immutable.
- New dynamic modules need wiring in `settings.gradle.kts`, root `publishableModules`, `gradle/module-versions.properties`, and Java `DynamicModuleRegistry.KNOWN_MODULES`. Their `Module.YVtilsModule` entrypoint needs a public no-arg constructor and `Module.addModule(MODULE)` during enable; read its version via `Module.readVersion`.
- `scripts/new-module.sh` is currently stale: it targets the removed Kotlin registry and old build-file insertion patterns, and does not add the centralized version entry. It can partially modify the repo before failing; inspect/fix its wiring before using it.
- For loaded modules use `config-v2` (`yv.tils.configv2`), not legacy `config`. Follow `status/src/main/kotlin/yv/tils/status/configs/ConfigFile.kt`: typed state via `ObjectMapperFileUtils`, with any compatibility map derived from state. Register translations through `BuildLanguage` in `onLoad()` and retrieve them with `LanguageHandler`.
- `docs/adding-a-new-module.md` and `docs/migrating-to-dynamic-modules.md` explain intent but contain stale `test-core`, Kotlin-loader, legacy-config and migration-status references. Current Gradle files and `core` loader sources take precedence. Consult module-specific docs/READMEs for feature semantics.

## Local runtime testing
Building core alone does not make edited dynamic modules available to its server. Publish locally, serve the Maven cache in another terminal, then start core:

```sh
./gradlew publishAllModulesLocally
jwebserver -p 8095 -d "$HOME/.m2/repository"
REPOSILITE_URL="http://127.0.0.1:8095/" ./gradlew :core:runServer
```

- Focused publish: `./gradlew :sit:publishToMavenLocal generateLocalMavenChecksums`; publish required feature dependencies and the matching GUI artifact too. Plain `publishToMavenLocal` lacks checksums; runtime resolution requires an HTTP(S) repository.
- Enable features in `core/run/plugins/yvtils/modules.yml` (lowercase shared data directory), then restart. Defaults are all disabled; GUI is always fetched. `yv-smp` is manual opt-in; `migration` is hidden and cannot be enabled here.
- Paper also caches resolved artifacts under `core/run/libraries/yv/yvtils/`, independently of `~/.m2/repository`. When reusing a local module version, remove that module/version's stale Paper cache before restarting, or use a new version.
- Publishing credentials/environment are documented in `.env.example`; Gradle does not auto-load `.env` (`set -a; source .env; set +a`). Release CI runs `./gradlew build publish --no-daemon -PskipExistingReleases=true`, skipping complete existing releases and failing incomplete ones.
