package yv.tils.config.language

import yv.tils.utils.data.Data

class LanguageBroadcast {
    companion object {
        fun broadcast(key: String, params: Map<String, Any> = emptyMap()) {
            Data.instance.server.broadcast(
                LanguageHandler.getMessage(
                    key,
                    null,
                    params
                )
            )
        }

        fun broadcast(key: String, permission: String, params: Map<String, Any> = emptyMap()) {
            Data.instance.server.broadcast(
                LanguageHandler.getMessage(
                    key,
                    null,
                    params
                ),
                permission
            )
        }
    }
}
