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
    REGION_DELETE_FAIL_NONE("command.regions.delete.fail.none"),
    REGION_DELETE_FAIL_MULTIPLE("command.regions.delete.fail.multiple"),
}
