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

package yv.tils.yv_smp.language

import yv.tils.configv2.language.FileTypes
import yv.tils.configv2.language.LanguageProvider

enum class LangStrings(override val key: String, override val translations: Map<FileTypes, String>) : LanguageProvider.LangStrings {
    START_TITLE_HEAL(
        "command.start.heal.title",
        mapOf(
            FileTypes.EN to "<yellow>Healing Players at Pokecenter</yellow>",
            FileTypes.DE to "<yellow>Heile Spieler im Pokecenter</yellow>",
        )
    ),
    START_DESC_HEAL(
        "command.start.heal.description",
        mapOf(
            FileTypes.EN to "<gradient:#FF0000:#FF6666:#FFAAAA>❤ ❤ ❤ Health Restored ❤ ❤ ❤</gradient>",
            FileTypes.DE to "<gradient:#FF0000:#FF6666:#FFAAAA>❤ ❤ ❤ Leben wiederhergestellt ❤ ❤ ❤</gradient>",
        )
    ),

    START_PLACEHOLDER_BOOTING(
        "command.start.placeholder.booting",
        mapOf(
            FileTypes.EN to "Booting",
            FileTypes.DE to "Hochfahren",
        )
    ),
    START_PLACEHOLDER_ONLINE(
        "command.start.placeholder.online",
        mapOf(
            FileTypes.EN to "ONLINE",
            FileTypes.DE to "ONLINE",
        )
    ),
    START_PLACEHOLDER_READY(
        "command.start.placeholder.ready",
        mapOf(
            FileTypes.EN to "System Ready",
            FileTypes.DE to "System Bereit",
        )
    ),

