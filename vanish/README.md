# Vanish

`/vanish` (`/v`) toggles layer 1; `/vanish <1-100>` selects a staff layer.
Ordinary players cannot see vanished staff; higher/equal active layers see lower
ones. Vanished players do not attract mobs, pick up/drop items, collide normally
or participate in direct attacks. Silent container viewing is a read-only snapshot.

- `VanishYVtils.kt`: lifecycle only.
- `commands/`, `permissions/`, `language/`: command policy and localized feedback.
- `logic/VanishService.kt`: visibility pairs, layers and collision restoration.
- `listeners/VanishListener.kt`: entity/player protections and join/quit handling.
- `listeners/SilentContainers.kt`: read-only InvUI container snapshots and window cleanup.
- `configs/`: typed settings and the shared data-class config editor.
- `VanishPolicy.kt`, `src/test/`: pure layer visibility policy and tests.

Enable `vanish`. Permissions `yvtils.vanish.use` and `.layers` default to OP.
Each selected layer additionally requires `yvtils.vanish.layer.<1-100>`; `.layers`
only grants access to the numeric command, not every layer. `/vanish config`
requires `yvtils.vanish.config` and opens the shared InvUI typed config editor.
It is also available from core's config list. Settings live in `/vanish/config.yml`.

Capabilities require both their config switch and their own permission:

- `yvtils.vanish.function.silentcontainers`
- `yvtils.vanish.function.preventpickup`
- `yvtils.vanish.function.preventdrop`
- `yvtils.vanish.function.preventmobtarget`
- `yvtils.vanish.function.preventdamage`
- `yvtils.vanish.function.preventphysical`
- `yvtils.vanish.function.disablecollision`

All default to OP; `yvtils.vanish.*` contains the registered capabilities/layers.
Denying a capability restores ordinary behavior for that function. Collision and
layer access refresh every second and when settings change. Silent container
snapshots now use InvUI `Gui`/`Item`/`Window`, so items remain GUI-only and cannot
be withdrawn. Config updates close existing snapshots. Translation enum keys are
grouped under `vanish.command`, `vanish.error`, `vanish.gui` and `vanish.config`.
State is not persisted; disable restores owned visibility/collision.
Personal preferences are persisted separately in `plugins/yvtils/vanish/players.json`:
open `/vanish settings` to choose a permitted layer, announcements and individual
capabilities. A preference cannot grant a capability denied by permissions or global
configuration. `/vanish silent` toggles without a fake join/leave announcement;
disabling announcements in personal settings makes ordinary toggles quiet too.
With Server enabled, fake announcements use its custom messages and enabled switch;
otherwise the vanilla translated join/leave message is used. Real quits while
vanished are suppressed in both Vanish and Server's message handler.
The InvUI settings screen pairs item icons with green/red controls underneath,
matching the Vanish menu design. Additional capabilities and layer selection occupy
a second section. Join/leave announcements include the configured Status display
when that module is enabled and the player has a saved status.

Silent containers refresh once per second from the live block inventory, including
changes made by other players/hoppers. They remain read-only and do not open the
real container. Removed/replaced containers, unloaded chunks or lost capability
close the view. Container inventory state is never copied back into the world.
Verify with `./gradlew :vanish:build` plus multi-player/container checks.
