package yv.tils.discord.logic.whitelist

import data.Data
import kotlinx.serialization.Serializable
import logger.Logger
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import org.bukkit.OfflinePlayer
import yv.tils.discord.configs.ConfigFile
import yv.tils.discord.configs.SaveFile
import yv.tils.discord.logic.AppLogic

class WhitelistLogic {
    companion object {
        /**
         * Validates a Minecraft username.
         * A valid username can contain alphanumeric characters and underscores.
         *
         * @param username The Minecraft username to validate.
         * @return True if the username is valid, false otherwise.
         */
        fun isValidUsername(username: String): Boolean {
            return username.matches(Regex("[a-zA-Z0-9_]+"))
        }

        /**
         * A map to store whitelisted entries.
         * The key is the Discord user ID, and the value is a WhitelistEntry object.
         */
        val whitelistMap: MutableMap<String, WhitelistEntry> = mutableMapOf()

        /**
         * Gets a whitelisted entry by Discord user ID.
         * @param discordUserID The Discord user ID to search for.
         * @return The WhitelistEntry associated with the Discord user ID, or null if not found.
         */
        fun getEntryByDiscordID(discordUserID: String): WhitelistEntry? {
            return whitelistMap[discordUserID]
        }

        /**
         * Gets a whitelisted entry by Minecraft username or UUID.
         * Searches for the entry by either the Minecraft name or UUID, ignoring case.
         *
         * @param minecraftName The Minecraft username to search for.
         * @return The WhitelistEntry associated with the Minecraft username, or null if not found.
         */
        fun getEntryByMinecraftName(minecraftName: String): WhitelistEntry? {
            return whitelistMap.values.find { it.minecraftName.equals(minecraftName, ignoreCase = true) }
        }

        /**
         * Gets a whitelisted entry by Minecraft UUID.
         * Searches for the entry by the Minecraft UUID, ignoring case.
         *
         * @param minecraftUUID The Minecraft UUID to search for.
         * @return The WhitelistEntry associated with the Minecraft UUID, or null if not found.
         */
        fun getEntryByMinecraftUUID(minecraftUUID: String): WhitelistEntry? {
            return whitelistMap.values.find { it.minecraftUUID.equals(minecraftUUID, ignoreCase = true) }
        }

        /**
         * Gets all whitelisted entries.
         * @return A list of all WhitelistEntry objects in the whitelist.
         */
        fun getAllEntries(): List<WhitelistEntry> {
            return whitelistMap.values.toList()
        }

        /**
         * Adds a new entry to the whitelist.
         * If an entry with the same Discord user ID already exists, it will be replaced.
         *
         * @param entry The WhitelistEntry to add.
         * @throws IllegalArgumentException if the Minecraft username is invalid.
         */
        fun addEntry(entry: WhitelistEntry, player: OfflinePlayer? = null) {
            if (isValidUsername(entry.minecraftName)) {
                if (player != null && !player.isWhitelisted) {
                    Data.instance.server.scheduler.runTask(Data.instance, Runnable {
                        player.isWhitelisted = true
                    })
                }

                SaveFile().addSave(
                    entry.discordUserID,
                    entry.minecraftName,
                    entry.minecraftUUID
                )
            } else {
                throw IllegalArgumentException("Invalid Minecraft username: ${entry.minecraftName}") // TODO: Look over error message
            }
        }

        /**
         * Removes an entry from the whitelist.
         *
         * @param discordUserID The Discord user ID of the entry to remove.
         * @param player The OfflinePlayer associated with the entry, if applicable.
         * @throws IllegalArgumentException if no entry exists for the given Discord user ID.
         */
        fun removeEntry(discordUserID: String, player: OfflinePlayer? = null) {
            if (containsEntry(discordUserID)) {
                if (player != null && player.isWhitelisted) {
                    Data.instance.server.scheduler.runTask(Data.instance, Runnable {
                        player.isWhitelisted = false
                    })
                }

                SaveFile().removeSave(discordUserID)
            } else {
                throw IllegalArgumentException("No whitelist entry found for Discord user ID: $discordUserID")
            }
        }

        /**
         * Checks if an entry exists in the whitelist by Discord user ID.
         *
         * @param discordUserID The Discord user ID to check.
         * @return True if the entry exists, false otherwise.
         */
        fun containsEntry(discordUserID: String): Boolean {
            return whitelistMap.containsKey(discordUserID)
        }

        /**
         * A list to store roles configured for the whitelist feature.
         * This is used to manage roles assigned to Discord members when they are whitelisted.
         */
        val roles = mutableListOf<Role>()

        /**
         * Adds all roles configured for the whitelist feature to a Discord member.
         *
         * @param discordUserID The ID of the Discord user to add roles to.
         * @param guildID The ID of the guild in which to add roles.
         * @throws IllegalArgumentException if the guild or user is not found, or if no roles are configured.
         */
        fun addRolesToMember(
            discordUserID: String,
            guildID: String
        ) {
            val guild: Guild? = AppLogic.jda.getGuildById(guildID)
            if (guild == null) {
                throw IllegalArgumentException("Guild not found for ID: $guildID")
            }

            val discordUser = guild.getMemberById(discordUserID)
            if (discordUser == null) {
                throw IllegalArgumentException("Discord user not found for ID: $discordUserID")
            }

            loadRoles(guild)

            for (role in roles) {
                if (guild.getRoleById(role.id) != null) {
                    guild.addRoleToMember(discordUser, role).queue(
                        { Logger.debug("Role ${role.name} added to user ${discordUser.user.name}") },
                        { error -> throw IllegalArgumentException("Failed to add role ${role.name} to user ${discordUser.user.name}: ${error.message}") },
                    )
                } else {
                    throw IllegalArgumentException("Role with ID ${role.id} not found in guild ${guild.name}")
                }
            }
        }

        /**
         * Removes all roles configured for the whitelist feature from a Discord member.
         *
         * @param discordUserID The ID of the Discord user to remove roles from.
         * @param guildID The ID of the guild from which to remove roles.
         * @throws IllegalArgumentException if the guild or user is not found, or if no roles are configured.
         */
        fun removeRolesFromMember(
            discordUserID: String,
            guildID: String
        ) {
            val guild: Guild? = AppLogic.jda.getGuildById(guildID)
            if (guild == null) {
                throw IllegalArgumentException("Guild not found for ID: $guildID")
            }

            val discordUser = guild.getMemberById(discordUserID)
            if (discordUser == null) {
                throw IllegalArgumentException("Discord user not found for ID: $discordUserID")
            }

            loadRoles(guild)

            for (role in roles) {
                if (guild.getRoleById(role.id) != null) {
                    guild.removeRoleFromMember(discordUser, role).queue(
                        { Logger.debug("Role ${role.name} removed from user ${discordUser.user.name}") },
                        { error -> throw IllegalArgumentException("Failed to remove role ${role.name} from user ${discordUser.user.name}: ${error.message}") },
                    )
                } else {
                    throw IllegalArgumentException("Role with ID ${role.id} not found in guild ${guild.name}")
                }
            }
        }

        /**
         * Loads the roles configured for the whitelist feature in the specified guild.
         *
         * @param guild The guild from which to load the roles.
         * @throws IllegalArgumentException if no roles are configured or if any role IDs are invalid.
         */
        private fun loadRoles(guild: Guild) {
            if (roles.isEmpty()) {
                val roleConfig = ConfigFile.getValueAsString("whitelistFeature.roles")
                if (roleConfig.isNullOrEmpty()) {
                    throw IllegalArgumentException("No roles configured for guild ${guild.name}")
                }

                val roleIds = roleConfig.split(",").map { it.trim() }
                if (roleIds.isEmpty()) {
                    throw IllegalArgumentException("No valid role IDs found in configuration for guild ${guild.name}")
                }

                roles.clear()

                for (roleId in roleIds) {
                    try {
                        val role = guild.getRoleById(roleId)
                        if (role != null) {
                            roles.add(role)
                        } else {
                            throw IllegalArgumentException("Role with ID $roleId not found in guild ${guild.name}")
                        }
                    } catch (_: NumberFormatException) {
                        throw IllegalArgumentException("Invalid role ID format: $roleId in guild ${guild.name}")
                    }
                }
            }
        }
    }
}

@Serializable
data class WhitelistEntry (
    val discordUserID: String,
    val minecraftName: String,
    val minecraftUUID: String,
)