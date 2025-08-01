package yv.tils.discord.utils

import net.dv8tion.jda.api.entities.Icon
import okio.IOException
import yv.tils.discord.logic.AppLogic
import java.io.File
import java.net.URI
import java.util.*

// TODO: Error handling
// TODO: Add logic for keeping emojis, until app has more than 1800 emojis from this plugin -> For this switch to uuid only as emoji name
class DiscordEmoji {
    companion object {
        val playerEmojis = mutableMapOf<UUID, Long>()
    }

    fun createSkinEmoji(uuid: UUID, url: String, playerName: String): Long {
        val emojiName = "skin_$playerName"
        val icon = try {
            createIcon(downloadImageFromUrl(url))
        } catch (e: Exception) {
            throw RuntimeException("Failed to create icon for emoji: $emojiName", e)
        }

        val emojiID = try {
            createAppEmoji(emojiName, icon)
        } catch (e: Exception) {
            throw RuntimeException("Failed to create emoji: $emojiName", e)
        }
        playerEmojis[uuid] = emojiID
        return emojiID
    }

    fun removeSkinEmoji(uuid: UUID) {
        val emojiID = playerEmojis.remove(uuid)
        if (emojiID != null) {
            removeAppEmoji(emojiID.toString())
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
     * Creates an application emoji with the given name and icon.
     * @param name The name of the emoji.
     * @param icon The icon for the emoji.
     * @throws RuntimeException if the emoji creation fails.
     */
    fun createAppEmoji(name: String, file: File): Long {
        try {
            val icon = Icon.from(file)
            return createAppEmoji(name, icon)
        } catch (e: IOException) {
            throw RuntimeException("Failed to create emoji from file: ${file.absolutePath}", e)
        }
    }

    /**
     * Removes an application emoji by its ID.
     * @param emojiId The ID of the emoji to remove.
     */
    fun removeAppEmoji(emojiId: String) {
        AppLogic.jda.retrieveApplicationEmojiById(emojiId).queue { emoji ->
            emoji.delete().queue()
        }
    }

    /**
     * Creates an icon from a file.
     * @param file The file to create the icon from.
     * @return The created icon.
     * @throws RuntimeException if the icon creation fails.
     */
    fun createIcon(file: File): Icon {
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
            throw IOException("Failed to download image from URL: $url", e)
        }
    }
}
