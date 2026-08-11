#!/usr/bin/env bash
#
# Part of the YVtils Project.
# Copyright (c) 2026 Lyvric / YVtils
#
# Licensed under the Mozilla Public License 2.0 (MPL-2.0)
# with additional YVtils License Terms.
# License information: https://yvtils.net/license
#
# Use of the YVtils name, logo, or brand assets is subject to
# the YVtils Brand Protection Clause.
#
# Scaffolds a brand-new YVtils feature module and wires it into every place
# that needs to know about it, per docs/adding-a-new-module.md:
#
#   1. Creates <module>/build.gradle.kts and the module's full source tree:
#      entry-point class, config (data class + ConfigFile + in-game config
#      GUI), translations, permissions (incl. wildcard), a README.md, and -
#      if you opt in - a commands/ and/or listeners/ package with one working
#      example each.
#   2. Registers it in settings.gradle.kts (`include(...)`).
#   3. Registers it in the root build.gradle.kts `publishableModules` set.
#   4. Registers it in core's DynamicModuleRegistry.KNOWN_MODULES so it's
#      fetchable/toggleable at runtime.
#
# Fully interactive by default - just run it with no arguments and it walks
# you through everything (module name, description, hidden, commands,
# listeners), one question at a time:
#
#   ./scripts/new-module.sh
#
# Anything you'd rather not be asked about can instead be supplied up front,
# either positionally (module name, then description) or via flags - only
# whatever's still missing/undecided afterward gets prompted for:
#
#   ./scripts/new-module.sh waypoint-v2 "Second waypoint system, experimental."
#   ./scripts/new-module.sh internal-tool "One-off internal tool." --hidden --no-commands --no-listeners
#   ./scripts/new-module.sh my-feature "Does a thing." --yes
#
# Options:
#   --hidden               Internal/non-togglable module (e.g. like `migration`) -
#                          excluded from `publishableModules` and marked
#                          `hidden = true` in the module registry.
#   --public               Explicitly the opposite of --hidden (skips that prompt).
#   --with-commands        Scaffold a commands/ package + one example command.
#   --no-commands          Skip commands/ entirely.
#   --with-listeners       Scaffold a listeners/ package + one example listener.
#   --no-listeners         Skip listeners/ entirely.
#   --yes, -y              Non-interactive: accept the default answer for
#                          every remaining yes/no prompt (hidden = no,
#                          commands = yes, listeners = yes) instead of asking.
#                          A missing module name or description still falls
#                          back to being prompted for unless also piped/non-tty,
#                          in which case a placeholder description is used and
#                          a missing module name is still a hard error.
#
# Every prompt is skipped automatically (falling back to its default) when
# stdin isn't a terminal (e.g. piped input, CI) - you don't need --yes for
# that case, only to silence prompts while still running interactively.
#
# Config/language/permissions/config-GUI scaffolding is ALWAYS generated
# (every module needs somewhere to put these, even if empty to start) -
# only commands/listeners are conditional, since plenty of modules
# legitimately need only one of the two (or neither, to start with).
#
# After running this script you still need to:
#   - Fill in the actual feature logic
#   - Flesh out config fields, translations and permissions beyond the
#     placeholder example each package ships with
#   - Add an icon to ModuleIcons.kt if you want a dedicated GUI icon
#     (optional, purely cosmetic - falls back to Material.PAPER otherwise)
#   - Fill in the generated README.md
#   - Run `./gradlew :your-module:build` to make sure it compiles
#
# See docs/adding-a-new-module.md for the full picture.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

print_usage() {
    echo "Usage: $0 [module-name] [\"Description\"] [--hidden|--public] [--with-commands|--no-commands] [--with-listeners|--no-listeners] [--yes]" >&2
    echo "Run with no arguments for a fully interactive walkthrough." >&2
}

MODULE_NAME=""
DESCRIPTION=""
HIDDEN=""
ADD_COMMANDS=""
ADD_LISTENERS=""
ASSUME_YES="false"
POSITIONAL_COUNT=0

