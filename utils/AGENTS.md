# Shared utilities and module API

Follow [root AGENTS.md](../AGENTS.md) for toolchain, shared-runtime boundaries, and versioning.

- `src/main/kotlin/yv/tils/utils/modules/Core.kt` holds initialized launcher context; `modules/Module.kt` defines lifecycle interfaces, loaded-module metadata, and generated-version lookup used across features.
- These classes must have one runtime identity through common's embedded bundle. Consumers use `compileOnly`, not private shaded copies.
- `Module.readVersion` requires the exact Gradle project name. Its comment about editing per-module build versions is stale; edit `gradle/module-versions.properties`.
- Preserve the JDK `Consumer<Player>` type for `configGuiOpener`; this public callback crosses classloader boundaries.
- `coroutine/CoroutineHandler.kt` runs work on `Dispatchers.IO`, not Paper's server thread. Schedule Bukkit mutations explicitly; utils disable cancels shared coroutine tasks.
- Verification from the repository root: `./gradlew :utils:build :core:shadowJar`; changes to module APIs require consumer compilation too.
