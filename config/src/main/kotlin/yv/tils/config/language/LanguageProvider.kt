package yv.tils.config.language

class LanguageProvider {
    companion object {
        fun registerNewString(langKey: LangStrings, translations: Map<FileTypes, String>) {
            for ((fileType, value) in translations) {
                registerString(langKey.key, value, fileType)
            }
        }

        fun registerString(key: String, value: String, file: FileTypes) {
            BuildLanguage.registerString(BuildLanguage.RegisteredString(file, key, value))
        }
    }

    interface LangStrings {
        val key: String
    }

    interface RegisterStrings {
        fun registerStrings()
    }
}