    START_DESC_PREPARE_1(
        "command.start.prepare.description.1",
        mapOf(
            FileTypes.EN to "<gradient:#00FF00:#66FF66:#AAFFAA>◈ Initializing Systems ◈</gradient>",
            FileTypes.DE to "<gradient:#00FF00:#66FF66:#AAFFAA>◈ Systeme Initialisieren ◈</gradient>",
        )
    ),
    START_DESC_PREPARE_2(
        "command.start.prepare.description.2",
        mapOf(
            FileTypes.EN to "<gradient:#00FF00:#66FF66:#AAFFAA>◈ Loading Protocols ◈</gradient>",
            FileTypes.DE to "<gradient:#00FF00:#66FF66:#AAFFAA>◈ Protokolle Laden ◈</gradient>",
        )
    ),
    START_DESC_PREPARE_3(
        "command.start.prepare.description.3",
        mapOf(
            FileTypes.EN to "<gradient:#00FF00:#66FF66:#AAFFAA>◈ Calibrating Sensors ◈</gradient>",
            FileTypes.DE to "<gradient:#00FF00:#66FF66:#AAFFAA>◈ Sensoren Kalibrieren ◈</gradient>",
        )
    ),
    START_DESC_PREPARE_4(
        "command.start.prepare.description.4",
        mapOf(
            FileTypes.EN to "<gradient:#00FF00:#66FF66:#AAFFAA>◈ Activating Modules ◈</gradient>",
            FileTypes.DE to "<gradient:#00FF00:#66FF66:#AAFFAA>◈ Module Aktivieren ◈</gradient>",
        )
    ),
    START_DESC_PREPARE_5(
        "command.start.prepare.description.5",
        mapOf(
            FileTypes.EN to "<gradient:#00FF00:#66FF66:#AAFFAA>◈ All Systems Nominal ◈</gradient>",
            FileTypes.DE to "<gradient:#00FF00:#66FF66:#AAFFAA>◈ Alle Systeme Nominal ◈</gradient>",
        )
    ),
    START_DESC_PREPARE_6(
        "command.start.prepare.description.6",
        mapOf(
            FileTypes.EN to "<gradient:#00FF00:#66FF66:#AAFFAA>◈ Engage Sequence ◈</gradient>",
            FileTypes.DE to "<gradient:#00FF00:#66FF66:#AAFFAA>◈ Sequenz Starten ◈</gradient>",
        )
    ),
    START_TITLE_ITEMCLEAR(
        "command.start.item-clear.title",
        mapOf(
            FileTypes.EN to "<red>Burning Items, Armor, and Levels</red>",
            FileTypes.DE to "<red>Verbrenne Items, Rüstung und Level</red>",
        )
    ),
    START_DESC_ITEMCLEAR(
        "command.start.item-clear.description",
        mapOf(
            FileTypes.EN to "<gradient:#ffaa00:#ffff00>⚡ Inventory Cleared ⚡</gradient>",
            FileTypes.DE to "<gradient:#ffaa00:#ffff00>⚡ Inventar Geleert ⚡</gradient>",
        )
    ),
    START_TITLE_DEATH(
        "command.start.death-visualizer.title",
        mapOf(
            FileTypes.EN to "<light_purple>Pixelating Death Counter</light_purple>",
            FileTypes.DE to "<light_purple>Pixeln des Todescounter</light_purple>",
        )
    ),
    START_DESC_DEATH(
        "command.start.death-visualizer.description",
        mapOf(
            FileTypes.EN to "<gradient:#000000:#333333:#666666>☠ ⚰ Deaths Recorded ⚰ ☠</gradient>",
            FileTypes.DE to "<gradient:#000000:#333333:#666666>☠ ⚰ Tode Erfasst ⚰ ☠</gradient>",
        )
    ),
    START_TITLE_BORDER(
        "command.start.border.title",
        mapOf(
            FileTypes.EN to "<gold>Building Additional Chunks</gold>",
            FileTypes.DE to "<gold>Baue Zusätzliche Chunks</gold>",
        )
    ),
    START_DESC_BORDER(
        "command.start.border.description",
        mapOf(
            FileTypes.EN to "<gradient:#00ffff:#0088ff>⬢ Border Expanding ⬢</gradient>",
            FileTypes.DE to "<gradient:#00ffff:#0088ff>⬢ Border Erweitern ⬢</gradient>",
        )
    ),
    START_TITLE_FINAL(
        "command.start.final.phase1.title",
        mapOf(
            FileTypes.EN to "<blue>Releasing Players</blue>",
            FileTypes.DE to "<blue>Spieler Freilassen</blue>",
        )
    ),
    START_DESC_FINAL(
        "command.start.final.phase1.description",
        mapOf(
            FileTypes.EN to "<gradient:#FFFF00:#FFAA00:#FF0000>◈ ◈ Final Phase ◈ ◈</gradient>",
            FileTypes.DE to "<gradient:#FFFF00:#FFAA00:#FF0000>◈ ◈ Finale Phase ◈ ◈</gradient>",
        )
    ),
    START_TITLE_FINAL_2(
        "command.start.final.phase2.title",
        mapOf(
            FileTypes.EN to "<gradient:#FFD700:#FFAA00:#FFD700>✦ ✦ ✦ READY ✦ ✦ ✦</gradient>",
            FileTypes.DE to "<gradient:#FFD700:#FFAA00:#FFD700>✦ ✦ ✦ BEREIT ✦ ✦ ✦</gradient>",
        )
    ),
    START_DESC_FINAL_2(
        "command.start.final.phase2.description",
        mapOf(
            FileTypes.EN to "<gradient:#00FF00:#00FFAA:#00FFFF>The Adventure Begins...</gradient>",
            FileTypes.DE to "<gradient:#00FF00:#00FFAA:#00FFFF>Das Abenteuer Beginnt...</gradient>",
        )
    ),

