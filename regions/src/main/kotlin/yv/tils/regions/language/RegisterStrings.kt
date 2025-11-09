/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.regions.language

import yv.tils.config.language.BuildLanguage
import yv.tils.config.language.FileTypes

class RegisterStrings {
    fun registerStrings() {
        registerNewString(
            LangStrings.REGION_CREATE_SUCCESS,
            mapOf(
                FileTypes.EN to "<prefix> <green>Region created successfully!",
                FileTypes.DE to "<prefix> <green>Region erfolgreich erstellt!"
            )
        )

        registerNewString(
            LangStrings.REGION_CREATE_FAIL_ALREADY_EXISTS,
            mapOf(
                FileTypes.EN to "<prefix> <red>The region <region> already exists!",
                FileTypes.DE to "<prefix> <red>Die Region <region> existiert bereits!"
            )
        )

        registerNewString(
            LangStrings.REGION_CREATE_FAIL_OVERLAP,
            mapOf(
                FileTypes.EN to "<prefix> <red>Region overlaps with another region!",
                FileTypes.DE to "<prefix> <red>Region überschneidet sich mit einer anderen Region!"
            )
        )

        registerNewString(
            LangStrings.REGION_CREATE_FAIL_SIZE_MAX,
            mapOf(
                FileTypes.EN to "<prefix> <red>Region size exceeds maximum size of <maxSize>!",
                FileTypes.DE to "<prefix> <red>Die Region überschreitet die maximale Größe von <maxSize>!"
            )
        )

        registerNewString(
            LangStrings.REGION_CREATE_FAIL_SIZE_MIN,
            mapOf(
                FileTypes.EN to "<prefix> <red>Region size is below minimum size of <minSize>!",
                FileTypes.DE to "<prefix> <red>Die Region ist kleiner als die minimale Größe von <minSize>!"
            )
        )

        registerNewString(
            LangStrings.REGION_CREATE_FAIL_OWNED_MAX,
            mapOf(
                FileTypes.EN to "<prefix> <red>You already own the maximum number of regions: <maxRegions>!",
                FileTypes.DE to "<prefix> <red>Du besitzt bereits die maximale Anzahl an Regionen: <maxRegions>!"
            )
        )

        registerNewString(
            LangStrings.REGION_DELETE_SUCCESS,
            mapOf(
                FileTypes.EN to "<prefix> <green>The region <region> has been deleted!",
                FileTypes.DE to "<prefix> <green>Die Region <region> wurde gelöscht!"
            )
        )

        registerNewString(
            LangStrings.REGION_MEMBERS_INFO,
            mapOf(
                FileTypes.EN to "<prefix> <white>Region members: <gray><members>",
                FileTypes.DE to "<prefix> <white>Region Mitglieder: <gray><members>"
            )
        )

        registerNewString(
            LangStrings.REGION_MEMBER_ADD_SUCCESS,
            mapOf(
                FileTypes.EN to "<prefix> <green><player> has been added to the region <region> as <role>!",
                FileTypes.DE to "<prefix> <green><player> wurde der Region <region> als <role> hinzugefügt!"
            )
        )

        registerNewString(
            LangStrings.REGION_MEMBER_ADD_FAILED,
            mapOf(
                FileTypes.EN to "<prefix> <red>Failed to add <player> to the region <region>!",
                FileTypes.DE to "<prefix> <red>Fehler beim Hinzufügen von <player> zur Region <region>!"
            )
        )

        registerNewString(
            LangStrings.REGION_MEMBER_ADD_FAILED_MAX_MEMBERSHIPS,
            mapOf(
                FileTypes.EN to "<prefix> <red>The player you are trying to add already has the maximum number of region memberships: <maxRegions>!",
                FileTypes.DE to "<prefix> <red>Der Spieler, den du hinzufügen möchtest, hat bereits die maximale Anzahl an Regionsmitgliedschaften: <maxRegions>!"
            )
        )

        registerNewString(
            LangStrings.REGION_MEMBER_ADD_FAILED_MAX_MEMBERS,
            mapOf(
                FileTypes.EN to "<prefix> <red>The region already has the maximum number of members: <maxRegions>!",
                FileTypes.DE to "<prefix> <red>Die Region hat bereits die maximale Anzahl an Mitgliedern: <maxRegions>!"
            )
        )

        registerNewString(
            LangStrings.REGION_MEMBER_REMOVE_SUCCESS,
            mapOf(
                FileTypes.EN to "<prefix> <green><player> has been removed from the region <region>!",
                FileTypes.DE to "<prefix> <green><player> wurde aus der Region <region> entfernt!"
            )
        )

        registerNewString(
            LangStrings.REGION_MEMBER_REMOVE_FAILED,
            mapOf(
                FileTypes.EN to "<prefix> <red>Failed to remove <player> from the region <region>!",
                FileTypes.DE to "<prefix> <red>Fehler beim Entfernen von <player> aus der Region <region>!"
            )
        )

        registerNewString(
            LangStrings.REGION_MEMBER_ROLE_SUCCESS,
            mapOf(
                FileTypes.EN to "<prefix> <green><player>'s role has been changed to <role> in the region <region>!",
                FileTypes.DE to "<prefix> <green>Die Rolle von <player> wurde in der Region <region> auf <role> geändert!"
            )
        )

        registerNewString(
            LangStrings.REGION_MEMBER_ROLE_FAILED,
            mapOf(
                FileTypes.EN to "<prefix> <red>Failed to change <player>'s role in the region <region>!",
                FileTypes.DE to "<prefix> <red>Fehler beim Ändern der Rolle von <player> in der Region <region>!"
            )
        )

        registerNewString(
            LangStrings.REGION_INFO_SUCCESS,
            mapOf(
                FileTypes.EN to "<prefix> <white>Region info: <newline><yellow>Name: <gray><name> <newline><yellow>World: <gray><world> <newline><yellow>Location 1: <gray><location1> <newline><yellow>Location 2: <gray><location2> <newline><yellow>Owner: <gray><owner> <newline><yellow>Role: <gray><role> <newline><yellow>Members: <gray><members> <newline><yellow>Created: <gray><created> <newline><yellow>Flags: <gray><flags>",
                FileTypes.DE to "<prefix> <white>Regionsinfo: <newline><yellow>Name: <gray><name> <newline><yellow>Welt: <gray><world> <newline><yellow>Standort 1: <gray><location1> <newline><yellow>Standort 2: <gray><location2> <newline><yellow>Besitzer: <gray><owner> <newline><yellow>Rolle: <gray><role> <newline><yellow>Mitglieder: <gray><members> <newline><yellow>Erstellt: <gray><created> <newline><yellow>Flags: <gray><flags>"
            )
        )

        registerNewString(
            LangStrings.REGION_INFO_FAIL_GENERIC,
            mapOf(
                FileTypes.EN to "<prefix> <red>Failed to retrieve region info!",
                FileTypes.DE to "<prefix> <red>Fehler beim Abrufen der Regionsinformationen!"
            )
        )

        registerNewString(
            LangStrings.REGION_INFO_FLAGS_NOT_ALLOWED,
            mapOf(
                FileTypes.EN to "<prefix> <red>You are not allowed to view flags in this region!",
                FileTypes.DE to "<prefix> <red>Du darfst die Flags in dieser Region nicht anzeigen!"
            )
        )

        registerNewString(
            LangStrings.REGION_LIST_SUCCESS,
            mapOf(
                FileTypes.EN to "<prefix> <white>Region list: <newline><gray><lines>",
                FileTypes.DE to "<prefix> <white>Regionsliste: <newline><gray><lines>"
            )
        )

        registerNewString(
            LangStrings.REGION_LIST_LINE,
            mapOf(
                FileTypes.EN to "<white>Region: <gray><name>, <white>Owner: <gray><owner>, <white>ID: <gray><id>",
                FileTypes.DE to "<white>Region: <gray><name>, <white>Besitzer: <gray><owner>, <white>ID: <gray><id>"
            )
        )

        registerNewString(
            LangStrings.REGION_FLAG_CHANGE_SUCCESS,
            mapOf(
                FileTypes.EN to "<prefix> <green>Flag <flag> changed to <value> in region <region>!",
                FileTypes.DE to "<prefix> <green>Flagge <flag> in Region <region> auf <value> geändert!"
            )
        )

        registerNewString(
            LangStrings.REGION_FLAG_CHANGE_FAIL_INVALID,
            mapOf(
                FileTypes.EN to "<prefix> <red>Invalid flag!",
                FileTypes.DE to "<prefix> <red>Ungültige Flagge!"
            )
        )

        registerNewString(
            LangStrings.REGION_FLAG_CHANGE_FAIL_NO_PERMISSION,
            mapOf(
                FileTypes.EN to "<prefix> <red>You do not have permission to change this flag!",
                FileTypes.DE to "<prefix> <red>Du hast keine Berechtigung, diese Flagge zu ändern!"
            )
        )

        registerNewString(
            LangStrings.REGION_GENERIC_MULTIPLE,
            mapOf(
                FileTypes.EN to "<prefix> <white>Multiple regions found: <gray><regions>",
                FileTypes.DE to "<prefix> <white>Mehrere Regionen gefunden: <gray><regions>"
            )
        )

        registerNewString(
            LangStrings.REGION_GENERIC_NONE,
            mapOf(
                FileTypes.EN to "<prefix> <red>No regions found!",
                FileTypes.DE to "<prefix> <red>Keine Regionen gefunden!"
            )
        )

        registerNewString(
            LangStrings.FLAG_TRIGGER_DENIED,
            mapOf(
                FileTypes.EN to "<prefix> <red>You are not allowed to do this (<flag>) here!",
                FileTypes.DE to "<prefix> <red>Du darfst dies (<flag>) hier nicht tun!"
            )
        )
    }