while [[ $# -gt 0 ]]; do
    case "$1" in
        --hidden)
            HIDDEN="true"
            ;;
        --public)
            HIDDEN="false"
            ;;
        --with-commands)
            ADD_COMMANDS="true"
            ;;
        --no-commands)
            ADD_COMMANDS="false"
            ;;
        --with-listeners)
            ADD_LISTENERS="true"
            ;;
        --no-listeners)
            ADD_LISTENERS="false"
            ;;
        --yes|-y)
            ASSUME_YES="true"
            ;;
        --help|-h)
            print_usage
            exit 0
            ;;
        -*)
            echo "error: unknown option '$1'" >&2
            print_usage
            exit 1
            ;;
        *)
            if [[ "$POSITIONAL_COUNT" -eq 0 ]]; then
                MODULE_NAME="$1"
            elif [[ "$POSITIONAL_COUNT" -eq 1 ]]; then
                DESCRIPTION="$1"
            else
                echo "error: unexpected extra argument '$1'" >&2
                print_usage
                exit 1
            fi
            POSITIONAL_COUNT=$((POSITIONAL_COUNT + 1))
            ;;
    esac
    shift
done

MODULE_NAME_VALID_RE='^[a-zA-Z][a-zA-Z0-9_-]*$'

validate_module_name() {
    local name="$1"

    if [[ ! "$name" =~ $MODULE_NAME_VALID_RE ]]; then
        echo "error: module name '$name' must start with a letter and contain only letters, digits, '-' or '_'" >&2
        return 1
    fi

    if [[ -d "$name" ]]; then
        echo "error: directory '$name' already exists" >&2
        return 1
    fi

    if grep -q "include(\"$name\")" settings.gradle.kts; then
        echo "error: '$name' is already registered in settings.gradle.kts" >&2
        return 1
    fi

    return 0
}

# --- Interactive prompts (only for whatever wasn't already decided via args) -
prompt_yes_no() {
    local question="$1"
    local default_yes="$2" # "true" or "false"

    if [[ "$ASSUME_YES" == "true" || ! -t 0 ]]; then
        echo "$default_yes"
        return
    fi

    local suffix="[Y/n]"
    [[ "$default_yes" == "false" ]] && suffix="[y/N]"

    local reply
    read -rp "$question $suffix " reply || reply=""
    reply="$(echo "$reply" | tr '[:upper:]' '[:lower:]')"

    if [[ -z "$reply" ]]; then
        echo "$default_yes"
    elif [[ "$reply" == "y" || "$reply" == "yes" ]]; then
        echo "true"
    else
        echo "false"
    fi
}

# Module name: keep (re-)prompting until a valid, unused name is given - this
# is the one thing the rest of the script can't sensibly fall back on, so
# unlike the other prompts below, non-interactive contexts (no tty) get a
# hard error instead of a made-up default.
if [[ -z "$MODULE_NAME" ]]; then
    if [[ ! -t 0 ]]; then
        echo "error: no module name given, and no terminal to prompt for one (pass it as the first argument)" >&2
        print_usage
        exit 1
    fi

    while true; do
        read -rp "Module name (e.g. 'waypoint-v2'): " MODULE_NAME || true
        if [[ -z "$MODULE_NAME" ]]; then
            echo "error: a module name is required" >&2
            continue
        fi
        validate_module_name "$MODULE_NAME" && break
        MODULE_NAME=""
    done
else
    validate_module_name "$MODULE_NAME" || exit 1
fi

if [[ -z "$DESCRIPTION" ]]; then
    if [[ "$ASSUME_YES" == "true" || ! -t 0 ]]; then
        DESCRIPTION="TODO: describe this module."
    else
        read -rp "Short description [TODO: describe this module.]: " DESCRIPTION || true
        [[ -z "$DESCRIPTION" ]] && DESCRIPTION="TODO: describe this module."
    fi