    // Music Command
    MUSIC_UNAVAILABLE(
        "command.music.unavailable",
        mapOf(
            FileTypes.EN to "<red>Music system is not available. Install Simple Voice Chat!</red>",
            FileTypes.DE to "<red>Musiksystem ist nicht verfügbar. Installiere Simple Voice Chat!</red>",
        )
    ),
    MUSIC_PLAY_SUCCESS(
        "command.music.play.success",
        mapOf(
            FileTypes.EN to "<green>Playing audio: <yellow><url></yellow></green>",
            FileTypes.DE to "<green>Spiele Audio ab: <yellow><url></yellow></green>",
        )
    ),
    MUSIC_PLAY_FAILED(
        "command.music.play.failed",
        mapOf(
            FileTypes.EN to "<red>Failed to play audio. Check console for errors.</red>",
            FileTypes.DE to "<red>Fehler beim Abspielen. Prüfe die Konsole für Fehler.</red>",
        )
    ),
    MUSIC_STOP_SUCCESS(
        "command.music.stop.success",
        mapOf(
            FileTypes.EN to "<green>Audio stopped.</green>",
            FileTypes.DE to "<green>Audio gestoppt.</green>",
        )
    ),
    MUSIC_STOP_NOT_PLAYING(
        "command.music.stop.not-playing",
        mapOf(
            FileTypes.EN to "<yellow>No audio is currently playing.</yellow>",
            FileTypes.DE to "<yellow>Es wird derzeit kein Audio abgespielt.</yellow>",
        )
    ),
    MUSIC_BROADCAST_SUCCESS(
        "command.music.broadcast.success",
        mapOf(
            FileTypes.EN to "<green>Broadcasting audio to all players: <yellow><url></yellow></green>",
            FileTypes.DE to "<green>Sende Audio an alle Spieler: <yellow><url></yellow></green>",
        )
    ),
    MUSIC_BROADCAST_FAILED(
        "command.music.broadcast.failed",
        mapOf(
            FileTypes.EN to "<red>Failed to broadcast audio.</red>",
            FileTypes.DE to "<red>Fehler beim Senden des Audios.</red>",
        )
    ),
    MUSIC_FADE_SUCCESS(
        "command.music.fade.success",
        mapOf(
            FileTypes.EN to "<green>Fading out audio over <seconds> seconds...</green>",
            FileTypes.DE to "<green>Audio wird über <seconds> Sekunden ausgeblendet...</green>",
        )
    ),
    MUSIC_STATUS_TITLE(
        "command.music.status.title",
        mapOf(
            FileTypes.EN to "<gold>Music System Status</gold>",
            FileTypes.DE to "<gold>Musiksystem Status</gold>",
        )
    ),
    MUSIC_STATUS_AVAILABLE(
        "command.music.status.available",
        mapOf(
            FileTypes.EN to "<gray>- Available: <status></gray>",
            FileTypes.DE to "<gray>- Verfügbar: <status></gray>",
        )
    ),
    MUSIC_STATUS_PLAYING(
        "command.music.status.playing",
        mapOf(
            FileTypes.EN to "<gray>- Currently Playing: <status></gray>",
            FileTypes.DE to "<gray>- Spielt gerade ab: <status></gray>",
        )
    ),
    MUSIC_STATUS_YES(
        "command.music.status.yes",
        mapOf(
            FileTypes.EN to "<green>Yes</green>",
            FileTypes.DE to "<green>Ja</green>",
        )
    ),
    MUSIC_STATUS_NO(
        "command.music.status.no",
        mapOf(
            FileTypes.EN to "<red>No</red>",
            FileTypes.DE to "<red>Nein</red>",
        )
    ),
    START_COMMAND_SUCCESS(
        "command.start.success",
        mapOf(
            FileTypes.EN to "<green>SMP is starting...</green>",
            FileTypes.DE to "<green>SMP wird gestartet...</green>",
        )
    ),
    START_ALREADY_RUNNING(
        "command.start.already-running",
        mapOf(
            FileTypes.EN to "<red>The SMP start sequence is already running!</red>",
            FileTypes.DE to "<red>Die SMP-Startsequenz läuft bereits!</red>",
        )
    ),
    START_NO_PLAYERS(
        "command.start.no-players",
        mapOf(
            FileTypes.EN to "<red>Cannot start - no players are online.</red>",
            FileTypes.DE to "<red>Start nicht möglich - es sind keine Spieler online.</red>",
        )
    ),
    START_STOP_SUCCESS(
        "command.start.stop.success",
        mapOf(
            FileTypes.EN to "<green>The SMP start sequence has been aborted.</green>",
            FileTypes.DE to "<green>Die SMP-Startsequenz wurde abgebrochen.</green>",
        )
    ),
    START_STOP_NOT_RUNNING(
        "command.start.stop.not-running",
        mapOf(
            FileTypes.EN to "<yellow>There is no start sequence currently running.</yellow>",
            FileTypes.DE to "<yellow>Es läuft derzeit keine Startsequenz.</yellow>",
        )
    ),
    START_NOT_ENOUGH_PLAYERS(
        "command.start.not-enough-players",
        mapOf(
            FileTypes.EN to "<red>Cannot start - need at least <required> player(s) online" +
                " (currently <online>).</red>",
            FileTypes.DE to "<red>Start nicht möglich - es werden mindestens <required> Spieler benötigt" +
                " (aktuell <online>).</red>",
        )
    ),
    START_WARMUP_TITLE(
        "command.start.warmup.title",
        mapOf(
            FileTypes.EN to "<gradient:#ffaa00:#ff5500><bold><seconds></bold></gradient>",
            FileTypes.DE to "<gradient:#ffaa00:#ff5500><bold><seconds></bold></gradient>",
        )
    ),
    START_WARMUP_SUBTITLE(
        "command.start.warmup.subtitle",
        mapOf(
            FileTypes.EN to "<yellow>Get ready - the SMP is starting!</yellow>",
            FileTypes.DE to "<yellow>Mach dich bereit - das SMP startet!</yellow>",
        )
    ),

