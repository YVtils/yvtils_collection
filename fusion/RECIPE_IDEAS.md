# Future Fusion recipes and improvements

These are design proposals, not installed recipes. Costs are starting points for
playtesting. **Data** means the current recipe schema can produce the item;
**Logic** means additional gameplay code is needed; **Pack** means a resource pack
would improve its appearance. Prefer sidegrades, building tools and cooperation
over replacing vanilla progression.

The Nourishing Flask is now implemented separately: manual XP-point feeding with
no automatic spending. Craft hooks/output/interaction registration are also available
in [API.md](API.md); automatic feeding, catalyst handling and other proposals below
remain future options.

## Decoration and building

| # | Idea | Suggested ingredients | Behavior / implementation |
|---|---|---|---|
| 1 | Invisible glow frame | Glow frame + glass | Data: use INVISIBLE_FRAME with GLOW_ITEM_FRAME. |
| 2 | Builder's light kit | Glass + glowstone | Data: economical multi-light output. |
| 3 | Head collection tokens | Paper + dye + copper | Data: named custom tokens for exchanges. |
| 4 | Biome decoration bundles | Leaves + flowers + wood | Logic: output bundles containing themed blocks. |
| 5 | Moss conversion recipe | Cobblestone + moss | Data: mossy building materials. |
| 6 | Cracked brick forging | Stone bricks + charcoal | Data: alternative cracked-brick production. |
| 7 | Weathered copper accelerator | Copper + honeycomb + water ingredient | Logic: catalyst/container handling. |
| 8 | Ornamented lantern | Lantern + amethyst | Data/Pack: custom decorative item; placement logic for appearance. |
| 9 | Seasonal wreath | Leaves + berries + string | Data/Pack: collectible decoration. |
| 10 | Display pedestal | Stone + slab + frame | Logic: place a compact item display. |
| 11 | Custom banner commission | Banner + dyes + paper | Data: capture a patterned banner as output. |
| 12 | Written signage kit | Signs + ink + paper | Logic: template text before crafting. |
| 13 | Decorative map prints | Map + paper + dye | Logic: retain source map ID and charge per copy. |
| 14 | Faux treasure pile | Nuggets + quartz | Data/Pack: decoration without mining value. |
| 15 | Miniature monument | Stone + lapis + gold | Data/Pack: server landmark collectible. |

## Everyday utility

| # | Idea | Suggested ingredients | Behavior / implementation |
|---|---|---|---|
| 16 | Repair patch | Leather + iron nuggets | Logic: small durability repair, no enchantment changes. |
| 17 | Tool maintenance kit | Copper + string + leather | Logic: limited charges, repair only outside combat. |
| 18 | Reusable torch lighter | Flint + iron + coal | Logic: durability-consuming ignition tool. |
| 19 | Builder's measuring tape | String + copper + paper | Logic: show distance/selection dimensions without claiming land. |
| 20 | Empty bottle recycling | Glass bottles | Data: recover a modest amount of glass. |
| 21 | Wool recycling | Wool + shears catalyst | Logic: reversible conversion with loss and tool wear. |
| 22 | Saddle manufacture | Leather + iron + string | Data: configurable access to vanilla item. |
| 23 | Name-tag manufacture | Paper + string + iron | Data: accessible cosmetic naming. |
| 24 | Bell forging | Gold + iron + stone | Data: expensive village-themed craft. |
| 25 | Builder's bundle | Leather + string | Data: alternative bundle recipe. |
| 26 | Safe fireworks pack | Paper + gunpowder | Data: captured flight rockets without explosion payload. |
| 27 | Compass engraving | Compass + name tag | Logic: custom name input, no teleportation. |
| 28 | Portable crafting token | Crafting table + copper | Logic: open crafting interface, cooldown/permission. |
| 29 | Inventory label stamp | Ink + paper + stick | Logic: name owned items in bulk with durability cost. |
| 30 | Experience bottle bottling | Bottles + XP | Data: XP-priced output; measure exchange rates to prevent gain loops. |

## Farming, food and nature

| # | Idea | Suggested ingredients | Behavior / implementation |
|---|---|---|---|
| 31 | Farmer's seed mix | Several seeds + paper | Logic: randomized seed bundle with transparent odds. |
| 32 | Compost pellet | Leaves + rotten flesh + seeds | Data: bonemeal at balanced conversion rate. |
| 33 | Picnic ration | Bread + cooked meat + berries | Data/Pack: named food; additional effects need Logic. |
| 34 | Honey candy | Honey + sugar | Logic: modest hunger item, correctly return bottles. |
| 35 | Orchard sapling kit | Saplings + bonemeal | Data: alternative ingredient sapling output. |
| 36 | Mushroom grower's pack | Mushrooms + dirt | Logic: bundle output. |
| 37 | Bee-friendly lantern | Lantern + honeycomb | Pack/Logic: decorative beehive-area marker. |
| 38 | Watering can | Copper + bucket | Logic: limited moisture restoration, claim-aware. |
| 39 | Harvest basket | Bundle + wheat + sticks | Logic: collect nearby crop drops, respect pickup permissions. |
| 40 | Flower pressing | Flowers + paper | Data/Pack: collectible pressed flowers. |

## Exploration and adventure

