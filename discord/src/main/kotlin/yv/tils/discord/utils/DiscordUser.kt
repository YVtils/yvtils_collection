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

package yv.tils.discord.utils

import net.dv8tion.jda.api.entities.User
import yv.tils.discord.logic.AppLogic

class DiscordUser {
    companion object {
        /**
         * Parses a user ID string to a User object.
         *
         * @param id The user ID as a string.
         * @return The User object corresponding to the given ID.
         * @throws IllegalArgumentException if the user ID is invalid or the user cannot be found.
         */
        fun parseIDToUser(id: String): User {
            if (id.startsWith("~")) {
                throw IllegalArgumentException("User ID cannot start with a tilde (~). Please provide a valid user ID.")
            }

            return try {
                AppLogic.jda.retrieveUserById(id).complete()
            } catch (e: Exception) {
                throw IllegalArgumentException("User with ID $id not found or invalid.")
            }
        }

        /**
         * Parses a user ID string to the user's name.
         *
         * @param id The user ID as a string.
         * @return The name of the user corresponding to the given ID, or "Unknown User" if the user cannot be found.
         */
        fun parseIDToName(id: String): String {
            if (id.startsWith("~")) {
                return id
            }

            return try {
                val user = parseIDToUser(id)
                user.name
            } catch (_: IllegalArgumentException) {
                "Unknown User"
            }
        }
    }
}
