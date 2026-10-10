# Fusion crafting

## Source layout

The module follows the same separation as Moderation and the other collection modules:

```text
fusion/
├── FusionYVtils.kt             # lifecycle wiring
├── api/                       # public extension contracts and registry
├── commands/                  # CommandAPI registration
├── configs/                   # recipe persistence, migration and defaults
├── data/                      # recipe models and permission definitions
├── gui/                       # browser, editor and presentation helpers
├── language/                  # translated text registration
├── listeners/                 # Paper event adapters
├── logic/
│   ├── crafting/              # transactions and ingredient allocation
│   └── items/
│       └── flask/             # flask behavior and pure pricing rules
└── utils/                     # tag lookup helpers
```

Tests mirror their gameplay logic packages. Public `FusionRecipe`, `Ingredient`
and `RecipeBook` classes live in `data/` but retain their `yv.tils.fusion` package
to preserve addon compatibility; all API contracts remain in `yv.tils.fusion.api`.

Fusion is a dynamically loaded feature module, enabled with `fusion: true` in
`plugins/yvtils/modules.yml`. Publish Fusion and the matching GUI artifact before
starting the updated launcher. The feature JAR is a Maven library, not a standalone
Paper plugin.

## Commands and permissions

| Command | Purpose | Permission |
|---|---|---|
| `/fusion` (`/fc`, `/ccr`) | Recipe browser | `yvtils.fusion.use` (everyone) |
| `/fusion manage` | Edit existing recipes | `yvtils.fusion.manage` (OP) |
| `/fusion new <id>` | Create a draft | `yvtils.fusion.manage` |
| `/fusion reload` | Strictly validate/reload recipe file; console supported | `yvtils.fusion.manage` |

The module editor is also available through `/yvtils config`.
`yvtils.fusion.*` grants both permissions. Individual recipes can require another
permission; the browser explains the requirement and crafting rechecks it.

## Default recipes

- Invisible item frame: one item frame + four glass blocks of any color.
- Light block: four glowstone dust + one glass block of any color; choose level 0–15.
- Player head: one armor stand + four leather + one name tag; enter a Java name
  or UUID. Skin lookup is asynchronous, with a 15-second timeout and preview.
- Nourishing Flask: one glass bottle + two gold ingots + four wheat + one lapis.
  Its output is a lime-green custom potion; it is reusable, not a drinkable vanilla potion.
  A reusable right-click item spending **8 raw XP points per hunger point**, up
  to 4 hunger points (two drumsticks) per use, then up to 4 saturation at 8 XP points
  per saturation point. One-second cooldown and no automatic feeding. Saturation
  can be restored at full hunger and never exceeds current food level. Partial feeding works
  when XP is insufficient for a full use. Survival/Adventure only; requires
  `yvtils.fusion.flask.use` (everyone by default) and `yvtils.fusion.use`.

The browser supports name/ID/description search, category cycling, ingredient-ready
sorting and exact missing quantities. Click a recipe to view its output and craft
one, a stack, or as many as possible (maximum 64 recipe executions per click).
Bulk crafting checks both inventory space and available XP levels.

Clear glass and all 16 stained-glass block colors can be mixed in the frame/light
recipes (panes and tinted glass are excluded). Existing built-in plain-glass
selectors upgrade on reload; customized tag/exact/alternative selectors are preserved.
Crafted outputs use compact non-italic colored lore with a Fusion/category header,
description, behavior stats and usage hints. Captured NORMAL outputs retain their
original lore. New crafting receives the updated styling; existing items are unchanged.

The six-row browser separates filters from recipe cards and navigation. Recipe
cards use green/red status text and explicit check/cross marks. Recipe details
include a paginated ingredient grid with representative item icons, allocated
available/required counts and missing quantities; overlapping selectors still
count each inventory item once. A requirements summary shows XP, output selection
and inventory-space status. Craft buttons show readiness, quantities and total XP
cost; Refresh updates all counts from the player's current inventory.

The six-row editor groups naming/category/icon options at the top, output/behavior
and ingredient controls below, and save/discard at the bottom. Each setting uses a
distinct icon (name tag, book, bookshelf, frame, key, XP bottle, bundle, chest,
blaze powder, comparator, crafting table). Enabled state toggles directly in the
draft; the output preview remains centered at the top.

Numeric fields use the config-editor controls: left/right click changes by +1/-1,
shift-left/shift-right by +10/-10, with bounds enforced. Middle-click opens exact
number input. Behavior settings have their own menu; numeric values use these
controls and other values use text input. The ingredient editor uses the same
numeric controls for quantities while preserving exact metadata selectors.
Light level selection also supports
left/right and shift-click steps, clamped to 0–15. Legacy flask recipe files using
GLASS_BOTTLE remain valid and produce the new colored potion on their next craft;
already-crafted bottles retain their feeding behavior.

Crafting only consumes storage/hotbar contents, never equipped armor or offhand
items. Plans operate on clones. Overlapping alternative ingredients are allocated
without double counting. Crafting revalidates the recipe and permissions before
committing ingredients/output/XP together. Nothing is dropped to bypass a full
inventory. Repeat clicks in the same server tick are ignored.

## Recipe files

Each recipe has its own formatted JSON file under
`plugins/yvtils/fusion/recipes/`, named after its ID, e.g. `wood-token.json`:

```json
{
  "schemaVersion": 1,
      "id": "wood-token",
      "name": "Wood token",
      "description": "A token made from any logs.",
      "category": "Materials",
      "thumbnail": "OAK_LOG",
      "ingredients": [{ "amount": 8, "tag": "minecraft:logs" }],
      "output": "PAPER",
      "outputAmount": 1,
      "outputKind": "NORMAL",
      "xpLevels": 0,
      "settings": {}
}
```

