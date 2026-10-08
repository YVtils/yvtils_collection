# Essentials commands

Follow [root AGENTS.md](../AGENTS.md) for toolchain, dynamic-module publishing, and shared-runtime rules.

- `src/main/kotlin/yv/tils/essentials/EssentialYVtils.kt` wires utility commands and gameplay listeners. The class name is singular `EssentialYVtils`, unlike the Gradle module name.
- Command trees live in `commands/register/`, behavior in `commands/handler/`; language uses `LanguageProvider.registerEnumStrings<LangStrings>()`, and permissions come from `permissions/PermissionsData.kt`.
- Enable unregisters vanilla `gamemode` and `seed` before installing replacements. Preserve replacement/alias behavior when modifying registration.
- `config/StatesFile.kt` persists PvP and per-dimension access in `/essentials/states.json`. Load existing typed state before saving; this is runtime state, not a generic GUI-editable config.
- Verification from the repository root: `./gradlew :essentials:build`; command and listener behavior needs the root Paper/local-module workflow.
