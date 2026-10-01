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

package yv.tils.regionsv2.language

import yv.tils.configv2.language.LanguageProvider

class RegisterStrings : LanguageProvider.RegisterStrings {
    override fun registerStrings() {
        LangStrings.entries.forEach { string ->
            LanguageProvider.registerString(string)
        }
    }
}
