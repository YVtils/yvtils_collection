# Shared GUI — Minecraft 26.1

Canonical GUI source tree for all GUI variants, compiled against InvUI 2.1.x.
Core always resolves one GUI library for the server version; it is not a feature toggle.

- `core/InvUIBootstrap.kt`: idempotent InvUI plugin setup during enable.
- `logic/ConfigGui.kt`: entry-backed settings editor.
- `logic/DataClassConfigGui.kt`: annotation-driven typed settings editor.
- `logic/CommonConfigGui.kt`, `ToggleControl.kt`: shared entrypoints/controls.
- `utils/`, `language/`: styles, heads, fillers and translations.

Consumers use `compileOnly`; shared runtime classes must not be shaded again.
Verify shared changes with `./gradlew :gui-26.1:build :gui-26.2:build :gui-26.3:build`.
All three versions must be bumped when shared sources change.