fi

if [[ -z "$HIDDEN" ]]; then
    HIDDEN="$(prompt_yes_no "Is '$MODULE_NAME' an internal/non-toggleable module (like 'migration')?" "false")"
fi

if [[ -z "$ADD_COMMANDS" ]]; then
    ADD_COMMANDS="$(prompt_yes_no "Does '$MODULE_NAME' need commands?" "true")"
fi

if [[ -z "$ADD_LISTENERS" ]]; then
    ADD_LISTENERS="$(prompt_yes_no "Does '$MODULE_NAME' need Bukkit event listeners?" "true")"
fi

# --- Derive package name and class prefix -----------------------------------
# package: dashes/underscores stripped, all lowercase (e.g. "your-module" -> "yourmodule")
PACKAGE_NAME="$(echo "$MODULE_NAME" | tr -d '_-' | tr '[:upper:]' '[:lower:]')"

# class prefix: PascalCase per dash/underscore-separated segment (e.g. "your-module" -> "YourModule")
CLASS_PREFIX="$(echo "$MODULE_NAME" | awk -F'[-_]' '{
    out = ""
    for (i = 1; i <= NF; i++) {
        s = $i
        first = toupper(substr(s, 1, 1))
        rest = substr(s, 2)
        out = out first rest
    }
    print out
}')"

CLASS_NAME="${CLASS_PREFIX}YVtils"
STATE_CLASS_NAME="${CLASS_PREFIX}ConfigState"
COMMAND_CLASS_NAME="${CLASS_PREFIX}Command"
LISTENER_CLASS_NAME="${CLASS_PREFIX}Join"
ROOT_VERSION="$(grep -m1 'version = "' build.gradle.kts | sed -E 's/.*version = "([^"]+)".*/\1/')"

echo
echo "Module name:   $MODULE_NAME"
echo "Package:       yv.tils.$PACKAGE_NAME"
echo "Entry class:   $CLASS_NAME"
echo "Version:       $ROOT_VERSION"
echo "Hidden:        $HIDDEN"
echo "Commands:      $ADD_COMMANDS"
echo "Listeners:     $ADD_LISTENERS"
echo

LICENSE_HEADER='/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */'

# --- 1. Gradle module scaffolding -------------------------------------------
BASE_PKG_DIR="$MODULE_NAME/src/main/kotlin/yv/tils/$PACKAGE_NAME"
mkdir -p "$BASE_PKG_DIR/configs" "$BASE_PKG_DIR/language" "$BASE_PKG_DIR/data" "$BASE_PKG_DIR/logic"

[[ "$ADD_COMMANDS" == "true" ]] && mkdir -p "$BASE_PKG_DIR/commands"
[[ "$ADD_LISTENERS" == "true" ]] && mkdir -p "$BASE_PKG_DIR/listeners"

cat > "$MODULE_NAME/build.gradle.kts" <<EOF
$LICENSE_HEADER

dependencies {
    compileOnly(project(":utils"))
    compileOnly(project(":config-v2"))
    compileOnly(project(":common"))
    // Compile-time only, to reference GUI types - NOT shaded into this module's own
    // published artifact. The actual gui-<version> build used at runtime is resolved
    // dynamically by core's DynamicModuleLoader, matching the running server's Minecraft
    // version (InvUI, which \`gui\` wraps, dropped multi-version support in v2 - see
    // DynamicModuleRegistry.GUI_ARTIFACTS for the full explanation).
    compileOnly(project(":gui-26.1"))
}
EOF

# --- 2. Config: data class + ConfigFile + config GUI ------------------------
cat > "$BASE_PKG_DIR/configs/${STATE_CLASS_NAME}.kt" <<EOF
$LICENSE_HEADER

package yv.tils.$PACKAGE_NAME.configs

import yv.tils.configv2.data.annotations.ConfigDescription
import yv.tils.configv2.data.annotations.NotGuiEditable