Selectors support:

- `materials`: alternative uppercase material names (e.g. `[OAK_LOG, BIRCH_LOG]`).
- `tag`: a registered Minecraft block tag, such as `minecraft:logs`.
- `fusionId`: an exact persistent Fusion recipe ID.
- `exactItem`: Base64 serialized item data, generated by the editor, matched with
  `ItemStack.isSimilar` so metadata matters but stack quantity does not.

Material/tag matching does not consume custom Fusion items. One selector type is
allowed per ingredient. Built-in output kinds are `NORMAL`, `INVISIBLE_FRAME`,
`LIGHT`, `PLAYER_HEAD` and `NOURISHING_FLASK`. Extensions register additional
namespaced kinds, so adding behavior does not require editing a central switch.
Special kinds validate their material/settings.

If the `recipes/` directory does not exist, the combined `recipes.json` (or older
`recipes.yml` when combined JSON is absent) is strictly validated and split into
individual files. The original combined file remains as a backup. Migration creates
the complete directory atomically, avoiding partially migrated live recipes.
Once the directory exists, it is authoritative—even when empty. Removing a recipe
file removes that recipe on `/fusion reload`; defaults are not silently recreated.
Individual files require `schemaVersion: 1` and a filename matching their `id`.
Malformed files reject the entire reload without changing live recipes.
Older combined schema 1 retains its one-time flask addition during migration;
combined schema 2 preserves exactly its existing recipe list.

Flask recipe settings are `xpPerFoodPoint` (default 8), `foodPerUse` (4), and
`cooldownTicks` (20). They are copied into crafted item PDC so already-crafted
flasks retain their price. Settings are edited individually: Add asks for a key
and then a value in separate inputs; Remove lists the settings to delete.
Crafting XP prices still use **levels**; the flask's usage price uses **points**.

Saturation settings: `saturationPerUse` (default 4, set 0 to disable) and
`xpPerSaturationPoint` (default 8). Hunger is purchased first, then saturation from
remaining XP. Fractional saturation deficits are charged proportionally, rounded
up to an XP point. Existing flask items missing these keys use the defaults;
newly crafted items store the configured values. Full hunger and saturation spend
no XP. Food-change cancellation prevents both hunger and saturation nourishment.

IDs are stable, lowercase letters/digits/underscores/hyphens (maximum 64 chars).
Recipe limits: 500 recipes, 36 ingredients per recipe, 1–4096 items per ingredient,
1–64 output items, and 0–10000 XP levels per craft. Recipe IDs must be unique.

The editor changes detached drafts: name, description, category, thumbnail,
enabled state, permission, XP price, output amount/material/kind and ingredients.
It previews output/ingredients, captures the held item as an exact output, adds
the held stack as an exact ingredient, and removes the last ingredient.
Cancel/closing discards the draft. Validate-and-save persists atomically; a changed
recipe rejects stale drafts. IDs are immutable when editing existing recipes.

Browser icon, output and ingredient selection use a clickable inventory-item
picker instead of material-name input. Items are copied, never taken from the
player. Browser icons retain selected metadata through `exactThumbnail`; outputs
retain metadata through `exactOutput`. New ingredients offer material-only or
exact-metadata matching. Quantities remain editable with numeric click controls.
Alternative materials, tags and Fusion IDs remain supported in recipe JSON/API.

Copy individual recipe JSON files to share recipes. Reload validates all files
before replacing live data; malformed/unsupported files remain untouched.
There is no automatic import of old standalone SMP Fusion files. Serialized item
bytes are Minecraft-version data, so test captured items when changing server versions.

## Persistent items

Outputs carry `yvtils:fusion_id` and `yvtils:fusion_kind`. Renaming an item does not
change its identity. Invisible frames keep a serialized copy of their original
item in entity PDC; placement hides the frame and breaking restores that original
custom item, even if its recipe has since changed. Existing items keep their ID
if a recipe is disabled/removed. Placement/drop behavior requires Fusion enabled.

## Verification

`./gradlew :fusion:build :essentials:build :core:build`

Pure allocation tests cover alternative ingredient rerouting, no double consumption,
and untouched nonmatching slots. On Paper, also verify:

1. First startup generates all four recipes; restart and reload preserve edits.
2. Search/categories, empty results, multiple pages and missing ingredient counts.
3. Craft one/stack/maximum with full inventory, partial stacks, insufficient XP,
   overlapping alternatives, exact metadata, and Fusion-ID ingredients.
4. Rapid double clicks, permission revocation and recipe reload while details are open.
5. Draft save/cancel, stale drafts, invalid fields and malformed JSON reload;
   migration preserves the original combined file and all existing recipes.
6. Async head preview, failed lookup, disconnect during lookup and selected skin output.
7. Light levels after placement; invisible frame placement/break/restart, displayed
   items (including another frame), explosions, supporting-block removal and Creative mode.
8. WorldGuard/event cancellation prevents forbidden placement/crafting side effects.
9. Flask use at full/partial hunger, insufficient XP, level-boundary spending,
   rapid clicks, both hands, cancelled food change, and permissions. Check no
   bottle consumption/water filling, saturation caps and no automatic XP drain.

See [RECIPE_IDEAS.md](RECIPE_IDEAS.md) for future recipe and system ideas.
See [API.md](API.md) for extension points, lifecycle and working examples.
