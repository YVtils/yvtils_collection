package apis

import data.Data
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import logger.Logger
import org.bukkit.OfflinePlayer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.util.*
import kotlin.concurrent.thread

class MojangAPI {
    /**
     * Verify if a Minecraft account is valid by UUID
     * @param uuid The UUID of the player (optional, if name is provided)
     * @param name The name of the player (optional, if UUID is provided)
     * @return True if the account is valid, false otherwise
     * @throws IllegalArgumentException if neither uuid nor name is provided
     * @throws Exception if the verification fails
     */
    fun verifyMinecraftAccount(uuid: UUID? = null, name: String? = null): MojangResponse {
        if (uuid == null && name == null) {
            throw IllegalArgumentException("Either uuid or name must be provided")
        }
        if (name != null) {
            val playerUUID = nameToUUID(name)
            try {
                return verifyMinecraftAccount(playerUUID)
            } catch (e: Exception) {
                throw e
            }
        }
        try {
            val url = "https://api.minecraftservices.com/minecraft/profile/lookup/$uuid"
            return sendRequest(url)
        } catch (e: Exception) {
            throw e
        }
    }

    fun nameToUUID(playerName: String): UUID {
        return Data.instance.server.getOfflinePlayer(playerName).uniqueId
    }

    fun nameToOfflinePlayer(playerName: String): OfflinePlayer {
        return Data.instance.server.getOfflinePlayer(playerName)
    }

    private fun sendRequest(url: String): MojangResponse {
        try {
            val connection = URI(url).toURL().openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            when (responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                    val json = Json { ignoreUnknownKeys = true }
                    return json.decodeFromString<SuccessfulResponse>(response.toString())
                }
                HttpURLConnection.HTTP_NOT_FOUND -> {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                    val json = Json { ignoreUnknownKeys = true }
                    return json.decodeFromString<ErrorResponse>(response.toString())
                }
                else -> {
                    Logger.error("Unexpected response code: $responseCode")
                    return ErrorResponse(url, "Unexpected response code: $responseCode")
                }
            }
        } catch (e: Exception) {
            Logger.error("Failed to connect to Mojang API: ${e.message}")
            return ErrorResponse(url, e.message ?: "Unknown error")
        }
    }

    sealed class MojangResponse
    @Serializable
    data class SuccessfulResponse(val id: String, val name: String) : MojangResponse()
    @Serializable
    data class ErrorResponse(val path: String, val error: String, val errorMessage: String? = null) : MojangResponse()
}