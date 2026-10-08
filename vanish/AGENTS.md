# Vanish scaffold

Follow [root AGENTS.md](../AGENTS.md) for toolchain and new-module wiring.

- `vanish` is currently an included Gradle scaffold with an empty dependency block and no source tree or module entrypoint. Root README's extended-vanish features are not implemented in this directory.
- It is absent from `publishableModules`, the centralized version entries, and `DynamicModuleRegistry.KNOWN_MODULES`; existing build JARs do not establish runtime availability.
- Implementing this feature requires the root new-module registration steps and shared-runtime dependency scopes before it can be enabled in core.
- Scaffold verification from the repository root: `./gradlew :vanish:build`; this currently validates no gameplay implementation.
