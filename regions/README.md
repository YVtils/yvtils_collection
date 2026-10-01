# Regions

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

`/regions` is an alias. `/rg` routes execution and tab completion
according to the sender's current permissions: players granted `worldguard.*`,
`worldguard.region.*`, or an effective `worldguard.region.<...>` command permission use
WorldGuard; other players use YVtils. Console uses WorldGuard. WorldGuard protection
bypass permissions alone do not select its commands. The YVtils
`yvtils.regions.worldguard` query permission does not select WorldGuard routing.
Use `/worldguard:region` to address WorldGuard directly and `/regions` to address
YVtils directly. WorldGuard still checks permission for each requested operation.

The Create Claim menu sets either corner at your current position, previews the selection,
accepts a name and asks for creation confirmation. Walk to each corner and reopen the menu
to select it. Commands remain available. Menus use custom-textured player heads.
Its fixed 3×9 layout has back at row 1/column 1; corner heads at row 2/columns 2–3;
name tag at row 2/column 5; spyglass preview at row 2/column 8; confirm/cancel heads
at row 3/columns 4 and 6. Name is a separate prefilled editor and remains in the
selection draft while setting corners or previewing. Back preserves the draft;
Cancel clears both corners/name and returns to the main menu. Confirmation shows
geometry and diamond cost before creation.

- **Main menu (3×9):** four spaced heads in the middle row for My Regions, Create,
  server settings (admins only), and the current region (closes the menu and sends a
  compact chat summary, or feedback that no region exists at the current location).
- **My Regions (5×9):** back in the top-left corner; 21 region heads per page in
  columns 2–8 of the three interior rows; previous/page indicator/next centered in
  the bottom row. Claims are sorted with the current world first, and show dimensions,
  viewer role. Admins also start with only their owned/member claims. A top-right **Other players' regions** filter
  opens a separate admin-only page for unrelated
  claims; switching back restores the personal list. Returning from another player's
  claim keeps the admin in that separate view. An empty personal list shows an explicit
  plus-head **Create a claim** action.
- **Region management (5×9):** persistent top-row tabs for Information, Members,
  Flags and Settings, with back in the top-left. Information opens first; the selected
  tab is marked in its lore. Information uses just two summary heads: Basic Info (owners and creation date in
  `dd.MM.yyyy HH:mm zone` format), and Location (world, dimensions and corners; click to highlight the boundary).
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

Permissions are checked in the operation services as well as commands/menus, so a
hidden GUI action cannot be bypassed through a command. Ownership is still required
for mutations unless `admin.others` is granted. All nodes below use the prefix
`yvtils.regions.`; explicit negative action permissions are respected.

| Suffix                                                            | Purpose                                                                  | Default   |
|-------------------------------------------------------------------|--------------------------------------------------------------------------|-----------|
| `claim`                                                           | Create a claim                                                           | Everyone  |
| `manage`                                                          | Base command/management access                                           | Everyone  |
| `claims.select`, `claims.preview`                                 | Select corners / highlight boundaries                                    | Everyone  |
| `claims.info`, `claims.list`                                      | Read information / browse personal claims or public command list         | Everyone  |
| `claims.delete`, `claims.rename`, `claims.resize`, `claims.merge` | Independent claim mutations                                              | Everyone  |
| `flags.view`, `flags.edit`, `flags.reset`                         | Flag overview / editing / full defaults reset                            | Everyone  |
| `members.view`, `members.edit`, `members.owners`                  | Member list / member changes / ownership changes                         | Everyone  |
| `subzones.view`, `subzones.create`, `subzones.delete`             | Subzone browsing / creation / deletion                                   | Everyone  |
| `subzones.flags`, `subzones.open`                                 | Local overrides / Open Protection                                        | Everyone  |
| `admin.others`                                                    | Browse and manage other players' claims; no cost/limit bypass            | Operators |
| `admin.config`, `admin.policies`                                  | Server settings / flag-policy editor                                     | Operators |
| `admin.create`                                                    | Create for another owner/world, including console createat               | Operators |
| `admin.messages`                                                  | Edit transition messages (still needs claim ownership or `admin.others`) | Operators |
| `bypass.cost`, `bypass.limits`, `bypass.survival`                 | Independent price, count-limit and game-mode exemptions                  | Operators |
| `worldguard`                                                      | Native WorldGuard location query                                         | Everyone  |
| `admin`                                                           | Backward-compatible bundle granting all action/staff/bypass nodes        | Operators |

