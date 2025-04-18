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

    REGION_INFO_SUCCESS("command.regions.info.success"),
    REGION_INFO_FAIL_GENERIC("command.regions.info.fail.generic"),
    REGION_INFO_FLAGS_NOT_ALLOWED("command.regions.info.flags.not.allowed"),

    REGION_LIST_SUCCESS("command.regions.list.success"),
    REGION_LIST_LINE("command.regions.list.line"),

    REGION_GENERIC_MULTIPLE("command.regions.generic.multiple"),
    REGION_GENERIC_NONE("command.regions.generic.none"),
}
