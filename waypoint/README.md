# Waypoints

Persistent public/private/unlisted locations with private terrain highlighting.

```text
/waypoint create <name> [public/private/unlisted]
/waypoint list
/waypoint navigate <name>
/waypoint delete <name>
/waypoint stop
/waypoint
/waypoint manage
/waypoint config
```

`/wp` is an alias; creation defaults to private. Names are globally unique and
case-insensitive (1–64 letters/digits/`_`/`-`). Public locations appear in everyone's
suggestions/lists; private locations are owner-only; unlisted locations can be
navigated by name but do not appear to other players in suggestions/lists.

Navigation highlights up to 32 nearby ground blocks using glowing, slightly
expanded block displays visible only to that navigator. The guide updates as the
player moves, follows nearby walkable terrain and stops at unloaded chunks or
unwalkable gaps. It is a directional guide, not obstacle-avoiding pathfinding.
Action-bar arrows and distance follow the older SMP navigator; selecting the same
destination toggles navigation off. No compass or real world blocks are modified.
World changes, death, quit, arrival, deletion and disable remove displays.

- `WaypointYVtils.kt`: lifecycle and wiring.
- `Waypoint.kt`, `WaypointPolicy.kt`: data model and visibility rules.
- `commands/`, `permissions/`, `language/`: commands and feedback.
- `config/WaypointStore.kt`: validation and atomic typed persistence.
- `config/ConfigFile.kt`, `ManageGUI.kt`: typed module settings and shared config editor.
- `gui/WaypointsGui.kt`: paginated player/admin management, input and confirmation screens.
- `logic/TerrainTrail.kt`: bounded ground sampling/direction calculation.
- `logic/BlockTrailDisplay.kt`: private non-persistent display ownership.
- `logic/WaypointNavigation.kt`, `listeners/`: run scheduling and cleanup.

Enable `waypoint`; state: `plugins/yvtils/waypoint/waypoints.json`.
Permissions: `yvtils.waypoint.use`, `.create`, `.admin` (OP defaults).
`/waypoint` opens the InvUI player browser; `/waypoint manage` requires `.admin`
and includes all owners/private records. Detail screens navigate, rename, move to the
actor's position, cycle visibility and confirm deletion. Name lookup supports
unlisted destinations without exposing them in listings. Creation uses anvil input
and initially creates a private waypoint. Every mutation rechecks permissions and
the current saved record. `.config` opens the shared typed config GUI (also listed
under `/yvtils config`). Settings persist to `/waypoint/config.yml`: trail length,
highlight material, arrival distance and per-owner creation limit.
Translations use `LangStrings` enum entries grouped under `waypoint.command`,
`waypoint.navigation`, `waypoint.error`, `waypoint.gui` and `waypoint.config`.
Verify with `./gradlew :waypoint:build` and two clients to check display privacy.
