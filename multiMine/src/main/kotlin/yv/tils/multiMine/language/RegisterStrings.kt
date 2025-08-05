package yv.tils.multiMine.language

import yv.tils.config.language.BuildLanguage
import yv.tils.config.language.FileTypes

class RegisterStrings {
    fun registerStrings() {
        registerNewString(
            "command.multiMine.activate",
            mapOf(
                FileTypes.EN to "<prefix> <white>MultiMine has been <yellow>activated<white>!",
                FileTypes.DE to "<prefix> <white>MultiMine wurde <yellow>aktiviert<white>!",
            )
        )

        registerNewString(
            "command.multiMine.deactivate",
            mapOf(
                FileTypes.EN to "<prefix> <white>MultiMine has been <yellow>deactivated<white>!",
                FileTypes.DE to "<prefix> <white>MultiMine wurde <yellow>deaktiviert<white>!",
            )
        )

        registerNewString(
            "command.multiMine.noBlock.asParam",
            mapOf(
                FileTypes.EN to "<prefix> <white>There was no block specified!",
                FileTypes.DE to "<prefix> <white>Es wurde kein Block angegeben!",
            )
        )

        registerNewString(
            "command.multiMine.noBlock.inHand",
            mapOf(
                FileTypes.EN to "<prefix> <white>There was no block in your hand!",
                FileTypes.DE to "<prefix> <white>Es wurde kein Block in der Hand gefunden!",
            )
        )

        registerNewString(
            "command.multiMine.multiple.console",
            mapOf(
                FileTypes.EN to "<prefix> <white>Multiple blocks can not be added over the console!",
                FileTypes.DE to "<prefix> <white>Es können nicht mehrere Blöcke über die Konsole hinzugefügt werden!",
            )
        )

        registerNewString(
            "command.multiMine.multiple.noContainer",
            mapOf(
                FileTypes.EN to "<prefix> <white>There was no container in your hand!",
                FileTypes.DE to "<prefix> <white>Es wurde kein Container in der Hand gefunden!",
            )
        )

        registerNewString(
            "command.multiMine.multiple.added",
            mapOf(
                FileTypes.EN to "<prefix> <white>Following blocks were added to the MultiMine list: <newline><yellow><blocks>",
                FileTypes.DE to "<prefix> <white>Folgende Blöcke wurden zur MultiMine Liste hinzugefügt: <newline><yellow><blocks>",
            )
        )

        registerNewString(
            "command.multiMine.multiple.removed",
            mapOf(
                FileTypes.EN to "<prefix> <white>Following blocks were removed from the MultiMine list: <newline><yellow><blocks>",
                FileTypes.DE to "<prefix> <white>Folgende Blöcke wurden von der MultiMine Liste entfernt: <newline><yellow><blocks>",
            )
        )

        registerNewString(
            "command.multiMine.block.alreadyInList",
            mapOf(
                FileTypes.EN to "<prefix> <white>The block <yellow><block> <white>is already in the list!",
                FileTypes.DE to "<prefix> <white>Der Block <yellow><block> <white>ist bereits in der Liste!",
            )
        )

        registerNewString(
            "command.multiMine.block.notInList",
            mapOf(
                FileTypes.EN to "<prefix> <white>The block <yellow><block> <white>is not in the list!",
                FileTypes.DE to "<prefix> <white>Der Block <yellow><block> <white>ist nicht in der Liste!",
            )
        )

        registerNewString(
            "command.multiMine.block.added",
            mapOf(
                FileTypes.EN to "<prefix> <white>The block <yellow><block> <white>was added to the list!",
                FileTypes.DE to "<prefix> <white>Der Block <yellow><block> <white>wurde zur Liste hinzugefügt!",
            )
        )

        registerNewString(
            "command.multiMine.block.removed",
            mapOf(
                FileTypes.EN to "<prefix> <white>The block <yellow><block> <white>was removed from the list!",
                FileTypes.DE to "<prefix> <white>Der Block <yellow><block> <white>wurde von der Liste entfernt!",
            )
        )
    }

    private fun registerNewString(langKey: String, translations: Map<FileTypes, String>) {
        for ((fileType, value) in translations) {
            registerString(langKey, value, fileType)
        }
    }

    private fun registerString(key: String, value: String, file: FileTypes) {
        BuildLanguage.registerString(BuildLanguage.RegisteredString(file, key, value))
    }
}