    // Setup Command
    SETUP_BORDER_SUCCESS(
        "command.setup.border.success",
        mapOf(
            FileTypes.EN to "<green>World border set to <yellow><size></yellow> blocks" +
                " (transition: <yellow><time></yellow>s).</green>",
            FileTypes.DE to "<green>Weltgrenze auf <yellow><size></yellow> Blöcke gesetzt" +
                " (Übergang: <yellow><time></yellow>s).</green>",
        )
    ),
    SETUP_BORDERCENTER_SUCCESS(
        "command.setup.bordercenter.success",
        mapOf(
            FileTypes.EN to "<green>World border center set to <yellow><x>, <z></yellow>.</green>",
            FileTypes.DE to "<green>Zentrum der Weltgrenze auf <yellow><x>, <z></yellow> gesetzt.</green>",
        )
    ),
    SETUP_SPAWN_SUCCESS(
        "command.setup.spawn.success",
        mapOf(
            FileTypes.EN to "<green>World spawn set to your current location.</green>",
            FileTypes.DE to "<green>Weltspawn auf deinen aktuellen Standort gesetzt.</green>",
        )
    ),
    SETUP_RESCAN_SUCCESS(
        "command.setup.rescan.success",
        mapOf(
            FileTypes.EN to "<green>Island edge cache cleared - it will be rescanned on next use.</green>",
            FileTypes.DE to "<green>Insel-Rand-Cache geleert - wird beim nächsten Gebrauch neu gescannt.</green>",
        )
    ),
    SETUP_ALL_SUCCESS(
        "command.setup.all.success",
        mapOf(
            FileTypes.EN to "<green>SMP setup complete: border reset, spawn recentered, island cache cleared.</green>",
            FileTypes.DE to "<green>SMP-Setup abgeschlossen: Grenze zurückgesetzt, Spawn neu zentriert," +
                " Insel-Cache geleert.</green>",
        )
    ),
    SETUP_STATUS_TITLE(
        "command.setup.status.title",
        mapOf(
            FileTypes.EN to "<gold>YV SMP Setup Status</gold>",
            FileTypes.DE to "<gold>YV SMP Setup-Status</gold>",
        )
    ),
    SETUP_STATUS_BORDER(
        "command.setup.status.border",
        mapOf(
            FileTypes.EN to "<gray>- Border (<world>): <status></gray>",
            FileTypes.DE to "<gray>- Grenze (<world>): <status></gray>",
        )
    ),
    SETUP_STATUS_SPAWN(
        "command.setup.status.spawn",
        mapOf(
            FileTypes.EN to "<gray>- Spawn (<world>): <x>, <y>, <z></gray>",
            FileTypes.DE to "<gray>- Spawn (<world>): <x>, <y>, <z></gray>",
        )
    ),
    SETUP_STATUS_RUNNING(
        "command.setup.status.running",
        mapOf(
            FileTypes.EN to "<gray>- Start sequence running: <status></gray>",
            FileTypes.DE to "<gray>- Startsequenz läuft: <status></gray>",
        )
    ),
    SETUP_PLAYER_ONLY(
        "command.setup.player-only",
        mapOf(
            FileTypes.EN to "<red>This command can only be used by a player.</red>",
            FileTypes.DE to "<red>Dieser Befehl kann nur von einem Spieler verwendet werden.</red>",
        )
    ),

