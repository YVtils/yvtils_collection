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

enum class LangStrings(override val key: String, override val translations: Map<FileTypes, String>) : LanguageProvider.LangStrings {
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
