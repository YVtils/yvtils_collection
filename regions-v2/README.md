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

- `/region` - opens the main region menu
- `/region pos1` and `/region pos2` - select your X/Z coordinates as the two footprint corners
- `/region preview` - show selection edges to you with particles for 15 seconds
- `/region clear` - cancel your selection and preview
- `/region cost` - quote the diamond price of your selection
- `/region create <name>` - create the selected cuboid and become its owner
- `/region create <name> <x1> <z1> <x2> <z2>` - create directly from coordinates
- `/region createat <world> <owner name/UUID> <name> <x1> <z1> <x2> <z2>` - admin/console creation
- `/region info [name/UUID]` - public detailed information; no argument inspects your location
- `/region list` - public list of loaded-world claims
- `/region delete <name/UUID>` - remove a claim
- `/region role <claim> <player name/UUID> <OWNER|MEMBER|VISITOR>`
- `/region flag <claim> <flag> <GLOBAL|OWNER|MEMBER|VISITOR> <value>`
- `/region rename <claim> <new name...>`
- `/region resize <claim> <x1> <z1> <x2> <z2>`
- `/region merge <kept claim> <other claim>`
- `/region message <claim> <welcome|goodbye> <text...>` - admin-only (`unset` restores translated defaults)
- `/region manage <name>` - open a claim in your current world
- `/region admin` - open server configuration and editable-flag controls
- `/region worldguard` - lists region IDs at your current location (player-only)

`/regionsv2` remains a compatibility alias. `/rg` routes execution and tab completion
according to the sender's current permissions: players granted `worldguard.*`,
`worldguard.region.*`, or an effective `worldguard.region.<...>` command permission use
WorldGuard; other players use YVtils. Console uses WorldGuard. WorldGuard protection
bypass permissions alone do not select its commands. The YVtils
`yvtils.regions-v2.worldguard` query permission does not select WorldGuard routing.
Use `/worldguard:region` to address WorldGuard directly and `/regionsv2` to address
YVtils directly. WorldGuard still checks permission for each requested operation.

The Create Claim menu sets either corner at your current position, previews the selection,
accepts a name and asks for creation confirmation. Walk to each corner and reopen the menu
to select it. Commands remain available. Menus use custom-textured player heads.

- **Main menu (3×9):** four spaced heads in the middle row for My Regions, Create,
  server settings (admins only), and the current region (closes the menu and sends a
  compact chat summary, or feedback that no region exists at the current location).
- **My Regions (5×9):** back in the top-left corner; 21 region heads per page in
  columns 2–8 of the three interior rows; previous/page indicator/next centered in
  the bottom row. Claims are sorted with the current world first, and show dimensions,
  viewer role. Admins see all managed claims.
- **Region management (5×9):** persistent top-row tabs for Information, Members,
  Flags and Settings, with back in the top-left. Information opens first; the selected
  tab is marked in its lore. Information uses just two summary heads: Basic Info
  (owners and creation date in `dd.MM.yyyy HH:mm zone` format), and Location
  (world, dimensions and corners; click to highlight the boundary).
  Flags opens an overview with one head per Global/Owner/Member/Visitor group, whose
  lore lists its explicitly set values. Owners/admins can click a group to open its editor.
  Flag values appear only in flag windows. Member entries use the actual player's skull.
  Members and flag editing require ownership or admin rights.
  Settings contains rename, resize, merge, flag-default reset, admin-only transition messages and
  confirmed deletion. Editing actions retain the management tabs.

Auxiliary creation, input and confirmation menus remain available. Borders are static;
the glass-pane animation and its configuration toggle have been removed. Non-paged
windows hide the page indicator. Window titles are not repeated as decorative heads.

Every claim extends from the world's minimum height through maximum build height.
Particle previews include a horizontal footprint outline at your viewing height.
Names contain 1–64 printable characters and need not be unique; UUIDs are permanent
identities. Use UUIDs when a name is ambiguous. Overlaps with
existing WorldGuard regions and selections outside the world border are rejected.

## Permissions

- `yvtils.regions-v2.claim` (default: true) - selection and creation
- `yvtils.regions-v2.manage` (default: true) - commands and management menus
- `yvtils.regions-v2.admin` (default: false) - all managed claims and server settings;
  bypasses survival mode, diamond costs and claim-count limits, but not overlap or size checks
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
| `currencyEnabled`         | boolean     | `true`               | Charge diamonds for creation and expansion                                          |
| `freeClaimChunks`         | integer     | `2`                  | Free maximum footprint side, in 16-block chunks                                     |
| `diamondsPerChunk`        | integer     | `1`                  | Diamonds per additional longest-side chunk tier                                     |
| `maxClaimVolume`          | long        | `30000000`           | Maximum full-height block volume; existing configured limits are retained           |
| `maxClaimSide`            | integer     | `256`                | Maximum inclusive horizontal length on X/Z                                          |
| `survivalOnly`            | boolean     | `true`               | Survival required for creation (admins exempt)                                      |
| `disabledWorlds`          | string list | `[]`                 | Worlds where creation is disabled                                                   |
| `enabledRoleFlags`        | string list | see generated config | Legacy enabled list, superseded by per-flag policy                                  |
| `enabledGlobalFlags`      | string list | see generated config | Legacy enabled list, superseded by per-flag policy                                  |
| `flagPolicies`            | map         | `{}`                 | Admin-menu policies: editing enabled, role/global scope, and defaults               |

