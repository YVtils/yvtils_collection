# Common runtime

Follow [root AGENTS.md](../AGENTS.md) for toolchain, shared-runtime boundaries, and versioning.

- `src/main/kotlin/yv/tils/common/CommonYVtils.kt` loads collection-wide configuration and shared translations; this module also owns `PermissionManager`, shared listeners, and update-check helpers.
- `:common:shadowJar` is the runtime bundle embedded by core, including `config-v2`, `utils`, and shared dependencies. It is not a separately published feature artifact.
- `config/RootConfigState.kt` is the typed root schema; `config/ConfigFile.kt` derives its dotted-key compatibility map from that state. Use `applyState` for GUI updates so both stay synchronized.
- Keep GUI dependencies out of common: `CommonConfigGui` lives in the GUI library, and core installs common's opener after registration.
- Verification from the repository root: `./gradlew :common:build :core:shadowJar` when changing the embedded runtime.
