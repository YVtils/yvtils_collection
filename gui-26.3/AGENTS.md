# GUI compatibility build / Minecraft 26.3

Follow [root AGENTS.md](../AGENTS.md) and [shared GUI guidance](../gui-26.1/AGENTS.md).

- This supported artifact targets Minecraft 26.3.x with InvUI 2.5.x; it is not a superseded v1/v2 module.
- `build.gradle.kts` compiles `../gui-26.1/src/main/kotlin`; edit shared behavior there, not in a duplicate local source tree.
- InvUI dependencies here, root `paperApiVersionOverrides`, and core `DynamicModuleRegistry.GUI_ARTIFACTS` jointly define compatibility. This is currently the registry's GUI fallback build too.
- Verification from the repository root: `./gradlew :gui-26.3:build`; shared-source changes require all three GUI builds.