    // GUI - shared
    GUI_NO_PERMISSION(
        "gui.yvsmp.no-permission",
        mapOf(
            FileTypes.EN to "<red>You don't have permission to do that.</red>",
            FileTypes.DE to "<red>Du hast keine Berechtigung dafür.</red>",
        )
    ),
    GUI_ITEM_CLOSE_NAME(
        "gui.yvsmp.item.close.name",
        mapOf(
            FileTypes.EN to "<red>Close</red>",
            FileTypes.DE to "<red>Schließen</red>",
        )
    ),
    GUI_CONFIRM_NO_NAME(
        "gui.yvsmp.confirm.no",
        mapOf(
            FileTypes.EN to "<red><bold>Cancel</bold></red>",
            FileTypes.DE to "<red><bold>Abbrechen</bold></red>",
        )
    ),

    // GUI - main menu
    GUI_TITLE_MAIN(
        "gui.yvsmp.main.title",
        mapOf(
            FileTypes.EN to "<gold><bold>YV SMP Control</bold></gold>",
            FileTypes.DE to "<gold><bold>YV SMP Steuerung</bold></gold>",
        )
    ),
    GUI_ITEM_START_NAME(
        "gui.yvsmp.item.start.name",
        mapOf(
            FileTypes.EN to "<green><bold>Start SMP</bold></green>",
            FileTypes.DE to "<green><bold>SMP Starten</bold></green>",
        )
    ),
    GUI_ITEM_START_LORE(
        "gui.yvsmp.item.start.lore",
        mapOf(
            FileTypes.EN to "<gray>Click to begin the cinematic start sequence.</gray>",
            FileTypes.DE to "<gray>Klicke, um die Startsequenz zu beginnen.</gray>",
        )
    ),
    GUI_ITEM_START_RUNNING_LORE(
        "gui.yvsmp.item.start.running",
        mapOf(
            FileTypes.EN to "<yellow>A start sequence is already running!</yellow>",
            FileTypes.DE to "<yellow>Es läuft bereits eine Startsequenz!</yellow>",
        )
    ),
    GUI_ITEM_STOP_NAME(
        "gui.yvsmp.item.stop.name",
        mapOf(
            FileTypes.EN to "<red><bold>Stop Sequence</bold></red>",
            FileTypes.DE to "<red><bold>Sequenz Stoppen</bold></red>",
        )
    ),
    GUI_ITEM_STOP_LORE(
        "gui.yvsmp.item.stop.lore",
        mapOf(
            FileTypes.EN to "<gray>Click to abort the running start sequence.</gray>",
            FileTypes.DE to "<gray>Klicke, um die laufende Startsequenz abzubrechen.</gray>",
        )
    ),
    GUI_ITEM_STOP_IDLE_LORE(
        "gui.yvsmp.item.stop.idle",
        mapOf(
            FileTypes.EN to "<gray>No sequence is currently running.</gray>",
            FileTypes.DE to "<gray>Es läuft derzeit keine Sequenz.</gray>",
        )
    ),
    GUI_ITEM_SETUP_NAME(
        "gui.yvsmp.item.setup.name",
        mapOf(
            FileTypes.EN to "<aqua><bold>Setup</bold></aqua>",
            FileTypes.DE to "<aqua><bold>Setup</bold></aqua>",
        )
    ),
    GUI_ITEM_SETUP_LORE(
        "gui.yvsmp.item.setup.lore",
        mapOf(
            FileTypes.EN to "<gray>Configure the world border, spawn and more.</gray>",
            FileTypes.DE to "<gray>Konfiguriere Weltgrenze, Spawn und mehr.</gray>",
        )
    ),
    GUI_ITEM_STATUS_NAME(
        "gui.yvsmp.item.status.name",
        mapOf(
            FileTypes.EN to "<gold>Status</gold>",
            FileTypes.DE to "<gold>Status</gold>",
        )
    ),
    GUI_STATUS_LORE_PLAYERS(
        "gui.yvsmp.item.status.players",
        mapOf(
            FileTypes.EN to "<gray>Online: <white><count></white></gray>",
            FileTypes.DE to "<gray>Online: <white><count></white></gray>",
        )
    ),
    GUI_STATUS_LORE_RUNNING(
        "gui.yvsmp.item.status.running",
        mapOf(
            FileTypes.EN to "<gray>Running: <status></gray>",
            FileTypes.DE to "<gray>Läuft: <status></gray>",
        )
    ),
    GUI_STATUS_LORE_BORDER(
        "gui.yvsmp.item.status.border",
        mapOf(
            FileTypes.EN to "<gray>Border: <white><status></white></gray>",
            FileTypes.DE to "<gray>Grenze: <white><status></white></gray>",
        )
    ),
    GUI_STATUS_LORE_SPAWN(
        "gui.yvsmp.item.status.spawn",
        mapOf(
            FileTypes.EN to "<gray>Spawn: <white><x>, <y>, <z></white></gray>",
            FileTypes.DE to "<gray>Spawn: <white><x>, <y>, <z></white></gray>",
        )
    ),

