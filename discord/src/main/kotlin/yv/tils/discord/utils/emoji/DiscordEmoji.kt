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

package yv.tils.discord.utils.emoji

import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji
import okio.IOException
import org.bukkit.entity.Player
import yv.tils.discord.logic.AppLogic
import yv.tils.discord.utils.emoji.EmojiUtils.Companion.playerEmojis
import yv.tils.utils.player.PlayerUtils
import java.io.File
import java.net.URI

class DiscordEmoji {
    companion object {
        var persistentEmojis = true
    }

    /**
     * Sets the persistent emojis based on the current application emojis.
     * If there are more than 1800 emojis, it disables persistent emojis and cleans up old emojis.
     * If there are less than 1800 emojis, it enables persistent emojis and keeps them for 90 days.
     * @throws RuntimeException if the emoji cleanup fails.
     */
    fun setPersistentEmojis() {
        val emojis = AppLogic.jda.retrieveApplicationEmojis().complete()
        val emojiCount = emojis.count { it.name.startsWith("yv_") }

        try {
            if (emojiCount > 1800) {
                persistentEmojis = false
                cleanupAppEmojis(emojis = emojis)
            } else {
                persistentEmojis = true
                cleanupAppEmojis(90 * 24 * 60 * 60 * 1000L, emojis) // Keep emojis for 90 days if less than 1800 emojis
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to set persistent emojis: ${e.message}", e)
        }
    }

    /**
     * Loads persistent emojis from the Discord application.
     * It retrieves all emojis and adds those that start with "yv_" to the playerEmojis map.
     */
    fun loadPersistentEmojis() {
        val emojis = AppLogic.jda.retrieveApplicationEmojis().complete()
        for (emoji in emojis) {
            if (emoji.name.startsWith("yv_")) {
                playerEmojis[emoji.name.substring(3)] = emoji.idLong
            }
        }
    }

    /**
     * Creates a skin emoji for the given player and adds it to the Discord application.
     * @param player The player whose emoji should be created.
     * @return The ID of the created emoji.
     * @throws RuntimeException if the emoji creation fails.
     */
    fun createSkinEmoji(player: Player): Long {
        val uuid = player.uniqueId
        val skinUrl = PlayerUtils.PLAYER_HEAD_API.replace("<uuid>", uuid.toString())

        // Use skin hash as emoji name to avoid duplicates
        val emojiName = "yv_${PlayerUtils.getSkinHash(player).take(29)}"

        val icon = try {
            createIcon(downloadImageFromUrl(skinUrl))
        } catch (e: Exception) {
            throw RuntimeException("Failed to create icon for emoji: $emojiName", e)
        }

        val emojiID = try {
            createAppEmoji(emojiName, icon)
        } catch (e: Exception) {
            throw RuntimeException("Failed to create emoji: $emojiName", e)
        }

        return emojiID
    }

    /**
     * Removes an application emoji by its ID.
     * @param emojiId The ID of the emoji to remove.
     * @throws RuntimeException if the emoji deletion fails.
     */
    fun deleteAppEmoji(emojiId: Long) {
        try {
            removeAppEmoji(emojiId.toString())
        } catch (e: Exception) {
            throw RuntimeException("Failed to delete emoji with ID: $emojiId", e)
        }
    }

    /**
     * Cleans up application emojis that are older than the specified max age.
     * @param maxAge The maximum age in milliseconds. Emojis older than this will be deleted.
     * @param emojis The list of emojis to check. If empty, all emojis will be retrieved.
     * @throws RuntimeException if the emoji deletion fails.
     */
    fun cleanupAppEmojis(maxAge: Long = 7 * 24 * 60 * 60 * 1000L, emojis: List<ApplicationEmoji> = emptyList()) {
        if (emojis.isEmpty()) {
            val retrievedEmojis = AppLogic.jda.retrieveApplicationEmojis().complete()

            if (retrievedEmojis.isEmpty()) return

            cleanupAppEmojis(maxAge, retrievedEmojis)
            return
        }

        for (emoji in emojis) {
            if (emoji.name.startsWith("yv_")) {
                val creationTime = emoji.timeCreated.toInstant().toEpochMilli()
                val currentTime = System.currentTimeMillis()
                if (currentTime - creationTime > maxAge) {
                    try {
                        removeAppEmoji(emoji.id)
                    } catch (e: Exception) {
                        throw RuntimeException("Failed to cleanup emoji with ID: ${emoji.id}", e)
                    }
                }
            }
        }
    }

    /**
     * Creates an application emoji with the given name and icon.
     * @param name The name of the emoji.
     * @param icon The icon for the emoji.
     * @throws RuntimeException if the emoji creation fails.
     */
    fun createAppEmoji(name: String, icon: Icon): Long {
        val emoji = AppLogic.jda.createApplicationEmoji(
            name,
            icon,
        ).complete()

        return emoji.idLong
    }

    /**
     * Removes an application emoji by its ID.
     * @param emojiId The ID of the emoji to remove.
     * @throws RuntimeException if the emoji deletion fails.
     */
    private fun removeAppEmoji(emojiId: String) {
        try {
            AppLogic.jda.retrieveApplicationEmojiById(emojiId).queue { emoji ->
                emoji.delete().queue()
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to delete emoji with ID: $emojiId", e)
        }
    }

    /**
     * Creates an icon from a file.
     * @param file The file to create the icon from.
     * @return The created icon.
     * @throws RuntimeException if the icon creation fails.
     */
    private fun createIcon(file: File): Icon {
        try {
            return Icon.from(file)
        } catch (e: IOException) {
            throw RuntimeException("Failed to create icon from file: ${file.absolutePath}", e)
        }
    }

    /**
     * Downloads an image from a URL and saves it to a temporary file.
     * @param url The URL to download the image from.
     * @return The temporary file containing the downloaded image.
     * @throws IOException if the download fails.
     */
    private fun downloadImageFromUrl(url: String): File {
        try {
            val connection = URI(url).toURL().openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("Accept", "image/png,image/*;q=0.8,*/*;q=0.5")
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            val inputStream = connection.getInputStream()
            val tempFile = File.createTempFile("emoji_", ".png")
            tempFile.deleteOnExit()

            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }

            return tempFile
        } catch (e: IOException) {
            throw okio.IOException("Failed to download image from URL: $url", e)
        }
    }
}
