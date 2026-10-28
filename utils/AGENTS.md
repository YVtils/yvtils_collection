# Shared utilities and module API

Follow [root AGENTS.md](../AGENTS.md) for toolchain, shared-runtime boundaries, and versioning.

- `src/main/kotlin/yv/tils/utils/modules/Core.kt` holds initialized launcher context; `modules/Module.kt` defines lifecycle interfaces, loaded-module metadata, and generated-version lookup used across features.
- These classes must have one runtime identity through common's embedded bundle. Consumers use `compileOnly`, not private shaded copies.
- `Module.readVersion` requires the exact Gradle project name. Its comment about editing per-module build versions is stale; edit `gradle/module-versions.properties`.
- Preserve the JDK `Consumer<Player>` type for `configGuiOpener`; this public callback crosses classloader boundaries.
- `src/main/java/yv/tils/utils/stats/{StatsApi,ModuleStats}.java` is the optional, consent-aware module telemetry publishing boundary. Keep it in the single shared runtime and expose only JDK/shared types (no feature classes or Kotlin function types). No backend means discard updates, never buffer; publishers must follow later backend changes and close on their module's disable.
- `stats/ModuleTelemetry.java` holds only currently registered module descriptors/publishers and a fixed aggregate event catalog, never counter history. `Module.addModule/removeModule` owns these publishers; core and GUI register separately. Best-effort event instrumentation must never interrupt feature operations. No player/input data or metrics gameplay exports belong in this catalog.
- `coroutine/CoroutineHandler.kt` runs work on `Dispatchers.IO`, not Paper's server thread. Schedule Bukkit mutations explicitly; utils disable cancels shared coroutine tasks.
- Verification from the repository root: `./gradlew :utils:build :core:shadowJar`; changes to module APIs require consumer compilation too.