/**
 * $MODULE_NAME's \`config.yml\`, persisted as a plain data class via
 * \`ObjectMapperFileUtils\` and editable in-game through [yv.tils.gui.logic.DataClassConfigGui]
 * (see [yv.tils.$PACKAGE_NAME.configs.ManageGUI]) - add more fields here as the module grows,
 * annotated with [ConfigDescription] (GUI lore text) and, where relevant, [NotGuiEditable].
 */
data class ${STATE_CLASS_NAME}(
    @NotGuiEditable
    @ConfigDescription("Documentation URL")
    val documentation: String = "https://docs.yvtils.net/$MODULE_NAME/config.yml",

    @ConfigDescription("Whether $MODULE_NAME is enabled")
    var enabled: Boolean = true,
)
EOF

cat > "$BASE_PKG_DIR/configs/ConfigFile.kt" <<EOF
$LICENSE_HEADER

package yv.tils.$PACKAGE_NAME.configs

import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ObjectMapperFileUtils

class ConfigFile {
    companion object {
        /** The single source of truth. */
        var state: $STATE_CLASS_NAME = $STATE_CLASS_NAME()

        /**
         * Flattened \`"a.b.c" -> value\` view derived from [state], re-synced on every
         * [loadConfig] call - kept around purely so \`ConfigFile.config["key"]\`-style call
         * sites work without needing the full data class shape.
         */
        val config: MutableMap<String, Any> = mutableMapOf()

        private fun syncDerivedView() {
            config.clear()
            config.putAll(ObjectMapperFileUtils.flatten(state, ConfigFormat.YAML))
        }
    }

    private val filePath = "/$MODULE_NAME/config.yml"

    fun loadConfig() {
        state = ObjectMapperFileUtils.load(filePath, $STATE_CLASS_NAME(), format = ConfigFormat.YAML)
        registerStrings()
        syncDerivedView()
    }

    fun registerStrings() {
        ObjectMapperFileUtils.save(filePath, state, format = ConfigFormat.YAML)
    }

    /** Called by [yv.tils.gui.logic.DataClassConfigGui]'s saver after an in-game edit. */
    fun applyState(newState: $STATE_CLASS_NAME) {
        state = newState
        syncDerivedView()
        registerStrings()
    }
}
EOF

cat > "$BASE_PKG_DIR/configs/ManageGUI.kt" <<EOF
$LICENSE_HEADER

package yv.tils.$PACKAGE_NAME.configs

import org.bukkit.entity.Player
import yv.tils.gui.logic.DataClassConfigGui

class ManageGUI {
    fun openGUI(sender: Player) {
        DataClassConfigGui.open(
            sender,
            "${CLASS_PREFIX} Config",
            ConfigFile.state,
            saver = { updated -> ConfigFile().applyState(updated) }
        )
    }
}
EOF

# --- 3. Translations ---------------------------------------------------------
cat > "$BASE_PKG_DIR/language/RegisterStrings.kt" <<EOF
$LICENSE_HEADER

package yv.tils.$PACKAGE_NAME.language

import yv.tils.configv2.language.BuildLanguage
import yv.tils.configv2.language.FileTypes

class RegisterStrings {
    fun registerStrings() {
        registerNewString(
            "command.$PACKAGE_NAME.example",
            mapOf(
                FileTypes.EN to "<prefix> <white>Hello from $MODULE_NAME!",
                FileTypes.DE to "<prefix> <white>Hallo von $MODULE_NAME!",
            )
        )
    }

    private fun registerNewString(langKey: String, translations: Map<FileTypes, String>) {
        translations.forEach { (fileType, value) ->
            BuildLanguage.registerString(BuildLanguage.RegisteredString(fileType, langKey, value))
        }
    }
}
EOF

# --- 4. Permissions (incl. generated wildcard) -------------------------------
cat > "$BASE_PKG_DIR/data/Permissions.kt" <<EOF
$LICENSE_HEADER

package yv.tils.$PACKAGE_NAME.data

