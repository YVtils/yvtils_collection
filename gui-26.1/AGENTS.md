# Shared GUI implementation / Minecraft 26.1

Follow [root AGENTS.md](../AGENTS.md) for toolchain, classloader boundaries, and GUI selection.

- `src/main/kotlin/yv/tils/gui/` is the canonical source for all three GUI builds; `gui-26.2` and `gui-26.3` compile this same tree. This module targets Minecraft 26.1.x with InvUI 2.1.x.
- `core/InvUIBootstrap.kt` must initialize InvUI before any window class is used, during module enable rather than load. A failed static window initializer poisons that class for the server session.
- Core resolves GUI unconditionally but does not drive `GUIYVtils` lifecycle. Preserve idempotent bootstrap paths rather than relying on that entrypoint being instantiated.
- Shared config editors and navigation live here, including `logic/DataClassConfigGui.kt` and `CommonConfigGui.kt`. Common's GUI opener is installed by core to avoid a dependency cycle.
- Consumers reference GUI via `compileOnly`; preserve a single runtime InvUI copy and the common `yv.tils.gui` API across builds.
- Verify shared changes from the repository root: `./gradlew :gui-26.1:build :gui-26.2:build :gui-26.3:build`.
