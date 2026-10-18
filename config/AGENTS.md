# Configurate-backed configuration

Follow [root AGENTS.md](../AGENTS.md) for toolchain, shared-runtime boundaries, and versioning.

- Package `yv.tils.config` provides the Configurate-backed shared configuration runtime embedded via common.
- `files/ConfigurateFileUtils.kt` centralizes YAML/JSON path resolution, loading, saving, and merging beneath shared `plugins/yvtils/` by default. Keep `baseDirectory` lazy/null by default; eager access to `Core.instance` can precede `Core.initCore`.
- `files/ObjectMapperFileUtils.kt` maps typed Kotlin state and save lists; JSON is its default format, so YAML configs must request `ConfigFormat.YAML`. Preserve field defaults for older files and existing save-wrapper keys.
- Object-mapper saves replace the represented state; load disk state before saving defaults. `ConfigurateFileUtils.update` instead preserves existing values unless overwrite is requested.
- Missing typed files return defaults; malformed/unreadable files and invalid list records throw. Do not catch these errors and save defaults. Writes use same-directory atomic replacement.
- `data/` supplies config schemas/annotations; `language/` supplies registration, locale lookup, and broadcast APIs. `ConfigYVtils.onLateEnablePlugin()` loads language files after module registration.
- Verification from the repository root: `./gradlew :config:build :core:shadowJar`; config changes also affect all feature consumers and the data-class GUI editor.