import yv.tils.common.permissions.PermissionManager
import yv.tils.$PACKAGE_NAME.$CLASS_NAME

class PermissionsData {
    companion object {
        val permissionBase = "yvtils.\${${CLASS_NAME}.MODULE.name}"

        val wildcard: PermissionManager.YVtilsPermission
            get() = PermissionManager.YVtilsPermission(
                "\${permissionBase}.*",
                "Wildcard permission for all \${${CLASS_NAME}.MODULE.name} permissions",
                default = false,
                children = getPermissionsForWildcard()
            )

        private fun getPermissionsForWildcard(): Map<String, Boolean> {
            val permissions = mutableMapOf<String, Boolean>()
            val permissionList = PermissionsData().getPermissionList()

            permissionList.forEach { permissions[it.name] = true }

            return permissions
        }
    }

    fun getPermissionList(includeWildcard: Boolean = false): List<PermissionManager.YVtilsPermission> {
        val permList = Permissions.entries.map { it.permission }

        return if (includeWildcard) {
            permList + wildcard
        } else {
            permList
        }
    }
}

enum class Permissions(val permission: PermissionManager.YVtilsPermission) {
    EXAMPLE(
        PermissionManager.YVtilsPermission(
            "\${PermissionsData.permissionBase}.example",
            "Allows using the $MODULE_NAME example command",
            default = true
        )
    ),
}
EOF

# --- 5. Commands (opt-in) ----------------------------------------------------
COMMANDS_IMPORT=""
COMMANDS_REGISTER="        // No commands yet - see docs/adding-a-new-module.md Step 4 to add some."
if [[ "$ADD_COMMANDS" == "true" ]]; then
    cat > "$BASE_PKG_DIR/commands/${COMMAND_CLASS_NAME}.kt" <<EOF
$LICENSE_HEADER

package yv.tils.$PACKAGE_NAME.commands

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import yv.tils.configv2.language.LanguageHandler
import yv.tils.$PACKAGE_NAME.data.Permissions

/**
 * Example command scaffolded by scripts/new-module.sh - replace with real subcommands, or
 * delete this file if the module ends up not needing this particular one.
 */
class ${COMMAND_CLASS_NAME} {
    val command = commandTree("$PACKAGE_NAME") {
        withPermission(Permissions.EXAMPLE.permission.name)
        withUsage("/$PACKAGE_NAME")

        playerExecutor { player, _ ->
            player.sendMessage(
                LanguageHandler.getMessage("command.$PACKAGE_NAME.example", player)
            )
        }
    }
}
EOF
    COMMANDS_IMPORT="import yv.tils.$PACKAGE_NAME.commands.${COMMAND_CLASS_NAME}"
    COMMANDS_REGISTER="        ${COMMAND_CLASS_NAME}()"
fi

# --- 6. Listeners (opt-in) ---------------------------------------------------
LISTENERS_IMPORT=""
LISTENERS_REGISTER="        // No listeners yet - see docs/adding-a-new-module.md Step 8 to add some."
if [[ "$ADD_LISTENERS" == "true" ]]; then
    cat > "$BASE_PKG_DIR/listeners/${LISTENER_CLASS_NAME}.kt" <<EOF
$LICENSE_HEADER

package yv.tils.$PACKAGE_NAME.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * Example listener scaffolded by scripts/new-module.sh - replace with real event handling, or
 * delete this file if the module ends up not needing this particular one.
 */
class ${LISTENER_CLASS_NAME} : Listener {
    @EventHandler
    fun onEvent(e: PlayerJoinEvent) {
        // TODO: implement
    }
}
EOF
    LISTENERS_IMPORT="import yv.tils.$PACKAGE_NAME.listeners.${LISTENER_CLASS_NAME}"
    LISTENERS_REGISTER="        pm.registerEvents(${LISTENER_CLASS_NAME}(), plugin)"
fi