| # | Idea | Suggested ingredients | Behavior / implementation |
|---|---|---|---|
| 41 | Explorer's ration | Food + dried kelp | Logic: small timed exploration buff, no combat dominance. |
| 42 | Cave chalk | Calcite + dye | Logic: temporary wall markers with cleanup and claim checks. |
| 43 | Glow breadcrumb | Glow berries + paper | Logic: short-lived visible trail, no teleport/navigation system required. |
| 44 | Rope ladder kit | String + sticks | Logic: temporary ladders, block rollback and ownership. |
| 45 | Rescue flare | Firework + red dye | Logic: announce location to nearby teammates, cooldown. |
| 46 | Expedition journal | Book + compass | Logic: record visited biomes and discoveries. |
| 47 | Archaeology pouch | Brush + leather | Logic: collectible archaeology storage/filtering. |
| 48 | Cave survey lens | Spyglass + amethyst | Logic: inspect block/material names at range, no ore wallhack. |
| 49 | Cold-weather cloak | Leather + wool | Logic: limited powder-snow protection, not armor replacement. |
| 50 | Nether travel ration | Bread + magma cream | Logic: short mild environmental assistance; high cost. |

## Social and SMP collectibles

| # | Idea | Suggested ingredients | Behavior / implementation |
|---|---|---|---|
| 51 | Friendship medallion | Copper + player head | Logic/Pack: two named linked keepsakes. |
| 52 | Team banner kit | Banner + dyes | Data: captured team banner variants. |
| 53 | Event participation badge | Paper + rare event token | Data/Pack: persistent commemorative item. |
| 54 | Dragon victory trophy | Dragon breath + end stone + gold | Data/Pack: unlock via event permission/token. |
| 55 | Founder's plaque | Copper + book + name tag | Logic/Pack: owner and creation date. |
| 56 | Birthday gift box | Chest + cake + paper | Logic: gift packaging without item duplication. |
| 57 | Signed trading receipt | Paper + ink | Logic: record explicit trades and participants. |
| 58 | Festival lantern | Lantern + dye + paper | Pack/Logic: seasonal cosmetics. |
| 59 | Community project token | Bulk building materials | Data: receipt item for project donations. |
| 60 | Museum specimen label | Paper + item sample | Logic: item information/collector attribution. |

## Materials and intermediate components

| # | Idea | Suggested ingredients | Behavior / implementation |
|---|---|---|---|
| 61 | Copper gear | Copper nuggets/ingots | Data/Pack: @ID ingredient for later utility recipes. |
| 62 | Reinforced fabric | Leather + wool + string | Data: component for bags/kits. |
| 63 | Arcane glass | Glass + amethyst + lapis | Data/Pack: light/decorative recipe component. |
| 64 | Binding seal | Paper + wax/honeycomb | Data: recipe-chain ingredient. |
| 65 | Polished gemstone | Amethyst + quartz + XP | Data/Pack: expensive cosmetic intermediate. |
| 66 | Charcoal briquette | Charcoal + dried kelp | Data: fuel output; compare furnace efficiency. |
| 67 | Metal scrap recycling | Damaged iron equipment | Logic: durability-sensitive yields, no infinite conversion. |
| 68 | Enchantment residue | Enchanted book + bottle | Logic: controlled salvage, no duplicating enchants. |
| 69 | Prismarine dye kit | Prismarine + dyes | Data/Pack: ocean-themed components. |
| 70 | Ender filament | Chorus fruit + string + pearl | Data/Pack: late-game component for cosmetic recipes. |

## Future crafting-system features

1. **Catalysts:** required non-consumed ingredients with optional durability wear.
2. **Container returns:** empty bottles/buckets returned in the same inventory transaction.
3. **Multiple outputs:** coordinated products/byproducts with exact capacity checks.
4. **Recipe favorites:** persistent personal shortcuts and recently crafted recipes.
5. **Craftable-only filter:** include both output space and XP in readiness calculation.
6. **Ingredient GUI editor:** reorder ingredients, alternatives, selectors and quantities visually.
7. **Output metadata editor:** edit lore/model data/enchantments without holding a prepared item.
8. **Named recipe packs:** validated import/export with collision previews and atomic merge.
9. **Recipe version history:** backups, diffs and restore through admin GUI.
10. **Unlock conditions:** advancements, discoveries, quests or explicitly granted permissions.
11. **Catalyst recipe chains:** intermediates identified by PDC, never display name alone.
12. **Crafting stations:** optionally restrict recipes to world/area/workstation, respect protection.
13. **Recipe preview command:** inspect requirements from chat/console without crafting.
14. **Cooperative crafting:** participants explicitly contribute ingredients, with cancellation refunds.
15. **Optional cooldowns:** bounded repeated utility crafting, persisted where necessary.
16. **Per-player crafting quotas:** event recipes or seasonal limits, with visible remaining counts.
17. **Custom craft events/API:** integrations can validate/cancel operations before any mutation.
18. **Metrics:** aggregated recipe usage/failure reasons via the existing stats module.
19. **Accessibility:** short lore, keyboard-friendly search, readable categories and no color-only cues.
20. **Localization of recipe content:** English/German names/descriptions separate from immutable IDs.

## Recommended next batches

- **Low-complexity decoration:** invisible glow frames, banners, building conversions, event badges.
- **Crafting UX:** ingredient editor, favorites, craftable-only filtering, import previews.
- **Core extensions:** catalysts/container returns/multiple outputs before kits or recycling.
- **Gameplay utilities:** one or two claim-aware, durability-limited tools with targeted tests.

For each accepted recipe, decide ingredients, output, metadata matching, permissions,
return items, bulk behavior, inventory-space handling, interactions with protection,
and whether its output can be recycled. Check the full conversion graph for profit loops.