    // GUI - confirm start
    GUI_TITLE_CONFIRM_START(
        "gui.yvsmp.confirm-start.title",
        mapOf(
            FileTypes.EN to "<red><bold>Confirm Start</bold></red>",
            FileTypes.DE to "<red><bold>Start Bestätigen</bold></red>",
        )
    ),
    GUI_CONFIRM_START_WARNING_1(
        "gui.yvsmp.confirm-start.warning1",
        mapOf(
            FileTypes.EN to "<yellow>This will teleport all <white><count></white> online player(s)</yellow>",
            FileTypes.DE to "<yellow>Dies teleportiert alle <white><count></white> Online-Spieler</yellow>",
        )
    ),
    GUI_CONFIRM_START_WARNING_2(
        "gui.yvsmp.confirm-start.warning2",
        mapOf(
            FileTypes.EN to "<yellow>into spectator mode and clear their inventories!</yellow>",
            FileTypes.DE to "<yellow>in den Zuschauermodus und leert ihr Inventar!</yellow>",
        )
    ),

    // GUI - setup menu
    GUI_TITLE_SETUP(
        "gui.yvsmp.setup.title",
        mapOf(
            FileTypes.EN to "<aqua><bold>YV SMP Setup</bold></aqua>",
            FileTypes.DE to "<aqua><bold>YV SMP Setup</bold></aqua>",
        )
    ),
    GUI_ITEM_BORDER_SIZE_NAME(
        "gui.yvsmp.item.border-size.name",
        mapOf(
            FileTypes.EN to "<aqua>Border Size</aqua>",
            FileTypes.DE to "<aqua>Grenzgröße</aqua>",
        )
    ),
    GUI_ITEM_BORDER_SIZE_LORE(
        "gui.yvsmp.item.border-size.lore",
        mapOf(
            FileTypes.EN to "<gray>Current: <white><size></white> blocks</gray>",
            FileTypes.DE to "<gray>Aktuell: <white><size></white> Blöcke</gray>",
        )
    ),
    GUI_ITEM_BORDERCENTER_NAME(
        "gui.yvsmp.item.bordercenter.name",
        mapOf(
            FileTypes.EN to "<aqua>Border Center</aqua>",
            FileTypes.DE to "<aqua>Grenzzentrum</aqua>",
        )
    ),
    GUI_ITEM_BORDERCENTER_LORE(
        "gui.yvsmp.item.bordercenter.lore",
        mapOf(
            FileTypes.EN to "<gray>Click to center the border on your location.</gray>",
            FileTypes.DE to "<gray>Klicke, um die Grenze auf deinen Standort zu zentrieren.</gray>",
        )
    ),
    GUI_ITEM_SPAWN_NAME(
        "gui.yvsmp.item.spawn.name",
        mapOf(
            FileTypes.EN to "<aqua>World Spawn</aqua>",
            FileTypes.DE to "<aqua>Weltspawn</aqua>",
        )
    ),
    GUI_ITEM_SPAWN_LORE(
        "gui.yvsmp.item.spawn.lore",
        mapOf(
            FileTypes.EN to "<gray>Click to set spawn to your location.</gray>",
            FileTypes.DE to "<gray>Klicke, um den Spawn auf deinen Standort zu setzen.</gray>",
        )
    ),
    GUI_ITEM_RESCAN_NAME(
        "gui.yvsmp.item.rescan.name",
        mapOf(
            FileTypes.EN to "<aqua>Rescan Island</aqua>",
            FileTypes.DE to "<aqua>Insel Neu Scannen</aqua>",
        )
    ),
    GUI_ITEM_RESCAN_LORE(
        "gui.yvsmp.item.rescan.lore",
        mapOf(
            FileTypes.EN to "<gray>Click to clear the cached island edge scan.</gray>",
            FileTypes.DE to "<gray>Klicke, um den Insel-Rand-Cache zu leeren.</gray>",
        )
    ),
    GUI_ITEM_SETUP_ALL_NAME(
        "gui.yvsmp.item.setup-all.name",
        mapOf(
            FileTypes.EN to "<red>Reset Everything</red>",
            FileTypes.DE to "<red>Alles Zurücksetzen</red>",
        )
    ),
    GUI_ITEM_SETUP_ALL_LORE(
        "gui.yvsmp.item.setup-all.lore",
        mapOf(
            FileTypes.EN to "<gray>Click to reset the border & island cache.</gray>",
            FileTypes.DE to "<gray>Klicke, um Grenze & Insel-Cache zurückzusetzen.</gray>",
        )
    ),

