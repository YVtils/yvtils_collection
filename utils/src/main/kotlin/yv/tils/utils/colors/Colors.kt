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

package yv.tils.utils.colors

import java.awt.Color

class ColorUtils {
    companion object {
        fun colorFromHex(hex: String): Color {
            return Color.decode(hex)
        }
    }
}

enum class Colors(val color: String) {
    MAIN("#5F795B")
    , SECONDARY("#88D07C")
    , TERTIARY("#D6E0C6")
    , NEUTRAL("#FFFFFF")
    , GREEN("#009E4F")
    , RED("#DA1E28")
    , BLUE("#759EC5")
    , YELLOW("#FDE047")
    ;
}
