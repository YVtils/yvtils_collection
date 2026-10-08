# Configurate-backed configuration

Follow [root AGENTS.md](../AGENTS.md) for toolchain, shared-runtime boundaries, and versioning.

- Package `yv.tils.configv2` is the active replacement for legacy `config`; it is embedded via common. The entrypoint's comment claiming it is not wired into common is stale.
- `files/ConfigurateFileUtils.kt` centralizes YAML/JSON path resolution, loading, saving, and merging beneath shared `plugins/yvtils/` by default. Keep `baseDirectory` lazy/null by default; eager access to `Core.instance` can precede `Core.initCore`.
- `files/ObjectMapperFileUtils.kt` maps typed Kotlin state and save lists; JSON is its default format, so YAML configs must request `ConfigFormat.YAML`. Preserve field defaults for older files and existing save-wrapper keys.
- Object-mapper saves replace the represented state; load disk state before saving defaults. `ConfigurateFileUtils.update` instead preserves existing values unless overwrite is requested.
- `data/` supplies config schemas/annotations; `language/` supplies registration, locale lookup, and broadcast APIs. `ConfigV2YVtils.onLateEnablePlugin()` loads language files after module registration.
- Verification from the repository root: `./gradlew :config-v2:build :core:shadowJar`; config changes also affect all feature consumers and the data-class GUI editor.
