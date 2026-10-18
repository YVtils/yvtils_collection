# Configuration and localization

The single active configuration module: the Configurate implementation formerly
called `config-v2`, now using project `:config` and package `yv.tils.config`.
The old Bukkit YAML/hand-written JSON implementation has been removed.

- `files/`: YAML/JSON loading, atomic replacement, typed object/list mapping.
- `data/`: GUI annotations, entry schemas and entry-backed settings.
- `language/`: translation registration, custom-file merging and locale lookup.
- `ConfigYVtils.kt`: embedded runtime lifecycle and late language loading.
- `src/test/`: malformed-save, failed-write and translation-preservation checks.

Missing files can use defaults; unreadable or invalid existing files fail loading
and must not be overwritten with defaults. Relative paths resolve below
`plugins/yvtils/`. This is an embedded library, not a plugin or dynamic feature.
Verify with `./gradlew :config:build :core:shadowJar`.