    private fun registerNewString(langKey: LangStrings, translations: Map<FileTypes, String>) {
        for ((fileType, value) in translations) {
            registerString(langKey.key, value, fileType)
        }
    }

    private fun registerString(key: String, value: String, file: FileTypes) {
        BuildLanguage.registerString(BuildLanguage.RegisteredString(file, key, value))
    }
}

enum class LangStrings(val key: String) {
    REGION_CREATE_SUCCESS("command.regions.create.success"),
    REGION_CREATE_FAIL_ALREADY_EXISTS("command.regions.create.fail.already.exists"),
    REGION_CREATE_FAIL_OVERLAP("command.regions.create.fail.overlap"),
    REGION_CREATE_FAIL_SIZE_MAX("command.regions.create.fail.size.max"),
    REGION_CREATE_FAIL_SIZE_MIN("command.regions.create.fail.size.min"),
    REGION_CREATE_FAIL_OWNED_MAX("command.regions.create.fail.owned.max"),

    REGION_DELETE_SUCCESS("command.regions.delete.success"),

    REGION_MEMBERS_INFO("command.regions.members.info"),
    REGION_MEMBER_ADD_SUCCESS("command.regions.members.add.success"),
    REGION_MEMBER_ADD_FAILED("command.regions.members.add.failed.generic"),
    REGION_MEMBER_ADD_FAILED_MAX_MEMBERSHIPS("command.regions.members.add.failed.max.memberships"),
    REGION_MEMBER_ADD_FAILED_MAX_MEMBERS("command.regions.members.add.failed.max.members"),
    REGION_MEMBER_REMOVE_SUCCESS("command.regions.members.remove.success"),
    REGION_MEMBER_REMOVE_FAILED("command.regions.members.remove.failed"),
    REGION_MEMBER_ROLE_SUCCESS("command.regions.members.role.success"),
    REGION_MEMBER_ROLE_FAILED("command.regions.members.role.failed"),

    REGION_INFO_SUCCESS("command.regions.info.success"),
    REGION_INFO_FAIL_GENERIC("command.regions.info.fail.generic"),
    REGION_INFO_FLAGS_NOT_ALLOWED("command.regions.info.flags.not.allowed"),

    REGION_LIST_SUCCESS("command.regions.list.success"),
    REGION_LIST_LINE("command.regions.list.line"),

    REGION_FLAG_CHANGE_SUCCESS("command.regions.flags.change.success"),
    REGION_FLAG_CHANGE_FAIL_INVALID("command.regions.flags.change.fail.invalid"),
    REGION_FLAG_CHANGE_FAIL_REGION_NOT_FOUND("command.regions.flags.change.fail.region.not.found"),
    REGION_FLAG_CHANGE_FAIL_NO_PERMISSION("command.regions.flags.change.fail.no.permission"),

    REGION_GENERIC_MULTIPLE("regions.generic.multiple"),
    REGION_GENERIC_NONE("regions.generic.none"),

    FLAG_TRIGGER_DENIED("regions.flag.trigger.denied"),
}