Grouped wildcards: `claims.*`, `flags.*`, `members.*`, `subzones.*`, `admin.*`,
`bypass.*`, plus the full `yvtils.regions.*`. The shared permission registrar maps
non-public defaults to Bukkit **OP**, not universally denied. Geometry, overlaps,
world borders and server flag policies cannot be bypassed through these nodes.

Example LuckPerms restrictions/delegation:

```text
/lp group default permission set yvtils.regions.members.owners false
/lp group default permission set yvtils.regions.subzones.open false
/lp group moderator permission set yvtils.regions.admin.others true
/lp group moderator permission set yvtils.regions.claims.delete false
/lp group builder permission set yvtils.regions.bypass.cost true
```

Granting `admin.others` does not automatically grant config access or price exemptions.
Revoking `claim` stops creation; revoking `manage` stops management/service mutations.

## Config

`plugins/YVtils/regions/config.yml`, editable in-game via `/yvtils config`:

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
| `clusterPricingEnabled`   | boolean     | `true`               | Price connected nearby claims sharing owners together                               |
| `clusterDistanceBlocks`   | integer     | `16`                 | Maximum empty block gap on both X/Z axes; 0 connects touching footprints            |
| `freeClaimChunks`         | integer     | `2`                  | Free maximum footprint side, in 16-block chunks                                     |
| `diamondsPerChunk`        | integer     | `1`                  | Diamonds per additional longest-side chunk tier                                     |
| `maxClaimVolume`          | long        | `30000000`           | Maximum full-height block volume; existing configured limits are retained           |
| `maxClaimSide`            | integer     | `256`                | Maximum inclusive horizontal length on X/Z                                          |
| `survivalOnly`            | boolean     | `true`               | Survival required for creation (admins exempt)                                      |
| `disabledWorlds`          | string list | `[]`                 | Worlds where creation is disabled                                                   |
| `enabledRoleFlags`        | string list | see generated config | Legacy enabled list, superseded by per-flag policy                                  |
| `enabledGlobalFlags`      | string list | see generated config | Legacy enabled list, superseded by per-flag policy                                  |
| `flagPolicies`            | map         | `{}`                 | Admin-menu policies: editing enabled, role/global scope, and defaults               |
| `maxSubzonesPerClaim`     | integer     | `10`                 | Maximum independent 3D subzones per claim (-1 unlimited)                            |
| `allowOpenSubzones`       | boolean     | `true`               | Allow owners to enable Open Protection inside subzones                              |

`/region admin` → WorldGuard flag policies lists every flag in the live registry,
including custom flags from other plugins. Open a flag to configure owner editing,
global/per-role scope, and server defaults (one global default or separate defaults
for Owner, Member and Visitor). State and boolean defaults are toggles; other types
use WorldGuard's native input parser. Enter `unset` to remove a value; state/boolean
defaults also have explicit unset buttons. Defaults are stored using WorldGuard's
marshal/unmarshal format so location, set and map values retain their types.
The policy list also summarizes each role's current server default. Owners can use **Restore server flag defaults** in a
claim menu, with confirmation, to reset all
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

### 3D subzones

The redstone **3D subzones** item is at **row 3, column 9** (zero-based slot 26)
on claim management pages. Owners/admins can create, preview, edit and delete local
cuboids. Select two X/Y/Z corners at your current position inside the parent claim,
then name and confirm the subzone. This selection is independent of pos1/pos2 for
full-height claims. It survives menu navigation and is cleared on disconnect.
Subzones must fit entirely inside the claim, including Y, and cannot overlap each other.
They are free and do not affect claim counts, cluster pricing or footprint size.

Normal subzones inherit the parent's current role/global flags and domains. Owners
may override only admin-enabled editable flags using the existing role-group editor.
Missing overrides show **Inherited from parent claim**; right-click reset removes the
override and restores inheritance. Parent flag/role changes rebuild subzone policies.
Server flag editing/scope policies still apply to local overrides.

**Open Protection** is a confirmed opt-in mode that writes high-priority ALLOW values
for live-registry StateFlags plus explicit BUILD/PASSTHROUGH allowances for all roles.
This permits **everyone**, including visitors, to build and perform state-controlled
actions inside that cuboid. It is not a complete WorldGuard exclusion: boolean/text/
numeric/custom flag semantics, plugin-wide settings and cross-boundary checks can
still apply. Include the entire machine and its moving/output blocks in the subzone.
Disabling the mode restores inherited flags and saved local overrides; overrides
cannot be edited while open mode is active. Setting `allowOpenSubzones: false`
removes active open allowances in loaded worlds, and other worlds on load; saved
mode preferences remain but have no effect until allowed again.

