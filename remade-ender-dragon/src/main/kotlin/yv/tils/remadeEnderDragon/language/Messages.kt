/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.remadeEnderDragon.language

import org.bukkit.command.CommandSender
import yv.tils.configv2.language.BuildLanguage
import yv.tils.configv2.language.FileTypes
import yv.tils.configv2.language.LanguageHandler
import org.bukkit.entity.Player
import yv.tils.utils.message.MessageUtils

object Messages {
    fun register() {
        val strings = mapOf(
            "help" to ("<aqua>/redragon start | stop | status | reload | config | trigger [attack]" to "<aqua>/redragon start | stop | status | reload | config | trigger [Angriff]"),
            "result" to ("<gray>Remade Ender Dragon: <result>" to "<gray>Remade Ender Dragon: <result>"),
            "unavailable" to ("<red>No eligible dragon encounter here, or attack unavailable. Use start in a configured End world with a living dragon." to "<red>Kein gültiger Drachenkampf oder Angriff verfügbar. Nutze start in einer konfigurierten End-Welt mit lebendem Drachen."),
            "warning" to ("<gold><attack> <yellow>— watch the ground markers!" to "<gold><attack> <yellow>— achte auf die Bodenmarkierungen!"),
            "focus" to ("<aqua>Destroy the glowing focus to cancel the marked attack!" to "<aqua>Zerstöre den leuchtenden Fokus, um den markierten Angriff abzubrechen!"),
            "interrupted" to ("<green>Attack interrupted!" to "<green>Angriff unterbrochen!"),
            "pool" to ("<green>Healing pool: health, food, repairs and cleansing." to "<green>Heilquelle: Leben, Nahrung, Reparaturen und Reinigung."),
            "blue-name" to ("<blue>Rift Enderman" to "<blue>Riss-Enderman"),
            "focus-name" to ("<aqua>Unstable focus — destroy me!" to "<aqua>Instabiler Fokus — zerstöre mich!"),
            "started" to ("<light_purple>Remade Ender Dragon encounter started." to "<light_purple>Remade-Ender-Dragon-Kampf gestartet."),
            "stopped" to ("<gray>Encounter stopped and temporary objects cleaned up." to "<gray>Kampf gestoppt und temporäre Objekte entfernt."),
            "reloaded" to ("<green>Configuration reloaded; active encounters cleaned up." to "<green>Konfiguration neu geladen; aktive Kämpfe bereinigt."),
            "reload-failed" to ("<red>Invalid configuration: <error>" to "<red>Ungültige Konfiguration: <error>"),
            "no-permission" to ("<red>You do not have permission to edit this configuration." to "<red>Du hast keine Berechtigung, diese Konfiguration zu bearbeiten."),
            "saved" to ("<green>Dragon configuration saved; active encounters cleaned up." to "<green>Drachenkonfiguration gespeichert; aktive Kämpfe bereinigt."),
            "status" to ("<gray>Fighters: <players> | HP: <health>/<maximum> | Active effects: <hazards> | Summons: <summons>" to "<gray>Kämpfer: <players> | Leben: <health>/<maximum> | Aktive Effekte: <hazards> | Monster: <summons>"),
            "status-use" to ("<gray><attack>: <uses> uses" to "<gray><attack>: <uses> Einsätze"),
            "status-phase" to ("<gray>Stage: <phase> | Crystal empowerment: <empowerment>%" to "<gray>Phase: <phase> | Kristallverstärkung: <empowerment>%"),
            "anchor-name" to ("<light_purple>Rift anchor" to "<light_purple>Rissanker"),
            "gui.title" to ("Remade Ender Dragon Config" to "Remade Ender Dragon Einstellungen"),
            "gui.general" to ("General settings" to "Allgemeine Einstellungen"),
            "gui.attacks" to ("Attacks and support" to "Angriffe und Unterstützung"),
            "gui.activation" to ("Activation: <mode>" to "Aktivierung: <mode>"),
            "gui.activation-lore" to ("Click to cycle activation policy" to "Klicken, um den Aktivierungsmodus zu wechseln"),
            "gui.edit-lore" to ("Click to edit; validated changes save when closing the editor" to "Klicken zum Bearbeiten; gültige Änderungen werden beim Schließen gespeichert"),
            "gui.attack-lore" to ("Enabled: <enabled> | Uses: <uses> | Cooldown: <cooldown>s" to "Aktiviert: <enabled> | Einsätze: <uses> | Abklingzeit: <cooldown>s"),
            "mode.every" to ("Every fight" to "Jeder Kampf"),
            "mode.admin" to ("Administrator starts only" to "Nur durch Administratoren"),
            "mode.first" to ("First fight only" to "Nur der erste Kampf"),
        )
        strings.forEach { (key, values) ->
            val prefix =
                if (key.startsWith("gui.") || key.startsWith("mode.") || key.endsWith("-name") || key == "warning") "" else "<prefix> "
            BuildLanguage.registerString(
                BuildLanguage.RegisteredString(
                    FileTypes.EN,
                    "redragon.$key",
                    prefix + values.first
                )
            )
            BuildLanguage.registerString(
                BuildLanguage.RegisteredString(
                    FileTypes.DE,
                    "redragon.$key",
                    prefix + values.second
                )
            )
        }
        val names = mapOf(
            "marked-hunters" to "Marked hunters", "rift-anchors" to "Rift anchors", "breath-sweep" to "Breath sweep",
            "crystals" to "Crystal renewal", "island-wave" to "Island shockwave — jump",
            "effect-areas" to "Falling afflictions", "explosives" to "Timed explosives",
            "monsters" to "Monster reinforcements", "dragon-wave" to "Dragon shockwave — jump",
            "infestation" to "Island infestation", "magnetism" to "Magnetic pull",
            "blue-endermen" to "Rift Endermen", "summon-areas" to "Rift strikes — leave the circles",
            "sanctuary" to "Sanctuary — green circles protect", "rescue-updraft" to "Rescue updraft — cyan circles",
            "healing-pool" to "Healing pool", "enderman-wave" to "Enderman frontal shockwave — sidestep",
        )
        val germanNames = mapOf(
            "marked-hunters" to "Markierte Jäger", "rift-anchors" to "Rissanker", "breath-sweep" to "Atemschwenk",
            "crystals" to "Kristallerneuerung", "island-wave" to "Insel-Schockwelle — springen",
            "effect-areas" to "Fallende Flüche", "explosives" to "Zeitbomben",
            "monsters" to "Monsterverstärkung", "dragon-wave" to "Drachen-Schockwelle — springen",
            "infestation" to "Inselbefall", "magnetism" to "Magnetischer Sog",
            "blue-endermen" to "Riss-Endermen", "summon-areas" to "Rissschläge — Kreise verlassen",
            "sanctuary" to "Zuflucht — grüne Kreise schützen", "rescue-updraft" to "Rettungsaufwind — türkise Kreise",
            "healing-pool" to "Heilquelle", "enderman-wave" to "Frontale Enderman-Schockwelle — ausweichen",
        )
        names.forEach { (id, name) ->
            BuildLanguage.registerString(BuildLanguage.RegisteredString(FileTypes.EN, "redragon.attack.$id", name))
            BuildLanguage.registerString(
                BuildLanguage.RegisteredString(
                    FileTypes.DE,
                    "redragon.attack.$id",
                    germanNames.getValue(id)
                )
            )
        }
    }

    fun text(key: String, sender: CommandSender, params: Map<String, Any> = emptyMap()) =
        LanguageHandler.getMessage("redragon.$key", sender, params)

    fun send(sender: CommandSender, key: String, params: Map<String, Any> = emptyMap()) {
        sender.sendMessage(text(key, sender, params))
    }

    fun plain(key: String, sender: CommandSender, params: Map<String, Any> = emptyMap()): String =
        MessageUtils.strip(text(key, sender, params))

    fun actionBar(player: Player, key: String, params: Map<String, Any> = emptyMap()) {
        player.sendActionBar(text(key, player, params))
    }
}
