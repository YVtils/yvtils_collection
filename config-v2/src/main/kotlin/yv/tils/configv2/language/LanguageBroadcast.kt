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

package yv.tils.configv2.language

import yv.tils.utils.modules.Core

class LanguageBroadcast {
    companion object {
        fun broadcast(key: String, params: Map<String, Any> = emptyMap()) {
            Core.instance.server.broadcast(
                LanguageHandler.getMessage(
                    key,
                    null,
                    params
                )
            )
        }

        fun broadcast(key: String, permission: String, params: Map<String, Any> = emptyMap()) {
            for (p in Core.instance.server.onlinePlayers) {
                if (p.hasPermission(permission)) {
                    p.sendMessage(
                        LanguageHandler.getMessage(
                            key,
                            p,
                            params
                        )
                    )
                }
            }
        }
    }
}