# --- 7. Entry-point class -----------------------------------------------------
REGISTER_LISTENERS_BODY="    private fun registerListeners() {
$LISTENERS_REGISTER
    }"
if [[ "$ADD_LISTENERS" == "true" ]]; then
    REGISTER_LISTENERS_BODY="    private fun registerListeners() {
        val plugin = Core.instance
        val pm = plugin.server.pluginManager

$LISTENERS_REGISTER
    }"
fi

# Build the import block line-by-line so skipped commands/listeners imports
# don't leave stray blank lines behind (rather than heredoc-interpolating
# possibly-empty variables directly).
IMPORT_LINES=()
[[ -n "$COMMANDS_IMPORT" ]] && IMPORT_LINES+=("$COMMANDS_IMPORT")
[[ -n "$LISTENERS_IMPORT" ]] && IMPORT_LINES+=("$LISTENERS_IMPORT")
IMPORT_LINES+=(
    "import yv.tils.$PACKAGE_NAME.configs.ConfigFile"
    "import yv.tils.$PACKAGE_NAME.configs.ManageGUI"
    "import yv.tils.$PACKAGE_NAME.data.PermissionsData"
    "import yv.tils.$PACKAGE_NAME.language.RegisterStrings"
    "import yv.tils.common.permissions.PermissionManager"
    "import yv.tils.gui.core.InvUIBootstrap"
)
[[ "$ADD_LISTENERS" == "true" ]] && IMPORT_LINES+=("import yv.tils.utils.modules.Core")
IMPORT_LINES+=("import yv.tils.utils.modules.Module")
IMPORT_BLOCK="$(printf '%s\n' "${IMPORT_LINES[@]}")"

cat > "$BASE_PKG_DIR/${CLASS_NAME}.kt" <<EOF
$LICENSE_HEADER

package yv.tils.$PACKAGE_NAME

$IMPORT_BLOCK

class $CLASS_NAME : Module.YVtilsModule {
    companion object {
        val MODULE = Module.YVtilsModuleData(
            "$MODULE_NAME",
            "1.0.0",
            "$DESCRIPTION",
            "YVtils",
            "https://docs.yvtils.net/$MODULE_NAME/",
            configGuiOpener = { player -> ManageGUI().openGUI(player) },
        )
    }

    override fun onLoad() {
        RegisterStrings().registerStrings()
    }