Parent deletion removes all subzone policy regions. Parent resize is rejected if it
would leave a subzone outside; delete/recreate that subzone first. Merge transfers
both claims' subzones to the kept claim, subject to the subzone-count limit.
Subzone metadata/overrides are persisted in each parent record's `subzones` list in
`claims.json`. WorldGuard stores three higher-priority cuboids per subzone named
`yv2_<claimUuid>__zone_<subzoneUuid>` plus `__owner`/`__member`. These are excluded
from claim discovery/counts/occupancy and rebuilt on startup/world load. Saves roll
back policy replacements if WorldGuard or metadata persistence fails.

### Diamond pricing

Pricing uses `max(0, ceil(max(width, depth) / 16) - freeClaimChunks) * diamondsPerChunk`.
With cluster pricing enabled, width/depth describe the bounding rectangle of the
prospective connected cluster, rather than just the changed claim. Claims connect
when they are in the same world, share at least one owner UUID, and the empty block
gap is at most `clusterDistanceBlocks` on each axis. Touching edges/corners have zero
gap; diagonal neighbours can connect. Connections are transitive, including through
co-owned claims. Unrelated owners, other worlds and distant claims stay separate.
The rectangle includes gaps and empty space between claims, but changes no protection
geometry: names, flags, members and WorldGuard regions remain independent.

For example, creating a free 32×32 claim beside another free 32×32 claim produces a
64×32 cluster costing 2 diamonds. A third adjacent 32×32 claim produces 96×32 costing
4 total, so only 2 more diamonds are due. A 16-block gap is included in cluster size;
a 17-block gap keeps the claims separate with the default configuration.
Set `clusterPricingEnabled: false` to restore standalone per-claim pricing.
The distance must be non-negative. Existing claims are not billed on startup:
checks occur on creation, resize (including moves), merge and owner additions.
Removing an owner or deleting a claim cannot join clusters and never charges/refunds.
Dimensions are inclusive block lengths, independent of actual chunk boundaries and
world height. With defaults, up to 32 × 32 blocks is free, 48 × 48 costs 1 diamond,
64 × 64 costs 2, and 80 × 80 costs 3. Rectangular claims use their longer side.
Both settings must be non-negative; setting `currencyEnabled: false` disables charges.
They are editable through server controls or the shared config editor.
Server controls use the same `DataClassConfigGui` as other modules: left-click toggles
booleans; left/right clicks adjust numbers by ±1 and shift-clicks by ±10; disabled
worlds use the shared add/remove string-list editor. Changes save when the editor
closes. Editing uses a detached config snapshot, with validation and admin permission
checks before committing; invalid changes leave the live settings untouched.

Player flag editing uses the region module's 5×9 head-based editor, with top-left
back and centered bottom pagination. State/boolean flags use check/cross heads;
other flags use the custom flag texture. Lore shows current values and controls.
WorldGuard permissions/parsing still apply, and changes save immediately.

Claim state/boolean flags and admin policy/default toggles use the shared
`ToggleControl` used by both config GUI implementations. Left-click toggles and
refreshes the item in place; right-click unsets claim flags or server defaults.
Changing policy scope rebuilds the policy screen to show the correct role groups.
Other flag types continue to use WorldGuard's native parser through text input.
Anvil inputs show paper in the first slot, named with the current editable value;
rename, message and flag editors prefill their existing values. New values start blank.
Shared data-class and entry-based config editors use the same paper input behavior.
Menu items use the shared secondary-color titles and tertiary-color lore; confirmation
actions are green and cancellation/destructive actions red. Input values are literal
text, so formatting-like characters are not interpreted as MiniMessage tags.

Create, resize, merge and owner-promotion confirmation menus show the payable amount. Commands apply
the same pricing; `/region cost` provides a selection quote. Payment uses diamonds in
the player's main inventory/hotbar, excluding armor and offhand. A failed save restores
the inventory snapshot along with the region mutation. Admin and console operations
are free. Shrinking/deleting gives no refund; previously credited size remains available
on that claim for re-expansion. The player performing an owner addition pays if that
addition connects claims into a more expensive cluster; the recipient is not billed.
Credits are summed once per claim in the prospective cluster. Only the uncovered
deficit is added to the changed claim's stored credit; neighbour credits stay untouched.
Moving/removing a claim therefore cannot copy the cluster's credit onto its neighbours.
Merging transfers both source credits to the kept claim and adds any uncovered deficit.