    // GUI - confirm setup-all
    GUI_TITLE_CONFIRM_SETUP_ALL(
        "gui.yvsmp.confirm-setup-all.title",
        mapOf(
            FileTypes.EN to "<red><bold>Confirm Reset</bold></red>",
            FileTypes.DE to "<red><bold>Zurücksetzen Bestätigen</bold></red>",
        )
    ),
    GUI_CONFIRM_SETUP_ALL_WARNING_1(
        "gui.yvsmp.confirm-setup-all.warning1",
        mapOf(
            FileTypes.EN to "<yellow>This resets the world border to a default size</yellow>",
            FileTypes.DE to "<yellow>Dies setzt die Weltgrenze auf eine Standardgröße zurück</yellow>",
        )
    ),
    GUI_CONFIRM_SETUP_ALL_WARNING_2(
        "gui.yvsmp.confirm-setup-all.warning2",
        mapOf(
            FileTypes.EN to "<yellow>centered on spawn, and clears the island scan cache.</yellow>",
            FileTypes.DE to "<yellow>zentriert auf Spawn, und leert den Insel-Scan-Cache.</yellow>",
        )
    ),

    // GUI - border size editor (anvil)
    GUI_TITLE_BORDER_EDITOR(
        "gui.yvsmp.border-editor.title",
        mapOf(
            FileTypes.EN to "<gold>Set Border Size</gold>",
            FileTypes.DE to "<gold>Grenzgröße Setzen</gold>",
        )
    ),
    GUI_BORDER_EDITOR_INVALID(
        "gui.yvsmp.border-editor.invalid",
        mapOf(
            FileTypes.EN to "<red>Please enter a valid number greater than 0.</red>",
            FileTypes.DE to "<red>Bitte gib eine gültige Zahl größer als 0 ein.</red>",
        )
    ),
     ;
}
