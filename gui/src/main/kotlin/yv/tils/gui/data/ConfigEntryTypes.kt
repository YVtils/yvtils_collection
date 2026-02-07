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

package yv.tils.gui.data

import yv.tils.config.data.EntryType

enum class ConfigEntryTypes(val type: String, val clickActions: List<ClickActions>) {
    STRING("String", listOf(ClickActions.MODIFY_TEXT)),
    INT("Int", listOf(
        ClickActions.INCREMENT_VALUE,
        ClickActions.DECREMENT_VALUE,
        ClickActions.INCREMENT_VALUE_SHIFT,
        ClickActions.DECREMENT_VALUE_SHIFT
    )),
    DOUBLE("Double", listOf(
        ClickActions.INCREMENT_VALUE,
        ClickActions.DECREMENT_VALUE,
        ClickActions.INCREMENT_VALUE_SHIFT,
        ClickActions.DECREMENT_VALUE_SHIFT
    )),
    BOOLEAN("Boolean", listOf(ClickActions.TOGGLE_OPTION)),
    LIST("List", listOf(ClickActions.OPEN_SETTING)),
    MAP("Map", listOf(ClickActions.OPEN_SETTING)),
    UNKNOWN("Unknown", emptyList());

    companion object {
        fun fromEntryType(t: EntryType): ConfigEntryTypes = when (t) {
            EntryType.STRING -> STRING
            EntryType.INT -> INT
            EntryType.DOUBLE -> DOUBLE
            EntryType.BOOLEAN -> BOOLEAN
            EntryType.LIST -> LIST
            EntryType.MAP -> MAP
            EntryType.ANY, EntryType.UNKNOWN -> UNKNOWN
        }
    }
}