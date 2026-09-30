# RegionsV2

Full-height survival claims created through commands or a GUI, backed by WorldGuard, with particle previews,
Owner/Member/Visitor roles and in-game management menus.

Part of the [YVtils](https://yvtils.net) plugin collection - see the
[root README](../README.md) for the full module list and how the dynamic
module system works.

## Dependencies

- `utils`, `config-v2`, `common` (always `compileOnly`, provided by the shared runtime tier)
- `gui-26.1` (`compileOnly`, for the in-game config editor)
- WorldGuard Bukkit 7.0.18 (`compileOnly`, provided by the installed WorldGuard plugin)
- WorldEdit or FastAsyncWorldEdit (WorldGuard's runtime dependency)

## Commands

- `/regionsv2` - opens the claims GUI (all worlds; admins see all managed claims)
- `/regionsv2 pos1` and `/regionsv2 pos2` - select your X/Z coordinates as the two footprint corners
- `/regionsv2 preview` - show selection edges to you with particles for 15 seconds
- `/regionsv2 clear` - cancel your selection and preview
- `/regionsv2 create <name>` - create the selected cuboid and become its owner
- `/regionsv2 create <name> <x1> <z1> <x2> <z2>` - create directly from coordinates
- `/regionsv2 createat <world> <owner name/UUID> <name> <x1> <z1> <x2> <z2>` - admin/console creation
- `/regionsv2 info [name/UUID]` - public detailed information; no argument inspects your location
- `/regionsv2 list` - public list of loaded-world claims
- `/regionsv2 delete <name/UUID>` - remove a claim
- `/regionsv2 role <claim> <player name/UUID> <OWNER|MEMBER|VISITOR>`
- `/regionsv2 flag <claim> <flag> <GLOBAL|OWNER|MEMBER|VISITOR> <value>`
- `/regionsv2 rename <claim> <new name...>`
- `/regionsv2 resize <claim> <x1> <z1> <x2> <z2>`
- `/regionsv2 merge <kept claim> <other claim>`
- `/regionsv2 message <claim> <welcome|goodbye> <text...>` (`unset` restores translated defaults)
- `/regionsv2 manage <name>` - open a claim in your current world
- `/regionsv2 admin` - open server configuration and editable-flag controls
- `/regionsv2 worldguard` - lists region IDs at your current location (player-only)

The Create Claim menu sets either corner at your current position, previews the selection,
accepts a name and asks for creation confirmation. Walk to each corner and reopen the menu
to select it. Commands remain available. Menus resize to their content (3–6 rows), use
custom-textured heads, and paginate larger lists.

Every claim extends from the world's minimum height through maximum build height.
Particle previews include a horizontal footprint outline at your viewing height.
Names contain 1–64 printable characters and need not be unique; UUIDs are permanent
identities. Use UUIDs when a name is ambiguous. Overlaps with
existing WorldGuard regions and selections outside the world border are rejected.

## Permissions

- `yvtils.regions-v2.claim` (default: true) - selection and creation
- `yvtils.regions-v2.manage` (default: true) - commands and management menus
- `yvtils.regions-v2.admin` (default: false) - all managed claims and server settings;
  bypasses survival mode and claim-count limits, but not overlap or size checks
- `yvtils.regions-v2.worldguard` (default: true) - access to the region query
- `yvtils.regions-v2.*` - wildcard for all of the above

## Config

`plugins/YVtils/regions-v2/config.yml`, editable in-game via `/yvtils config`:

| Key                       | Type        | Default              | Description                                                                         |
|---------------------------|-------------|----------------------|-------------------------------------------------------------------------------------|
| `enabled`                 | boolean     | `true`               | Enables activation and claim management; restart to activate after startup-disabled |
| `maxClaimsPerWorld`       | integer     | `5`                  | Claims owned by each player in each world                                           |
| `maxClaimsTotal`          | integer     | `5`                  | Ownership limit across all worlds, including unloaded worlds                        |
| `minClaimArea`            | long        | `1`                  | Minimum inclusive horizontal footprint area                                         |
| `maxMembersPerClaim`      | integer     | `-1`                 | Maximum explicit members per claim                                                  |
| `maxMembershipsPerPlayer` | integer     | `-1`                 | Maximum member assignments across all worlds                                        |
| `actionBarTransitions`    | boolean     | `true`               | Welcome/goodbye action bars                                                         |
| `maxClaimVolume`          | long        | `30000000`           | Maximum full-height block volume; existing configured limits are retained           |
| `maxClaimSide`            | integer     | `256`                | Maximum inclusive horizontal length on X/Z                                          |
| `survivalOnly`            | boolean     | `true`               | Survival required for creation (admins exempt)                                      |
| `disabledWorlds`          | string list | `[]`                 | Worlds where creation is disabled                                                   |
| `enabledRoleFlags`        | string list | see generated config | Legacy enabled list, superseded by per-flag policy                                  |
| `enabledGlobalFlags`      | string list | see generated config | Legacy enabled list, superseded by per-flag policy                                  |
| `flagPolicies`            | map         | `{}`                 | Admin-menu policies: editing enabled, role/global scope, and defaults               |

`/regionsv2 admin` → WorldGuard flag policies lists every flag in the live registry,
including custom flags from other plugins. Open a flag to configure owner editing,
global/per-role scope, and server defaults (one global default or separate defaults
for Owner, Member and Visitor). State and boolean defaults are toggles; other types
use WorldGuard's native input parser. Enter `unset` to remove a value; state/boolean
defaults also have explicit unset buttons. Defaults are stored using WorldGuard's
marshal/unmarshal format so location, set and map values retain their types.

Changing editing availability, scope, or defaults resets that flag to server defaults
in **all managed claims**, removing stale role/global values. Loaded worlds update
immediately; a persisted revision history applies pending resets when other worlds load.
Other plugins' regions are not modified. Group flags, build, passthrough and nonplayer
protection domains are listed as reserved because they wire the protection model.
Flags without a group flag are global-only. Role-scoping environmental flags that have
no player subject uses WorldGuard's native non-member evaluation; global scope is usually
appropriate for explosions, growth, weather and similar events.

Owners can manage the claim, promote/demote players (including granting full ownership),
and delete it. Members have no management rights. Visitors are all players without an
explicit owner/member assignment. Removing membership is done by assigning Visitor.
The last owner cannot be removed. Unknown player names resolve asynchronously through
Paper's profile API, with a 15-second timeout. Cached names and UUIDs resolve immediately.
All maximum limits accept `-1` for unlimited; count limits also accept `0` to prohibit
new additions. Admins bypass ownership/membership count limits. Minimum area, geometry,
world-border and overlap checks apply to everyone.

Information is public and read-only: UUID, name, creation time, world, footprint,
owners, members, current viewer role, flags and online occupancy. Console supports
all non-GUI management commands; use UUIDs to disambiguate names across worlds.
Dynamic suggestions offer UUIDs/names, worlds, online players, roles and registered flags.

Resize updates all three WorldGuard cuboids atomically and preserves UUID, roles, flags
and metadata. Merge requires two same-world claims with identical domains, flags and
transition messages whose union forms a rectangle. It keeps the first claim's identity
and removes the second. Neither operation silently adds a gap or L-shaped remainder.
Both operations are also available in the owner GUI (resize uses pos1/pos2).

Welcome/goodbye action bars are evaluated every 10 ticks; direct transitions show both
messages. Custom plain-text messages support `{region}` and `{player}`. Empty/unset values
use English/German defaults. The GUI includes message and rename editors.
Built-in commands, menus, configuration controls and validation messages use the shared
EN/DE language catalogue; WorldGuard flag identifiers and user-supplied values stay literal.
Strings are defined by `language/LangStrings.kt` (`LanguageProvider.LangStrings`) and
registered through `LanguageProvider`. `LanguageHandler` resolves named placeholders,
server/player language preferences and MiniMessage components. Shared GUI navigation
translations are reused, with InvUI `PagedGui`, `Filler`, `HeadUtils` and YVtils colors.
The small `RegionMessage` helper only carries an explicit key and parameters; it does
not implement language selection or match English text. Permissions use the shared
`PermissionManager` and are registered before CommandAPI commands.

Without explicit admin overrides, new claims allow owners and members the original role actions. Visitors may enter,
pick up/drop items and use ender pearls; other supported role actions are denied.
Other flags initially use WorldGuard's unset value. Admin defaults override these initial values. Existing server-wide
rules and
WorldGuard bypass permissions still apply according to WorldGuard's normal evaluation.

## Development notes

The module is statically bundled into core's main plugin JAR and remains selectable
through `modules.yml`. Core declares an optional `WorldGuard` server dependency with
`load: BEFORE` and `join-classpath: true`. The module skips activation when WorldGuard
is absent or disabled.

Use ordinary WorldGuard/WorldEdit imports in `commands`, `logic`, and `listeners`.
There is no custom classloader or reflective API bridge. `logic/Regions.kt` contains
reusable helpers using the live WorldGuard API. Keep WorldGuard-dependent operations
behind the enable-time dependency check, and do not bundle a second copy of its API.

Geometry, protection flags and domains are stored by WorldGuard. Display metadata is
stored in `/regions-v2/claims.json`, indexed by UUID, with creation time, world UUID,
display name, messages and domain snapshots for limits in unloaded worlds. No import
of legacy regions claims/configuration is provided.
Earlier name-keyed **regions-v2** cuboids are re-keyed to UUIDs when their world loads;
their display name, domains and flags are preserved. Their original creation date was
not recorded, so the conversion time is used for those claims.
Each claim uses a primary cuboid named
`yv2_<uuid>` plus two overlapping policy cuboids (`__owner`, `__member`) at priority +1. Their
owner domains identify the role targeted by their OWNERS flag group; their passthrough
flag keeps native build membership on the primary claim. These policy domains are
implementation details, not additional application-level ownership grants. Visitors
use NON_MEMBERS on the primary claim. Keep the three regions together and manage
roles/geometry through this module; external `/rg` edits can desynchronize them.
Deleting a claim removes all three regions. Changes call WorldGuard's saveChanges;
failed saves roll back the in-memory mutation and report an error.

Run `./gradlew :regions-v2:test` for tests against WorldGuard's real flag calculator,
including all eight independent role combinations for every supported role flag.
Tests also cover cross-world limits, unlimited/zero settings, membership limits and
rectangle merge validation. `ClaimOccupancy.claimOf(player)` and
`ClaimOccupancy.playersIn(claimUuid)` provide a main-thread live-location API.

## TODOs

- [ ] Update GUI Design to fit to other gui layouts and better overview
- [ ] Add default flag settings
- [ ] Rename command to 'region' and 'rg' as alias
- [ ] Add animations to gui
- [ ] think if there are any other features
- [ ] add currency feature to regions (size based, for example 2x2 chunks free, then 1 diamond for 3x3, ...)
- [ ] cleanup