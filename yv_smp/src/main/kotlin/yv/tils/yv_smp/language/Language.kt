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
    )
     ;
}
