m# Extending Fusion

## Extension points

`yv.tils.fusion.api.FusionApi` is registered with Bukkit's `ServicesManager` during
Fusion `onLoad()`, after built-in output types. You can register extensions before
Fusion loads recipes in `enablePlugin()`. The browser and public crafting API use
the same registry and transaction path.

| API                                   | Purpose                                                   |
|---------------------------------------|-----------------------------------------------------------|
| `registerOutputType(id, handler)`     | Validate a recipe and create/preview its output           |
| `registerBehavior(kind, handler)`     | Dispatch right-click behavior for persistent custom items |
| `registerRecipe(recipe)`              | Add a runtime recipe without changing administrator JSON  |
| `registerCraftGuard(handler)`         | Reject a craft before ingredients/XP are committed        |
| `registerCraftObserver(handler)`      | Observe successful crafts for achievements/logging        |
| `recipes()` / `recipe(id)`            | Read currently available file and extension recipes       |
| `itemId(item)` / `itemKind(item)`     | Identify items by PDC, independent of display names       |
| `createOutput(recipe, selection)`     | Generate a validated item carrying stable IDs             |
| `craft(player, id, count, selection)` | Craft by recipe ID using the normal transaction           |

All registry/recipe/crafting calls run on the **Paper server thread**. `craft` count
0 means maximum, up to 64. Extensions must not mutate inventory, hunger, XP or
world state during output validation/creation/preview or craft guards. Observers
run after commit; their errors are logged and cannot roll back completed crafts.
Output stacks passed to guards/observers are clones.

Registrations return `AutoCloseable`. Keep the handles and close them on your
module/plugin disable, in reverse registration order (recipe, behavior, type).
Duplicate IDs are rejected. Runtime recipes are visible to players but excluded
from the administrator editor; the extension owns their lifecycle. JSON reload
rejects recipe-ID collisions with extensions. Use a project prefix for recipe
IDs (`example-token`) and namespaced output kinds (`example:token`).

Register custom output kinds **before** recipe load if JSON references them.
When Fusion shuts down the service/registry is cleared. Do not reuse cached
service/registration handles across a Fusion lifecycle restart.

## Classloader integration

For collection feature modules, add `compileOnly(project(":fusion"))`, wire your
module through the standard loader, and arrange lifecycle order after Fusion's
`onLoad()` but before recipe `enablePlugin()` when contributing JSON output kinds.
Built-ins (`logic/items/BuiltinOutputTypes`) are a compact example of the same API in use.

For a separately deployed Paper plugin, compile against the published Fusion
artifact, declare an explicit dependency on the launcher (`YVtils`) with
`join-classpath: true`, and obtain the service through Bukkit. The API must resolve
from the **same runtime classloader** as Fusion. Do not shade Fusion, Kotlin,
CommandAPI or shared YVtils runtime into your plugin. Startup ordering/classloader
integration for custom JSON kinds is your responsibility; register then explicitly
reload recipes if your addon starts after Fusion. Runtime-only recipes can be
added after enable. An addon must check that the service is present before use.

## Example: custom type, behavior and recipe

```kotlin
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.event.block.Action
import yv.tils.fusion.FusionRecipe
import yv.tils.fusion.Ingredient
import yv.tils.fusion.api.*

val api = Bukkit.getServicesManager().load(FusionApi::class.java)
    ?: error("Enable the Fusion module first")
val handles = mutableListOf<AutoCloseable>()

handles += api.registerOutputType("example:greeting", object : FusionOutputType {
    override fun validate(recipe: FusionRecipe) {
        require(recipe.output == "PAPER")
    }
    override fun create(recipe: FusionRecipe, base: ItemStack, selection: OutputSelection): ItemStack {
        // Base already has name, quantity and identity. Customize its metadata here.
        return base
    }
})
handles += api.registerBehavior("example:greeting") { event, _ ->
    if (event.action == Action.RIGHT_CLICK_AIR && event.player.hasPermission("example.greeting")) {
        event.player.sendMessage("Hello from a custom Fusion item!")
    }
}
handles += api.registerRecipe(FusionRecipe(
    id = "example-greeting",
    name = "Greeting card",
    ingredients = listOf(Ingredient(amount = 2, materials = listOf("PAPER"))),
    output = "PAPER",
    outputKind = "example:greeting"
))

// onDisable, on the server thread:
handles.asReversed().forEach { it.close() }
```

For new JSON recipes using built-in types, no Kotlin is needed: add
`recipes/<recipe-id>.json` and run `/fusion reload`. Each file contains one recipe
with `schemaVersion: 1` and an ID matching its filename. Addon recipes are not
persisted implicitly.

## Output options and custom inputs

`FusionOutputType` supports `ready`, `preview`, `control` and `configure`:

- Return a control ItemStack to add a button to recipe details.
- `configure` can cycle settings or open your own anvil/inventory interface.
- Supply updated `OutputSelection` through the provided Java `Consumer` on the
  server thread. Use `selection.values` for custom choices.
- `ready` prevents crafting before required choices are complete.
- `create` must still validate choices; never trust text from a client/UI.
- `recipe.settings` stores per-recipe string settings with bounded size.

Light-level selection and asynchronous head resolution use this design. A custom
output type does not need a new switch in `FusionItems`, `FusionGui` or the editor.

## Craft guards and observers

```kotlin
handles += api.registerCraftGuard { context ->
    if (context.player.world.name == "event_world") "Crafting is disabled in this event world"
    else null
}
handles += api.registerCraftObserver { context ->
    logger.info("${context.player.name} crafted ${context.crafts} ${context.recipe.id}")
}
```

Guard exceptions fail the craft before mutation. After guards, recipe permissions,
inventory space, ingredients and XP are validated again. Observers should avoid
recursively crafting; the per-player transaction lock remains held through dispatch.
Normal Minecraft item interactions belong in behavior handlers or dedicated Paper
listeners. Placement/physics/drop features may use dedicated listeners as the
invisible-frame implementation does. Respect existing event cancellation and keep
all entity/block mutations on the server thread.

## Testing new features

- Test overlapping ingredients and exact custom-item identity.
- Test full inventories, insufficient XP and repeated same-tick clicks.
- Revoke permissions/reload/remove a runtime recipe while its GUI is open.
- Check handle cleanup and service disappearance on disable.
- For interactions, test both hands, cancelled events, Creative/Adventure,
  player disconnects and server restarts.
- Keep pure balancing/allocation rules independent of Bukkit to unit-test them.
