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

package yv.tils.moderation.data

class Exceptions {
    companion object {
        val TargetToOfflinePlayerParseException = Exception("There occurred an error while trying to parse the target to an offline player.")
        val MuteDataNotFoundException = Exception("Mute data for the target could not be found.")

        val PlayerProfileToOfflinePlayerParseException = Exception("There occurred an error while trying to parse the PlayerProfile to an offline player.")
        val TimeUnitParseException = Exception("The provided time unit could not be parsed.")

        val ModerationActionException = Exception("The moderation action could not be performed due to an unknown error.")
    }
}