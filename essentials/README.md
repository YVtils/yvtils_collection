# Essentials

Utility commands (flight, game mode, healing, speed, god mode, ping, seed), PvP and
dimension controls, chat colors, anvil-limit handling and optional Spawn Elytra.

InvUI's anvil cost property uses the vanilla cost label rather than overriding its
Survival formatting. The module retains real-cost localized feedback and native payment:
[investigation and decision](../docs/anvil-cost-display.md).

## Inventory administration

`/invsee <known-name-or-UUID>` opens an online/offline player's inventory;
`/ecsee <known-name-or-UUID>` opens their ender chest. Viewing uses
`yvtils.essentials.command.invsee` / `.ecsee`; editing additionally requires
the corresponding `.edit` permission. Permissions default to OP.

Left-click a target slot to exchange its whole stack with your cursor. Pick items
up normally from your bottom inventory first. Bulk/shift/number-key/drag operations
into the target view are blocked. Player slots 0–35 are storage/hotbar, 36–39 are
boots/leggings/chest/helmet, 40 is offhand; remaining view cells are unused.
Online slots refresh live and stale clicks are rejected. One viewer locks the
target; self-editing through the remote view is disallowed. Edits persist on every
exchange, not on close. Offline sessions prevent login until closed. Offline data
must have been saved on the current server data version; unrelated NBT is preserved
and external changes reject further edits. Unknown/never-played players are rejected.

## Structure

- `EssentialYVtils.kt`: lifecycle, listeners and command wiring.
- `commands/register/`, `commands/handler/`: command trees and behavior.
- `inventory/`: editor sessions, transactions and isolated offline NBT adapter.
- `config/`, `permissions/`, `language/`: settings/state and user-facing policy.
- `listeners/`, `logic/`: gameplay features including Spawn Elytra.

Enable `essentials`; files live in `plugins/yvtils/essentials/`.
Verify with `./gradlew :essentials:build`. Test editing armor/offhand, custom items,
failed disk writes, concurrent target changes, logout/login and shutdown on Paper.