Existing claims without a stored credit are grandfathered at their current footprint
price (not the whole cluster). Successful creation/resize/merge/role updates store
the credit in `claims.json`; credits
remain diamond amounts if the server later changes tier prices. Free/admin operations
also credit the uncovered cluster deficit so a later owner is not charged retroactively.
Existing multi-claim clusters with insufficient combined credit pay the missing
difference the next time a cluster-growing/owner-adding operation is performed.

The module is statically bundled into core's main plugin JAR and remains selectable
through `modules.yml`. Core declares an optional `WorldGuard` server dependency with
`load: BEFORE` and `join-classpath: true`. The module skips activation when WorldGuard
is absent or disabled.

Use ordinary WorldGuard/WorldEdit imports in `commands`, `logic`, and `listeners`.
There is no custom classloader or reflective API bridge. `logic/Regions.kt` contains
reusable helpers using the live WorldGuard API. Keep WorldGuard-dependent operations
behind the enable-time dependency check, and do not bundle a second copy of its API.

Geometry, protection flags and domains are stored by WorldGuard. Display metadata is
stored in `/regions/claims.json`, indexed by UUID, with creation time, world UUID,
display name, messages and domain snapshots for limits in unloaded worlds. No import
of legacy regions claims/configuration is provided.
When upgrading from the versioned module, move its `config.yml` and `claims.json`
into the `regions` data directory, update its `modules.yml` entry to `regions`,
and update permission grants and custom translation keys to the `regions` namespace.
Archive any configuration from the removed legacy module before moving these files.
WorldGuard's existing `yv2_` region IDs remain stable so existing claims stay discoverable.
Earlier name-keyed WorldGuard-backed cuboids are re-keyed to UUIDs when their world loads;
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

Run `./gradlew :regions:test` for tests against WorldGuard's real flag calculator,
including all eight independent role combinations for every supported role flag.
Tests also cover cross-world limits, unlimited/zero settings, membership limits and
rectangle merge validation, diamond pricing and cluster connectivity/credit accounting.
`ClaimOccupancy.claimOf(player)` and
`ClaimOccupancy.playersIn(claimUuid)` provide a main-thread live-location API.

### GUI icons

Region-specific textures live in `gui/RegionHeads.kt`; shared navigation textures
come from the runtime GUI module's `Heads` catalogue. Individual members use player skins.

| Role                                               | Icon                                                       |
|----------------------------------------------------|------------------------------------------------------------|
| Region / My Regions                                | Filled map                                                 |
| Create / Add                                       | `Heads.PLUS_CHARACTER`                                     |
| Current region                                     | Spyglass                                                   |
| Information / Basic Info                           | `Heads.I_CHARACTER`                                        |
| Location / boundary highlight                      | Paper                                                      |
| Members tab                                        | Supplied Members texture                                   |
| Flags tab / flag policies                          | Supplied Flags texture                                     |
| Global flag group                                  | Supplied Global texture                                    |
| Owner / Member / Visitor groups and role selectors | Supplied role textures                                     |
| Settings / server configuration                    | `Heads.TOOLBOX`                                            |
| Rename                                             | Name tag                                                   |
| Resize                                             | Cartography table                                          |
| Merge                                              | Book and quill                                             |
| Reset / unset default                              | Structure void                                             |
| Delete                                             | Barrier                                                    |
| Messages                                           | `Heads.ENVELOPE`, admins only (GUI and command)            |
| Currency / limits                                  | Shared data-class config editor icons                      |
| Position 1 / Position 2                            | `Heads.NUMBER_1` / `Heads.NUMBER_2`                        |
| Back / Previous / Next                             | Shared `Heads.PREVIOUS_PAGE` / `Heads.NEXT_PAGE`           |
| Confirm / Enabled / Allow                          | `Heads.CHECK_MARK`                                         |
| Cancel / Disabled / Deny                           | `Heads.X_CHARACTER`                                        |
| Page indicator                                     | `Heads.I_CHARACTER`, only for multiple pages               |
| Text input                                         | Paper named with the current value in the first anvil slot |