`/region admin` → WorldGuard flag policies lists every flag in the live registry,
including custom flags from other plugins. Open a flag to configure owner editing,
global/per-role scope, and server defaults (one global default or separate defaults
for Owner, Member and Visitor). State and boolean defaults are toggles; other types
use WorldGuard's native input parser. Enter `unset` to remove a value; state/boolean
defaults also have explicit unset buttons. Defaults are stored using WorldGuard's
marshal/unmarshal format so location, set and map values retain their types.
The policy list also summarizes each role's current server default. Owners can use
**Restore server flag defaults** in a claim menu, with confirmation, to reset all
custom flag values in that claim while preserving owners and members.

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
Translation labels that also have child keys use a `.title` leaf to avoid YAML
scalar/map collisions (for example `gui.create.title` and `gui.create.lore`).
All navigation keys used by the module are registered, including Cancel.
The small `RegionMessage` helper only carries an explicit key and parameters; it does
not implement language selection or match English text. Permissions use the shared
`PermissionManager` and are registered before CommandAPI commands.

Without explicit admin overrides, new claims allow owners and members the original role actions. Visitors may enter,
pick up/drop items and use ender pearls; other supported role actions are denied.
Other flags initially use WorldGuard's unset value. Admin defaults override these initial values. Existing server-wide
rules and
WorldGuard bypass permissions still apply according to WorldGuard's normal evaluation.

## Development notes

### Diamond pricing

Pricing uses `max(0, ceil(max(width, depth) / 16) - freeClaimChunks) * diamondsPerChunk`.
Dimensions are inclusive block lengths, independent of actual chunk boundaries and
world height. With defaults, up to 32 × 32 blocks is free, 48 × 48 costs 1 diamond,
64 × 64 costs 2, and 80 × 80 costs 3. Rectangular claims use their longer side.
Both settings must be non-negative; setting `currencyEnabled: false` disables charges.
They are editable through server controls or the shared config editor.

Create, resize and merge confirmation menus show the payable amount. Commands apply
the same pricing; `/region cost` provides a selection quote. Payment uses diamonds in
the player's main inventory/hotbar, excluding armor and offhand. A failed save restores
the inventory snapshot along with the region mutation. Admin and console operations
are free. Shrinking/deleting gives no refund; previously credited size remains available
on that claim for re-expansion. Merging combines both claims' credits and charges any
remaining difference, including when combining free claims into a paid size.

Existing claims without a stored credit are grandfathered at their current footprint
price. Successful creation/resize/merge stores the credit in `claims.json`; credits
remain diamond amounts if the server later changes tier prices. Free/admin operations
also credit their resulting size so a later owner is not charged retroactively.

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
roles/geometry through this module; external WorldGuard edits can desynchronize them.
Deleting a claim removes all three regions. Changes call WorldGuard's saveChanges;
failed saves roll back the in-memory mutation and report an error.

Run `./gradlew :regions-v2:test` for tests against WorldGuard's real flag calculator,
including all eight independent role combinations for every supported role flag.
Tests also cover cross-world limits, unlimited/zero settings, membership limits and
rectangle merge validation and diamond pricing/expansion/merge credits. `ClaimOccupancy.claimOf(player)` and
`ClaimOccupancy.playersIn(claimUuid)` provide a main-thread live-location API.

## TODOs

- [x] Update GUI Design to fit to other gui layouts and better overview
- [x] Add default flag settings (admin defaults and owner reset)
- [x] Rename command to 'region' and 'rg' as permission-routed alias
- [x] Remove GUI pane animation in favor of static borders
- [x] Add useful extras: price quotes, occupancy summaries, defaults reset and persistent toolbar
- [x] Add size-based diamond currency for creation, resize and merge
- [x] Cleanup and document configuration and pricing

Possible follow-ups: searchable claim/flag lists, per-world price overrides and an
optional economy-provider integration.

### GUI icons

Region-specific textures live in `gui/RegionHeads.kt`; shared navigation textures
come from the runtime GUI module's `Heads` catalogue. Individual members use player skins.

| Role | Icon |
|------|------|
| Region / My Regions | Map |
| Create / Add | `Heads.PLUS_CHARACTER` |
| Current region | Spyglass |
| Information / Basic Info | `Heads.I_CHARACTER` |
| Location / boundary highlight | Paper |
| Members tab | Supplied Members texture |
| Flags tab / flag policies | Supplied Flags texture |
| Global flag group | Supplied Global texture |
| Owner / Member / Visitor groups and role selectors | Supplied role textures |
| Settings / server configuration | `Heads.TOOLBOX` |
| Rename | Name tag |
| Resize | Cartography table |
| Merge | Book and quill |
| Reset / unset default | Structure void |
| Delete | Barrier |
| Messages | `Heads.ENVELOPE`, admins only (GUI and command) |
| Currency / limits | `Heads.CHART` (retained; no replacement specified) |
| Position 1 / Position 2 | `Heads.NUMBER_1` / `Heads.NUMBER_2` |
| Back / Previous / Next | Shared `Heads.PREVIOUS_PAGE` / `Heads.NEXT_PAGE` |
| Confirm / Enabled / Allow | `Heads.CHECK_MARK` |
| Cancel / Disabled / Deny | `Heads.X_CHARACTER` |
| Page indicator | `Heads.I_CHARACTER`, only for multiple pages |
| Text input | Anvil |
