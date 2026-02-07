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

package yv.tils.moderation.configs.saveFile

import kotlinx.serialization.Serializable

@Serializable
data class MuteSave(
    val uuid: String,
    var reason: String,
    var muted: Boolean,
    var expires: String,
    val modAction: ModAction
)

@Serializable
data class WarnSave(
    val uuid: String,
    var warningCount: Int,
    val warnings: MutableList<Warning>
)

@Serializable
data class Warning(
    val id: String,
    var reason: String,
    val modAction: ModAction
)

@Serializable
data class ModAction(
    val uuid: String,
    val timestamp: String,
)