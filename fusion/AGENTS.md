# Fusion crafting

Follow [root AGENTS.md](../AGENTS.md) for toolchain and new-module wiring.

- `FusionYVtils` is the dynamic entrypoint. Keep shared runtime and GUI dependencies compile-only.
- Follow `commands/`, `configs/`, `data/`, `gui/`, `language/`, `listeners/`, `logic/` and `utils/` separation. Keep lifecycle wiring in the entrypoint, not command implementations.
- Public recipe models are physically in `data/` but keep their original `yv.tils.fusion` package for addon compatibility. Tests mirror `logic/crafting` and `logic/items/flask`.
- `RecipeStore` strictly loads the typed schema and atomically persists detached editor drafts. Do not overwrite malformed existing files with defaults.
- `IngredientAllocation` uses integral max flow for overlapping alternative ingredients. `Crafting` plans on clones and commits on the server thread only.
- Persistent item/entity keys use the stable `yvtils` namespace. Preserve stored invisible-frame items when changing recipes or output metadata.
- `api/FusionApi` is the server-thread service contract; output types, behaviors, runtime recipes, guards and observers register through `FusionRegistry`. Use these hooks instead of adding new hard-coded switches.
- Recipes use individual `recipes/<id>.json` files with schemaVersion 1. If the directory is absent, combined JSON/YAML is atomically split and retained as backup. An existing empty directory is authoritative. Nourishing Flask usage spends raw XP points, not levels.
- See `README.md` for recipe syntax/runtime checks, `API.md` for extensions and `RECIPE_IDEAS.md` for future ideas.
- Verify with `./gradlew :fusion:build :core:build`.
