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

package yv.tils.essentials.language

import yv.tils.config.language.FileTypes
import yv.tils.config.language.LanguageProvider
import yv.tils.utils.colors.ColorUtils
import yv.tils.utils.colors.Colors

enum class LangStrings(override val key: String, override val translations: Map<FileTypes, String>) :
    LanguageProvider.LangStrings {
    INVENTORY_NOT_FOUND("essentials.inventory.not_found", mapOf(
        FileTypes.EN to "<prefix> <red>Player not found. Use a known name or UUID.",
        FileTypes.DE to "<prefix> <red>Spieler nicht gefunden. Verwende einen bekannten Namen oder eine UUID."
    )),
    INVENTORY_SELF("essentials.inventory.self", mapOf(
        FileTypes.EN to "<prefix> <red>You cannot inspect your own inventory with this command.",
        FileTypes.DE to "<prefix> <red>Du kannst dein eigenes Inventar mit diesem Befehl nicht ansehen."
    )),
    INVENTORY_BUSY("essentials.inventory.busy", mapOf(
        FileTypes.EN to "<prefix> <red>This player is already being inspected. Try again shortly.",
        FileTypes.DE to "<prefix> <red>Dieser Spieler wird bereits angesehen. Versuche es gleich erneut."
    )),
    INVENTORY_CONNECTING("essentials.inventory.connecting", mapOf(
        FileTypes.EN to "<prefix> <red>This player is connecting. Reopen the editor after login.",
        FileTypes.DE to "<prefix> <red>Dieser Spieler verbindet sich gerade. Öffne den Editor nach der Anmeldung erneut."
    )),
    INVENTORY_LOAD_FAILED("essentials.inventory.load_failed", mapOf(
        FileTypes.EN to "<prefix> <red>The inventory could not be loaded. See the server log for details.",
        FileTypes.DE to "<prefix> <red>Das Inventar konnte nicht geladen werden. Details stehen im Serverprotokoll."
    )),
    INVENTORY_EDIT_FAILED("essentials.inventory.edit_failed", mapOf(
        FileTypes.EN to "<prefix> <red>The change could not be applied. The inventory may have changed; reopen it and try again.",
        FileTypes.DE to "<prefix> <red>Die Änderung konnte nicht übernommen werden. Das Inventar wurde möglicherweise geändert; öffne es erneut und versuche es noch einmal."
    )),
    INVENTORY_LOGIN_HANDOFF("essentials.inventory.login_handoff", mapOf(
        FileTypes.EN to "<prefix> <${Colors.SECONDARY.color}><player><white> is connecting. Your inventory editor was closed; completed changes are saved. <${Colors.TERTIARY.color}>Reopen it after login to continue editing live.",
        FileTypes.DE to "<prefix> <${Colors.SECONDARY.color}><player><white> verbindet sich. Dein Inventareditor wurde geschlossen; abgeschlossene Änderungen sind gespeichert. <${Colors.TERTIARY.color}>Öffne ihn nach der Anmeldung erneut, um live weiterzubearbeiten."
    )),
    INVENTORY_LOGOUT_HANDOFF("essentials.inventory.logout_handoff", mapOf(
        FileTypes.EN to "<prefix> <${Colors.SECONDARY.color}><player><white> disconnected. Your inventory editor was closed; completed changes are saved. <${Colors.TERTIARY.color}>Reopen it to continue editing offline.",
        FileTypes.DE to "<prefix> <${Colors.SECONDARY.color}><player><white> hat den Server verlassen. Dein Inventareditor wurde geschlossen; abgeschlossene Änderungen sind gespeichert. <${Colors.TERTIARY.color}>Öffne ihn erneut, um offline weiterzubearbeiten."
    )),
    INVENTORY_REOPEN("essentials.inventory.reopen", mapOf(
        FileTypes.EN to "<${Colors.SECONDARY.color}><underlined>[Reopen inventory]</underlined>",
        FileTypes.DE to "<${Colors.SECONDARY.color}><underlined>[Inventar erneut öffnen]</underlined>"
    )),
    INVENTORY_REOPEN_HINT("essentials.inventory.reopen_hint", mapOf(
        FileTypes.EN to "<${Colors.TERTIARY.color}>Click to reopen this player's inventory.",
        FileTypes.DE to "<${Colors.TERTIARY.color}>Klicke, um das Inventar dieses Spielers erneut zu öffnen."
    )),
    INVENTORY_OPEN_EDIT("essentials.inventory.open_edit", mapOf(
        FileTypes.EN to "<prefix> <white>Inspecting <${Colors.SECONDARY.color}><player><white>. Left-click: swap; right-click: half/one item; middle-click: copy in creative; shift-click: transfer; drag: distribute. <${Colors.TERTIARY.color}>Changes save immediately.",
        FileTypes.DE to "<prefix> <white>Du siehst das Inventar von <${Colors.SECONDARY.color}><player><white>. Linksklick: tauschen; Rechtsklick: Hälfte/ein Item; Mittelklick: im Kreativmodus kopieren; Umschalt-Klick: verschieben; Ziehen: verteilen. <${Colors.TERTIARY.color}>Änderungen werden sofort gespeichert."
    )),
    INVENTORY_OPEN_READ_ONLY("essentials.inventory.open_read_only", mapOf(
        FileTypes.EN to "<prefix> <white>Inspecting <${Colors.SECONDARY.color}><player><white> in <${Colors.TERTIARY.color}>read-only mode<white>.",
        FileTypes.DE to "<prefix> <white>Du siehst das Inventar von <${Colors.SECONDARY.color}><player><white> im <${Colors.TERTIARY.color}>Lesemodus<white>."
    )),
    INVENTORY_PLAYER_TITLE("essentials.inventory.player_title", mapOf(
        FileTypes.EN to "<${Colors.MAIN.color}>Invsee <dark_gray>» <${Colors.TERTIARY.color}><player>",
        FileTypes.DE to "<${Colors.MAIN.color}>Invsee <dark_gray>» <${Colors.TERTIARY.color}><player>"
    )),
    INVENTORY_ENDER_TITLE("essentials.inventory.ender_title", mapOf(
        FileTypes.EN to "<${Colors.MAIN.color}>ECsee <dark_gray>» <${Colors.TERTIARY.color}><player>",
        FileTypes.DE to "<${Colors.MAIN.color}>ECsee <dark_gray>» <${Colors.TERTIARY.color}><player>"
    )),
    INVENTORY_INFO("essentials.inventory.info", mapOf(
        FileTypes.EN to "<${Colors.SECONDARY.color}>Inventory inspection",
        FileTypes.DE to "<${Colors.SECONDARY.color}>Inventaransicht"
    )),
    INVENTORY_TARGET("essentials.inventory.target", mapOf(
        FileTypes.EN to "<gray>Player: <${Colors.TERTIARY.color}><player>",
        FileTypes.DE to "<gray>Spieler: <${Colors.TERTIARY.color}><player>"
    )),
    INVENTORY_ONLINE("essentials.inventory.online", mapOf(
        FileTypes.EN to "<gray>Status: <${Colors.SECONDARY.color}>Online <gray>• Refreshes live",
        FileTypes.DE to "<gray>Status: <${Colors.SECONDARY.color}>Online <gray>• Live-Aktualisierung"
    )),
    INVENTORY_OFFLINE("essentials.inventory.offline", mapOf(
        FileTypes.EN to "<gray>Status: <${Colors.TERTIARY.color}>Offline <gray>• Saved player data",
        FileTypes.DE to "<gray>Status: <${Colors.TERTIARY.color}>Offline <gray>• Gespeicherte Spielerdaten"
    )),
    INVENTORY_EDIT_HINT("essentials.inventory.edit_hint", mapOf(
        FileTypes.EN to "<${Colors.TERTIARY.color}>Left: swap • Right: half/one • Middle: creative copy<newline>Shift-click: transfer • Drag: distribute",
        FileTypes.DE to "<${Colors.TERTIARY.color}>Links: tauschen • Rechts: Hälfte/eins • Mitte: Kreativkopie<newline>Umschalt-Klick: verschieben • Ziehen: verteilen"
    )),
    INVENTORY_READ_ONLY_HINT("essentials.inventory.read_only_hint", mapOf(
        FileTypes.EN to "<${Colors.TERTIARY.color}>Read-only • Editing is not permitted",
        FileTypes.DE to "<${Colors.TERTIARY.color}>Lesemodus • Bearbeiten ist nicht erlaubt"
    )),
    INVENTORY_HELMET("essentials.inventory.helmet", mapOf(
        FileTypes.EN to "<${Colors.SECONDARY.color}>Helmet", FileTypes.DE to "<${Colors.SECONDARY.color}>Helm"
    )),
    INVENTORY_CHESTPLATE("essentials.inventory.chestplate", mapOf(
        FileTypes.EN to "<${Colors.SECONDARY.color}>Chestplate", FileTypes.DE to "<${Colors.SECONDARY.color}>Brustpanzer"
    )),
    INVENTORY_LEGGINGS("essentials.inventory.leggings", mapOf(
        FileTypes.EN to "<${Colors.SECONDARY.color}>Leggings", FileTypes.DE to "<${Colors.SECONDARY.color}>Beinschutz"
    )),
    INVENTORY_BOOTS("essentials.inventory.boots", mapOf(
        FileTypes.EN to "<${Colors.SECONDARY.color}>Boots", FileTypes.DE to "<${Colors.SECONDARY.color}>Stiefel"
    )),
    INVENTORY_OFFHAND("essentials.inventory.offhand", mapOf(
        FileTypes.EN to "<${Colors.SECONDARY.color}>Offhand", FileTypes.DE to "<${Colors.SECONDARY.color}>Nebenhand"
    )),
    INVENTORY_EMPTY("essentials.inventory.empty", mapOf(
        FileTypes.EN to "<gray>Empty equipment slot", FileTypes.DE to "<gray>Leerer Ausrüstungsslot"
    )),
    INVENTORY_STORAGE("essentials.inventory.storage", mapOf(
        FileTypes.EN to "<${Colors.SECONDARY.color}>↑ Main inventory", FileTypes.DE to "<${Colors.SECONDARY.color}>↑ Hauptinventar"
    )),
    INVENTORY_HOTBAR("essentials.inventory.hotbar", mapOf(
        FileTypes.EN to "<${Colors.SECONDARY.color}>↓ Hotbar", FileTypes.DE to "<${Colors.SECONDARY.color}>↓ Schnellzugriffsleiste"
    )),
    INVENTORY_ENDER_CHEST("essentials.inventory.ender_chest", mapOf(
        FileTypes.EN to "<${Colors.SECONDARY.color}>↓ Ender chest", FileTypes.DE to "<${Colors.SECONDARY.color}>↓ Endertruhe"
    )),
    CONFIG_TITLE(
        "essentials.config.title", mapOf(
            FileTypes.EN to "Essentials Config",
            FileTypes.DE to "Essentials-Einstellungen"
        )
    ),
    CONFIG_DENIED(
        "essentials.config.denied", mapOf(
            FileTypes.EN to "<prefix> <red>You do not have permission to edit Essentials configuration.",
            FileTypes.DE to "<prefix> <red>Du darfst die Essentials-Einstellungen nicht bearbeiten."
        )
    ),
    ANVIL_COST(
        "essentials.anvil.cost", mapOf(
            FileTypes.EN to "<prefix> <gray>Anvil cost: <yellow><cost> levels<gray>. You have <yellow><levels><gray> levels.",
            FileTypes.DE to "<prefix> <gray>Ambosskosten: <yellow><cost> Level<gray>. Du hast <yellow><levels><gray> Level."
        )
    ),
    SPAWN_ELYTRA_BOOST(
        "spawn.elytra.boost",
        mapOf(
            FileTypes.EN to "<white>Press <yellow><key:key.swapOffhand><white> to boost!",
            FileTypes.DE to "<white>Drücke <yellow><key:key.swapOffhand><white> für einen Schub!",
        )
    ),
    GAMEMODE_SURVIVAL(
        "gamemode.survival",
        mapOf(
            FileTypes.EN to "Survival",
            FileTypes.DE to "Überleben",
        )
    ),
    GAMEMODE_CREATIVE(
        "gamemode.creative",
        mapOf(
            FileTypes.EN to "Creative",
            FileTypes.DE to "Kreativ",
        )
    ),
    GAMEMODE_ADVENTURE(
        "gamemode.adventure",
        mapOf(
            FileTypes.EN to "Adventure",
            FileTypes.DE to "Abenteuer",
        )
    ),
    GAMEMODE_SPECTATOR(
        "gamemode.spectator",
        mapOf(
            FileTypes.EN to "Spectator",
            FileTypes.DE to "Zuschauer",
        )
    ),

    GLOBALMUTE_TRY_TO_WRITE(
        "globalmute.try_to_write",
        mapOf(
            FileTypes.EN to "<prefix> <red>Global mute is enabled! You can't write messages!",
            FileTypes.DE to "<prefix> <red>Global mute ist aktiviert! Du kannst keine Nachrichten schreiben!",
        )
    ),

    COMMAND_FLY_ENABLE_SELF(
        "command.fly.enable.self",
        mapOf(
            FileTypes.EN to "<prefix> <white>You can <green>now <white>fly!",
            FileTypes.DE to "<prefix> <white>Du kannst <green>nun <white>fliegen!",
        )
    ),
    COMMAND_FLY_ENABLE_OTHER(
        "command.fly.enable.other",
        mapOf(
            FileTypes.EN to "<prefix> <white><player> can <green>now <white>fly!",
            FileTypes.DE to "<prefix> <white><player> kann <green>nun <white>fliegen!",
        )
    ),
    COMMAND_FLY_DISABLE_SELF(
        "command.fly.disable.self",
        mapOf(
            FileTypes.EN to "<prefix> <white>You can <red>no longer <white>fly!",
            FileTypes.DE to "<prefix> <white>Du kannst nun <red>nicht mehr <white>fliegen!",
        )
    ),
    COMMAND_FLY_DISABLE_OTHER(
        "command.fly.disable.other",
        mapOf(
            FileTypes.EN to "<prefix> <white><player> can <red>no longer <white>fly!",
            FileTypes.DE to "<prefix> <white><player> kann nun <red>nicht mehr <white>fliegen!",
        )
    ),

    COMMAND_GAMEMODE_SELF(
        "command.gamemode.self",
        mapOf(
            FileTypes.EN to "<prefix> <white>Your gamemode has been set to <green><gamemode><white>!",
            FileTypes.DE to "<prefix> <white>Dein Spielmodus wurde zu <green><gamemode> <white>geändert!",
        )
    ),
    COMMAND_GAMEMODE_OTHER(
        "command.gamemode.other",
        mapOf(
            FileTypes.EN to "<prefix> <white><player>'s gamemode has been set to <green><gamemode><white>!",
            FileTypes.DE to "<prefix> <white>Der Spielmodus von <player> wurde zu <green><gamemode> <white>geändert!",
        )
    ),

    COMMAND_GLOBALMUTE_ALREADY(
        "command.globalmute.already",
        mapOf(
            FileTypes.EN to "<prefix> <red>Global mute is already in this state!",
            FileTypes.DE to "<prefix> <red>Global mute ist bereits in diesem Zustand!",
        )
    ),
    COMMAND_GLOBALMUTE_ENABLE(
        "command.globalmute.enable",
        mapOf(
            FileTypes.EN to "<prefix> <white>Global mute has been <green>enabled<white>!",
            FileTypes.DE to "<prefix> <white>Global mute wurde <green>aktiviert<white>!",
        )
    ),
    COMMAND_GLOBALMUTE_DISABLE(
        "command.globalmute.disable",
        mapOf(
            FileTypes.EN to "<prefix> <white>Global mute has been <red>disabled<white>!",
            FileTypes.DE to "<prefix> <white>Global mute wurde <red>deaktiviert<white>!",
        )
    ),

    COMMAND_GOD_ENABLE_SELF(
        "command.god.enable.self",
        mapOf(
            FileTypes.EN to "<prefix> <white>You are now <green>invulnerable<white>!",
            FileTypes.DE to "<prefix> <white>Du bist nun <green>unverwundbar<white>!",
        )
    ),
    COMMAND_GOD_ENABLE_OTHER(
        "command.god.enable.other",
        mapOf(
            FileTypes.EN to "<prefix> <white><player> is now <green>invulnerable<white>!",
            FileTypes.DE to "<prefix> <white><player> ist nun <green>unverwundbar<white>!",
        )
    ),
    COMMAND_GOD_DISABLE_SELF(
        "command.god.disable.self",
        mapOf(
            FileTypes.EN to "<prefix> <white>You are no longer <red>invulnerable<white>!",
            FileTypes.DE to "<prefix> <white>Du bist nun <red>nicht mehr <white>unverwundbar!",
        )
    ),
    COMMAND_GOD_DISABLE_OTHER(
        "command.god.disable.other",
        mapOf(
            FileTypes.EN to "<prefix> <white><player> is no longer <red>invulnerable<white>!",
            FileTypes.DE to "<prefix> <white><player> ist nun <red>nicht mehr <white>unverwundbar!",
        )
    ),

    COMMAND_HEAL_SELF(
        "command.heal.self",
        mapOf(
            FileTypes.EN to "<prefix> <white>You have been <green>healed<white>!",
            FileTypes.DE to "<prefix> <white>Du wurdest <green>geheilt<white>!",
        )
    ),
    COMMAND_HEAL_OTHER(
        "command.heal.other",
        mapOf(
            FileTypes.EN to "<prefix> <white><player> has been <green>healed<white>!",
            FileTypes.DE to "<prefix> <white><player> wurde <green>geheilt<white>!",
        )
    ),

    COMMAND_PING_SELF(
        "command.ping.self",
        mapOf(
            FileTypes.EN to "<prefix> <white>Your ping is <green><ping><white>ms!",
            FileTypes.DE to "<prefix> <white>Dein Ping ist <green><ping><white>ms!",
        )
    ),
    COMMAND_PING_OTHER(
        "command.ping.other",
        mapOf(
            FileTypes.EN to "<prefix> <white><player>'s ping is <green><ping><white>ms!",
            FileTypes.DE to "<prefix> <white>Der Ping von <player> ist <green><ping><white>ms!",
        )
    ),

    COMMAND_SPEED_CHANGE_SELF(
        "command.speed.change.self",
        mapOf(
            FileTypes.EN to "<prefix> <white>Your speed has been set to <green><speed><white>!",
            FileTypes.DE to "<prefix> <white>Deine Geschwindigkeit wurde auf <green><speed><white> gesetzt!",
        )
    ),
    COMMAND_SPEED_CHANGE_OTHER(
        "command.speed.change.other",
        mapOf(
            FileTypes.EN to "<prefix> <white><player>'s speed has been set to <green><speed><white>!",
            FileTypes.DE to "<prefix> <white>Die Geschwindigkeit von <player> wurde auf <green><speed><white> gesetzt!",
        )
    ),
    COMMAND_SPEED_RESET_SELF(
        "command.speed.reset.self",
        mapOf(
            FileTypes.EN to "<prefix> <white>Your speed has been reset!",
            FileTypes.DE to "<prefix> <white>Deine Geschwindigkeit wurde zurückgesetzt!",
        )
    ),
    COMMAND_SPEED_RESET_OTHER(
        "command.speed.reset.other",
        mapOf(
            FileTypes.EN to "<prefix> <white><player>'s speed has been reset!",
            FileTypes.DE to "<prefix> <white>Die Geschwindigkeit von <player> wurde zurückgesetzt!",
        )
    ),

    COMMAND_PVP_ALREADY(
        "command.pvp.already",
        mapOf(
            FileTypes.EN to "<prefix> <red>PvP is already in this state!",
            FileTypes.DE to "<prefix> <red>PvP ist bereits in diesem Zustand!",
        )
    ),

    COMMAND_PVP_ENABLE(
        "command.pvp.enable",
        mapOf(
            FileTypes.EN to "<prefix> <white>PvP has been <green>enabled<white>!",
            FileTypes.DE to "<prefix> <white>PvP wurde <green>aktiviert<white>!",
        )
    ),

    COMMAND_PVP_DISABLE(
        "command.pvp.disable",
        mapOf(
            FileTypes.EN to "<prefix> <white>PvP has been <red>disabled<white>!",
            FileTypes.DE to "<prefix> <white>PvP wurde <red>deaktiviert<white>!",
        )
    ),

    COMMAND_PVP_STATE_ENABLED(
        "command.pvp.state.enabled",
        mapOf(
            FileTypes.EN to "<prefix> <white>PvP is <green>enabled<white>!",
            FileTypes.DE to "<prefix> <white>PvP ist <green>aktiviert<white>!",
        )
    ),

    COMMAND_PVP_STATE_DISABLED(
        "command.pvp.state.disabled",
        mapOf(
            FileTypes.EN to "<prefix> <white>PvP is <red>disabled<white>!",
            FileTypes.DE to "<prefix> <white>PvP ist <red>deaktiviert<white>!",
        )
    ),

    PVP_DISABLED(
        "pvp.disabled",
        mapOf(
            FileTypes.EN to "<red>PvP is currently disabled.",
            FileTypes.DE to "<red>PvP ist derzeit deaktiviert.",
        )
    ),

    COMMAND_DIMENSION_DISABLED(
        "command.dimension.disabled",
        mapOf(
            FileTypes.EN to "<prefix> <white>You have <red>disabled<white> travel to the <${Colors.TERTIARY.color}><dimension><white> world!",
            FileTypes.DE to "<prefix> <white>Du hast das Reisen zur Welt <${Colors.TERTIARY.color}><dimension><white> <red>deaktiviert<white>!",
        )
    ),

    COMMAND_DIMENSION_ENABLED(
        "command.dimension.enabled",
        mapOf(
            FileTypes.EN to "<prefix> <white>You have <green>enabled<white> travel to the <${Colors.TERTIARY.color}><dimension><white> world!",
            FileTypes.DE to "<prefix> <white>Du hast das Reisen zur Welt <${Colors.TERTIARY.color}><dimension><white> <green>aktiviert<white>!",
        )
    ),

    COMMAND_DIMENSION_ALREADY(
        "command.dimension.already",
        mapOf(
            FileTypes.EN to "<prefix> <red>Travel to the <${Colors.TERTIARY.color}><dimension><red> world is already in this state!",
            FileTypes.DE to "<prefix> <red>Das Reisen zur Welt <${Colors.TERTIARY.color}><dimension><red> ist bereits in diesem Zustand!",
        )
    ),

    COMMAND_DIMENSION_STATE_ENABLED(
        "command.dimension.state.enabled",
        mapOf(
            FileTypes.EN to "<prefix> <white>Travel to the <${Colors.TERTIARY.color}><dimension><white> world is <green>enabled<white>!",
            FileTypes.DE to "<prefix> <white>Das Reisen zur Welt <${Colors.TERTIARY.color}><dimension><white> ist <green>aktiviert<white>!",
        )
    ),

    COMMAND_DIMENSION_STATE_DISABLED(
        "command.dimension.state.disabled",
        mapOf(
            FileTypes.EN to "<prefix> <white>Travel to the <${Colors.TERTIARY.color}><dimension><white> world is <red>disabled<white>!",
            FileTypes.DE to "<prefix> <white>Das Reisen zur Welt <${Colors.TERTIARY.color}><dimension><white> ist <red>deaktiviert<white>!",
        )
    ),

    DIMENSION_TRAVEL_DISABLED(
        "dimension.travel.disabled",
        mapOf(
            FileTypes.EN to "<white>Travel to the <${Colors.TERTIARY.color}><dimension><white> world is currently disabled.",
            FileTypes.DE to "<white>Das Reisen zur Welt <${Colors.TERTIARY.color}><dimension><white> ist derzeit deaktiviert.",
        )
    )
    ;
}
