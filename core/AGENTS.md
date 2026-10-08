# Core launcher

Follow [root AGENTS.md](../AGENTS.md) for toolchain, classloader rules, versioning, and local module resolution.

- `src/main/kotlin/yv/tils/core/YVtils.kt` is the Paper plugin entrypoint; `src/main/resources/paper-plugin.yml` selects it and the Java bootstrap loader.
- `DynamicModuleLoader` constructs the classpath; Kotlin `DynamicModuleDriver` re-reads `modules.yml` and instantiates module entrypoints. Communicate between these stages through files, not bootstrap static state.
- `YVtils` drives load, enable, late-enable, and disable for the embedded runtime and discovered features. GUI is resolved as a library but its `GUIYVtils` lifecycle is not driven.
- Core patches `common`'s config GUI opener after enable to avoid a `common` → `gui` → `common` dependency cycle.
- Keep supported server versions in `YVtils.kt`, GUI mappings, and Paper targets coordinated. Preserve optional WorldGuard `join-classpath` for statically bundled regions.
- Verification: `./gradlew :core:build`; deployable artifact: `./gradlew :core:shadowJar`. Edited dynamic features still require the root local-publish workflow.
