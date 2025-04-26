package yv.tils.regions.language

import language.BuildLanguage
import language.FileTypes

class RegisterStrings {
    fun registerStrings() {

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
    REGION_CREATE_FAIL_GENERIC("command.regions.create.fail.generic"),
    REGION_CREATE_FAIL_ALREADY_EXISTS("command.regions.create.fail.already.exists"),
    REGION_CREATE_FAIL_TOO_BIG("command.regions.create.fail.too.big"),

    REGION_DELETE_SUCCESS("command.regions.delete.success"),
    REGION_DELETE_FAIL_GENERIC("command.regions.delete.fail.generic"),

    REGION_MEMBERS_INFO("command.regions.members.info"),
    REGION_MEMBER_ADD_SUCCESS("command.regions.members.add.success"),
    REGION_MEMBER_ADD_FAILED("command.regions.members.add.failed"),
    REGION_MEMBER_REMOVE_SUCCESS("command.regions.members.remove.success"),
    REGION_MEMBER_REMOVE_FAILED("command.regions.members.remove.failed"),
    REGION_MEMBER_PROMOTE_SUCCESS("command.regions.members.promote.success"),
    REGION_MEMBER_PROMOTE_FAILED("command.regions.members.promote.failed"),
    REGION_MEMBER_DEMOTE_SUCCESS("command.regions.members.demote.success"),
    REGION_MEMBER_DEMOTE_FAILED("command.regions.members.demote.failed"),

    REGION_INFO_SUCCESS("command.regions.info.success"),
    REGION_INFO_FAIL_GENERIC("command.regions.info.fail.generic"),
    REGION_INFO_FLAGS_NOT_ALLOWED("command.regions.info.flags.not.allowed"),

    REGION_LIST_SUCCESS("command.regions.list.success"),
    REGION_LIST_LINE("command.regions.list.line"),

    REGION_GENERIC_MULTIPLE("regions.generic.multiple"),
    REGION_GENERIC_NONE("regions.generic.none"),

    FLAG_TRIGGER_DENIED("regions.flag.trigger.denied"),
}
