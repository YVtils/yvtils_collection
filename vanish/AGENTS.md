# Vanish

Follow [root AGENTS.md](../AGENTS.md) for toolchain and new-module wiring.

- `VanishYVtils` wires lifecycle; `commands/`, `language/`, `permissions/`, `logic/`, `listeners/` separate implementation. `VanishService` owns UUID-keyed main-thread visibility; higher layers see lower ones.
- Silent containers use read-only InvUI items/windows. Never copy their contents back to the world or replace them with transferable inventory slots. Capability permissions and config switches are both required.
- Disable restores collision and plugin-owned visibility; no permanent vanish state is persisted.
- `configs/ManageGUI.kt` uses the shared InvUI typed config editor with detached drafts. `yvtils.vanish.layers` enables numeric selection; `yvtils.vanish.layer.N` authorizes each layer. Capability permissions are independent `yvtils.vanish.function.*` nodes and require their config switches. Refresh closes unauthorized snapshots and restores collision/layer state after permission changes.
- Verify with `./gradlew :vanish:build` and the staging checks in `docs/breaking-release.md`.