    override fun enablePlugin() {
        InvUIBootstrap.ensure()

        Module.addModule(MODULE) // required - this is how the module shows
                                  // up in Module.getModulesString() and the
                                  // intra-jar dependency checks

        registerCommands()
        registerListeners()
        registerPermissions()
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

    private fun registerCommands() {
$COMMANDS_REGISTER
    }

$REGISTER_LISTENERS_BODY

    private fun registerPermissions() {
        PermissionManager.registerPermissions(PermissionsData().getPermissionList(includeWildcard = true))
    }

    private fun loadConfigs() {
        ConfigFile().loadConfig()
    }
}
EOF

echo "Created $MODULE_NAME/ scaffolding (config, language, permissions, config GUI$( [[ "$ADD_COMMANDS" == "true" ]] && echo ", commands" )$( [[ "$ADD_LISTENERS" == "true" ]] && echo ", listeners" ))."

# --- 8. README.md ------------------------------------------------------------
cat > "$MODULE_NAME/README.md" <<EOF
# $CLASS_PREFIX

TODO: One or two sentences describing what this module does.

Part of the [YVtils](https://yvtils.net) plugin collection - see the
[root README](../README.md) for the full module list and how the dynamic
module system works.

## Dependencies

- \`utils\`, \`config-v2\`, \`common\` (always \`compileOnly\`, provided by the shared runtime tier)
- \`gui-26.1\` (\`compileOnly\`, for the in-game config editor)
- TODO: list any other module/third-party dependency added later

## Commands

$( [[ "$ADD_COMMANDS" == "true" ]] && echo "- \`/$MODULE_NAME\` - TODO: describe" || echo "TODO: none yet." )

## Permissions

- \`yvtils.$MODULE_NAME.example\` (default: true) - TODO: describe/replace with real permissions
- \`yvtils.$MODULE_NAME.*\` - wildcard for all of the above

## Config

\`plugins/YVtils/$MODULE_NAME/config.yml\`, editable in-game via \`/yvtils config\`:

| Key | Type | Default | Description |
|---|---|---|---|
| \`enabled\` | boolean | \`true\` | Whether $MODULE_NAME is enabled |

## Development notes

TODO: anything a future contributor should know before touching this module.
EOF

echo "Created $MODULE_NAME/README.md."

# --- 9. settings.gradle.kts --------------------------------------------------
perl -0777 -pi -e "s/include\\(\"yv-smp\"\\)\\n?/include(\"yv-smp\")\\ninclude(\"$MODULE_NAME\")\\n/" settings.gradle.kts
echo "Registered in settings.gradle.kts."

# --- 10. root build.gradle.kts publishableModules ----------------------------
if [[ "$HIDDEN" == "false" ]]; then
    perl -0777 -pi -e "s/(\\n    \"yv-smp\"\\n\\))/\\n    \"yv-smp\",\\n    \"$MODULE_NAME\"\\n)/" build.gradle.kts
    echo "Registered in build.gradle.kts publishableModules."
else
    echo "Skipped publishableModules (--hidden): not published as a standalone Maven artifact."
fi

# --- 11. core's DynamicModuleRegistry.KNOWN_MODULES --------------------------
REGISTRY_FILE="core/src/main/kotlin/yv/tils/core/loader/DynamicModuleRegistry.kt"

NEW_ENTRY="        \"$MODULE_NAME\" to ModuleArtifact(
            \"$MODULE_NAME\", \"$ROOT_VERSION\", \"yv.tils.$PACKAGE_NAME.$CLASS_NAME\",
            \"$DESCRIPTION\","
if [[ "$HIDDEN" == "true" ]]; then
    NEW_ENTRY="$NEW_ENTRY
            hidden = true,"
fi
NEW_ENTRY="$NEW_ENTRY
        ),"

OLD_BLOCK='        "yv-smp" to ModuleArtifact(
            "yv-smp", "10.0.0-dev.2", "yv.tils.yv_smp.YV_SMPYVtils",
            "YV SMP module for YVtils",
        )
    )'

NEW_BLOCK="        \"yv-smp\" to ModuleArtifact(
            \"yv-smp\", \"10.0.0-dev.2\", \"yv.tils.yv_smp.YV_SMPYVtils\",
            \"YV SMP module for YVtils\",
        ),
$NEW_ENTRY
    )"

python3 - "$REGISTRY_FILE" <<PYEOF
import sys
path = sys.argv[1]
old_block = """$OLD_BLOCK"""
new_block = """$NEW_BLOCK"""
with open(path) as f:
    content = f.read()
if old_block not in content:
    print("error: could not find the expected yv-smp block in DynamicModuleRegistry.kt - registry file layout may have changed, please add the module manually", file=sys.stderr)
    sys.exit(1)
content = content.replace(old_block, new_block, 1)
with open(path, "w") as f:
    f.write(content)
PYEOF
echo "Registered in DynamicModuleRegistry.KNOWN_MODULES."

echo
echo "Done. Next steps:"
echo "  - Implement the actual feature in $MODULE_NAME/src/main/kotlin/yv/tils/$PACKAGE_NAME/"
echo "  - Flesh out $STATE_CLASS_NAME, RegisterStrings and Permissions beyond the placeholder example"
echo "  - Fill in $MODULE_NAME/README.md"
echo "  - (optional) add a GUI icon in core/src/main/kotlin/yv/tils/core/commands/gui/ModuleIcons.kt"
echo "  - ./gradlew :$MODULE_NAME:build"
echo "  - See docs/adding-a-new-module.md for the full conventions reference"
