# Adding a new feature module

This guide covers creating a **brand new** feature module from scratch (like
`sit`, `status`, `discord`, ...) - the folder/Gradle scaffolding, the code
conventions the rest of the codebase follows, and wiring it into the dynamic
module system.

If you're instead converting an **existing** module or core to the dynamic
module system, see [`migrating-to-dynamic-modules.md`](./migrating-to-dynamic-modules.md)
instead - this guide assumes you're starting from nothing.

## Table of contents

1. [Is a new module the right call?](#is-a-new-module-the-right-call)
2. [Step 1 - Create the Gradle module](#step-1---create-the-gradle-module)
3. [Step 2 - Package layout](#step-2---package-layout)
4. [Step 3 - The entry-point class](#step-3---the-entry-point-class)
5. [Step 4 - Commands](#step-4---commands)
6. [Step 5 - Config files](#step-5---config-files)
7. [Step 6 - Translations](#step-6---translations)
8. [Step 7 - Permissions](#step-7---permissions)
9. [Step 8 - Listeners](#step-8---listeners)
10. [Step 9 - Wire it into the dynamic module system](#step-9---wire-it-into-the-dynamic-module-system)
11. [Step 10 - Test it locally](#step-10---test-it-locally)
12. [Checklist](#checklist)

---

## Is a new module the right call?

A "module" in this repo is a self-contained feature (one thing a server admin
can turn on or off independently - `sit`, `status`, `discord`, `moderation`,
...). If what you're adding is a small addition to an *existing* feature
(e.g. one more `/status` subcommand), add it to that module instead of
creating a new one. Create a new module when the feature is genuinely
independent and someone might reasonably want it without the others.

## Step 1 - Create the Gradle module

Create the folder and a minimal `build.gradle.kts`:

```
your-module/
  build.gradle.kts
  src/main/kotlin/yv/tils/yourmodule/
```

```kotlin
/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

dependencies {
    compileOnly(project(":utils"))
    compileOnly(project(":config"))
    compileOnly(project(":common"))
}
```

`utils`/`config`/`common` are **always `compileOnly`**, never `implementation`
- they're provided at runtime by the shared runtime bundle every core embeds,
not shaded into your module's own jar. See
[the one rule](./migrating-to-dynamic-modules.md#the-one-rule-you-must-understand-first)
in the migration guide for why this matters - getting this wrong is the #1
way to break a module once it's fetched dynamically.

If your module needs another module that's itself published (e.g. `gui`),
that's a real `implementation` dependency instead:

```kotlin
dependencies {
    compileOnly(project(":utils"))
    compileOnly(project(":config"))
    compileOnly(project(":common"))
    implementation(project(":gui"))
}
```

Third-party libraries (JDA for `discord`, etc.) are also normal
`implementation` dependencies.

Register the module in the root `settings.gradle.kts`:

```kotlin
include("your-module")
```

## Step 2 - Package layout

Every module lives under `yv.tils.<moduleName>` (camelCase matching the
Gradle project name, e.g. `yv.tils.multiMine`). Sub-packages follow a loose
but consistent convention across existing modules - only create the ones you
actually need:

| Package | Contains |
|---|---|
| `commands/` | CommandAPI command classes |
| `configs/` | Config file schema + load/save logic |
| `language/` | Registered translation strings |
| `listeners/` | Bukkit event listeners |
| `logic/` | The actual feature logic/handlers |
| `data/` | Data classes, permission definitions, enums |
| `gui/` | Inventory GUI code (if using the `gui` module) |

`sit` is the simplest real example to copy the shape of
(`commands/`, `listeners/`, `logic/`); `status` is a more complete example
that also has `configs/` and `language/`.

## Step 3 - The entry-point class

Every module has exactly one class named `<Name>YVtils` in the module's root
package, implementing `yv.tils.utils.modules.Module.YVtilsModule`. This is
the class a launcher (static or dynamic) instantiates and drives through the
plugin lifecycle:

```kotlin
package yv.tils.yourmodule

import yv.tils.utils.modules.Core
import yv.tils.utils.modules.Module

class YourModuleYVtils : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "your-module",
            "1.0.0",
            "Your module description for YVtils",
            "YVtils",
            "https://docs.yvtils.net/your-module/"
        )
    }

    override fun onLoad() {
        // Register language strings and config schema here (see steps 5-6) -
        // this runs before enablePlugin(), so registrations are ready when
        // configs/language files actually get read or written.
    }

    override fun enablePlugin() {
        Module.addModule(MODULE) // required - this is how the module shows
                                  // up in Module.getModulesString() and the
                                  // intra-jar dependency checks

        registerCommands()
        registerListeners()
        loadConfigs()
    }

    override fun onLateEnablePlugin() {
        // Runs after every module's enablePlugin() has completed - use this
        // if your module needs to react to another module that might not be
        // ready yet during enablePlugin().
    }

    override fun disablePlugin() {
        Module.removeModule(MODULE)
    }

    private fun registerCommands() {}

    private fun registerListeners() {
        val plugin = Core.instance
        val pm = plugin.server.pluginManager
        // pm.registerEvents(YourListener(), plugin)
    }

    private fun loadConfigs() {}
}
```

Requirements for whichever launcher will fetch this module dynamically to be
able to use it:
- Public, no-argument constructor (it's instantiated via
  `Class.forName(...).getDeclaredConstructor().newInstance()`).
- All lifecycle methods have default no-op implementations on the interface -
  only override the ones you actually need.

## Step 4 - Commands

Commands use [CommandAPI's Kotlin DSL](https://commandapi.jorel.dev/), one
class per command tree, instantiated once from `registerCommands()`:

```kotlin
package yv.tils.yourmodule.commands

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor

class YourCommand {
    val command = commandTree("yourcommand") {
        withPermission("yvtils.command.yourcommand")
        withPermission(CommandPermission.NONE)
        withUsage("yourcommand")

        playerExecutor { player, _ ->
            // ...
        }
    }
}
```

> The available DSL functions depend on the exact CommandAPI version this
> repo pins (`commandAPIVersion` in the root `build.gradle.kts`) - not every
> function that existed in an older version still exists (e.g. `playerArgument`
> was replaced by `entitySelectorArgumentOnePlayer` at some point). If
> something doesn't resolve, check what's actually available in the
> `commandapi-kotlin-*` jars rather than assuming an older tutorial's API
> still applies.

## Step 5 - Config files

Config schemas are declared as a list of `ConfigEntry`, written/read through
`yv.tils.config.files.YMLFileUtils`/`FileUtils`:

```kotlin
package yv.tils.yourmodule.configs

import yv.tils.config.data.ConfigEntry
import yv.tils.config.data.EntryType
import yv.tils.config.files.FileUtils
import yv.tils.config.files.YMLFileUtils

class ConfigFile {
    companion object {
        val config: MutableMap<String, Any> = mutableMapOf()
    }

    private val filePath = "/your-module/config.yml"

    fun loadConfig() {
        val file = YMLFileUtils.loadYAMLFile(filePath)
        for (key in file.content.getKeys(true)) {
            config[key] = file.content.get(key) as Any
        }
    }

    fun registerStrings() {
        val entries = mutableListOf(
            ConfigEntry("documentation", EntryType.STRING, null, "https://docs.yvtils.net/your-module/config.yml", "Documentation URL"),
            ConfigEntry("enabled", EntryType.BOOLEAN, null, true, "Whether the feature is enabled"),
        )

        val ymlFile = YMLFileUtils.makeYAMLFileFromEntries(filePath, entries)
        FileUtils.saveFile(filePath, ymlFile)
    }
}
```

Call `ConfigFile().registerStrings()` from `onLoad()` and
`ConfigFile().loadConfig()` from `enablePlugin()` (matching `status`'s
`StatusYVtils.kt`).

## Step 6 - Translations

Register every user-facing string through `BuildLanguage`, per-locale, from
`onLoad()`:

```kotlin
package yv.tils.yourmodule.language

import yv.tils.config.language.BuildLanguage
import yv.tils.config.language.FileTypes

class RegisterStrings {
    fun registerStrings() {
        registerNewString(
            "command.yourmodule.success",
            mapOf(
                FileTypes.EN to "<prefix> <white>Done!",
                FileTypes.DE to "<prefix> <white>Erledigt!",
            )
        )
    }

    private fun registerNewString(langKey: String, translations: Map<FileTypes, String>) {
        translations.forEach { (fileType, value) ->
            BuildLanguage.registerString(BuildLanguage.RegisteredString(fileType, langKey, value))
        }
    }
}
```

Retrieve a registered string at runtime via
`yv.tils.config.language.LanguageHandler.getMessage(key, sender, params)`
(accepts a `CommandSender`/`UUID` for per-player locale, and a `params` map
for placeholder substitution like `<prefix>`, `<player>`, etc.).

## Step 7 - Permissions

Group permissions in an enum backed by
`yv.tils.common.permissions.PermissionManager.YVtilsPermission`, one file per
module (see `discord/src/main/kotlin/yv/tils/discord/data/Permissions.kt` for
the fullest example, including a generated wildcard permission):

```kotlin
package yv.tils.yourmodule.data

import yv.tils.common.permissions.PermissionManager

enum class Permissions(val permission: PermissionManager.YVtilsPermission) {
    DO_THING(
        PermissionManager.YVtilsPermission(
            "yvtils.your-module.do-thing",
            "Allows using the do-thing feature",
            default = true
        )
    ),
}
```

Register them once during `enablePlugin()`:

```kotlin
PermissionManager.registerPermissions(Permissions.entries.map { it.permission })
```

## Step 8 - Listeners

Plain Bukkit `Listener` implementations, registered in
`registerListeners()` via `Core.instance.server.pluginManager.registerEvents(...)`
(see Step 3). Nothing special here compared to a normal Paper plugin.

## Step 9 - Wire it into the dynamic module system

Once the module builds and behaves correctly, follow
[**Part 1 of the migration guide**](./migrating-to-dynamic-modules.md#part-1---migrating-a-feature-module)
to make it fetchable:

1. Add its Gradle project name to `publishableModules` in the root
   `build.gradle.kts`.
2. Add an entry to
   [`DynamicModuleRegistry.KNOWN_MODULES`](../test-core/src/main/kotlin/yv/tils/core/loader/DynamicModuleRegistry.kt)
   (or whichever core's copy you're targeting) mapping the module name to its
   artifactId, version, and entry-point class.
3. Publish it (`./gradlew :your-module:publish`, needs Reposilite credentials
   - see `.env.example`).
4. Sanity-check the generated POM doesn't list `utils`/`config`/`common`/
   CommandAPI/coroutines/serialization as dependencies.

## Step 10 - Test it locally

You don't need real Reposilite credentials or to wait for a real publish to
try your module out. Follow
[**Testing your migration locally**](./migrating-to-dynamic-modules.md#testing-your-migration-locally)
in the migration guide - in short:

```bash
./gradlew publishAllModulesLocally
jwebserver -p 8095 -d "$HOME/.m2/repository"
REPOSILITE_URL="http://127.0.0.1:8095/" ./gradlew :test-core:runServer
```

Then enable your module in the generated `modules.yml` and restart. Check the
[troubleshooting table](./migrating-to-dynamic-modules.md#troubleshooting) in
the migration guide if something doesn't resolve or load correctly.

## Checklist

- [ ] Gradle module created, registered in `settings.gradle.kts`
- [ ] `utils`/`config`/`common` are `compileOnly`; dependencies on other published modules or third-party libraries are `implementation`
- [ ] One `<Name>YVtils : Module.YVtilsModule` entry-point class with a public no-arg constructor, calling `Module.addModule(MODULE)` in `enablePlugin()`
- [ ] Commands registered via the CommandAPI Kotlin DSL
- [ ] Config schema registered in `onLoad()`, loaded in `enablePlugin()` (if the module has configurable settings)
- [ ] User-facing strings registered via `BuildLanguage`/`FileTypes`, retrieved via `LanguageHandler.getMessage(...)` (no hardcoded user-facing text)
- [ ] Permissions declared via `PermissionManager.YVtilsPermission` and registered in `enablePlugin()` (if the module has permission-gated features)
- [ ] Listeners registered through `Core.instance.server.pluginManager`
- [ ] Added to `publishableModules` and `DynamicModuleRegistry.KNOWN_MODULES`
- [ ] Published POM checked for stray `utils`/`config`/`common`/CommandAPI/coroutines/serialization dependencies
- [ ] Tested locally end-to-end (publish, fetch, enable, use the feature in-game)
