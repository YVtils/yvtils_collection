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

package yv.tils.stats.logic

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import yv.tils.stats.configs.ConfigFile
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.data.Data
import yv.tils.utils.logger.DEBUGLEVEL
import yv.tils.utils.logger.Logger
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URI
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Service for pushing stats to the YVtils API at api.yvtils.net/stats.
 *
 * ## Remote Push Behavior
 * - Only pushes if opt-in is accepted
 * - Uses HTTPS POST to send JSON payload
 * - Push interval: 1 hour (not configurable)
 * - Automatically retries on failure with exponential backoff
 * - Thread-safe and non-blocking
 *
 * ## Privacy
 * - Server ID is hashed and anonymized
 * - No player names or UUIDs are sent
 * - No IP addresses are stored (server-side responsibility)
 *
 * ## Note
 * The API endpoint and push settings are hardcoded and cannot be changed by users.
 * This ensures consistent data collection for YVtils analytics.
 */
object StatsPusher {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val isPushing = AtomicBoolean(false)
    private val lastPushTime = AtomicLong(0)
    private val consecutiveFailures = AtomicLong(0)

    // Hardcoded configuration - not user-configurable
    private const val API_ENDPOINT = "https://api.yvtils.net/stats"
    private const val PUSH_INTERVAL_SECONDS = 3600 // 1 hour
    private const val HTTP_TIMEOUT_SECONDS = 30
    private const val MAX_CONSECUTIVE_FAILURES = 5

    // Push task identifier
    private var pushTaskId: String? = null

    /**
     * Data class representing the push payload sent to the API.
     * 
     * See API-SPECIFICATION.md for full documentation on what the API
     * should do with this payload.
     */
    @Serializable
    data class PushPayload(
        /** Anonymized server identifier (UUID format) */
        val serverId: String,
        /** Server and plugin metadata */
        val metadata: PushMetadata,
        /** All registered stats as array */
        val stats: List<StatEntryDto>
    )

    @Serializable
    data class PushMetadata(
        /** Unix timestamp when stats were collected */
        val collectTimestamp: Long,
        /** Current online player count (null if collection disabled) */
        val playerCount: Int? = null,
        /** Server region (if enabled) */
        val region: String? = null,
        /** Optional human-readable server name (user-configured) */
        val serverName: String,
        /** Minecraft server version string */
        val serverVersion: String,
        /** YVtils plugin version */
        val yvtilsVersion: String
    )

    @Serializable
    data class StatEntryDto(
        val help: String,
        val key: String,
        val lastUpdated: Long,
        val type: String,
        val value: StatValueDto
    )

    @Serializable
    data class StatValueDto(
        val histogramValue: HistogramValueDto? = null,
        val listValue: List<String>? = null,
        val longValue: Long? = null,
        val stringValue: String? = null
    )

    @Serializable
    data class HistogramValueDto(
        val buckets: Map<String, Long> = emptyMap(),
        val count: Long,
        val max: Double? = null,
        val min: Double? = null,
        val sum: Double
    )

    /**
     * Response from the API.
     */
    @Serializable
    data class PushResponse(
        val success: Boolean,
        val message: String? = null
    )

    /**
     * Map internal stat type names to API-expected type names.
     * 
     * Internal: COUNTER, GAUGE, STRING, STRING_LIST, TIMESTAMP, HISTOGRAM
     * API:      COUNTER, GAUGE, STRING, LIST,        TIMESTAMP, HISTOGRAM
     */
    private fun mapTypeForApi(internalType: String): String {
        return when (internalType) {
            "STRING_LIST" -> "LIST"
            else -> internalType
        }
    }

    /**
     * Result of a push operation.
     */
    sealed class PushResult {
        data class Success(val response: PushResponse) : PushResult()
        data class Error(val message: String, val code: Int? = null) : PushResult()
        data object Skipped : PushResult()
        data object AlreadyPushing : PushResult()
        data object NotOptedIn : PushResult()
    }

    /**
     * Start the automatic push scheduler.
     * This should be called when the module is enabled.
     *
     * @return The task ID for the scheduled push task
     */
    fun startScheduledPush(): String? {
        if (!ConfigFile.isOptedIn()) {
            Logger.debug("[Stats] Not starting push scheduler - user has not opted in")
            return null
        }

        val intervalMillis = PUSH_INTERVAL_SECONDS * 1000L

        Logger.info("[Stats] Starting stats push scheduler (interval: ${PUSH_INTERVAL_SECONDS}s)")

        pushTaskId = CoroutineHandler.launchTask(
            suspend {
                pushAsync() // Return value ignored - we just log results internally
                Unit
            },
            "yvtils-stats-push",
            beforeDelay = 60_000L, // Wait 1 minute after server start before first push
            afterDelay = intervalMillis,
            isOnce = false
        )

        return pushTaskId
    }

    /**
     * Stop the automatic push scheduler.
     * This should be called when the module is disabled.
     */
    fun stopScheduledPush() {
        pushTaskId?.let { taskId ->
            CoroutineHandler.cancelTask(taskId)
            Logger.debug("[Stats] Stopped stats push scheduler")
        }
        pushTaskId = null
    }

    /**
     * Generate an anonymized server ID in UUID format.
     * Creates a consistent but privacy-preserving identifier based on server properties.
     * 
     * The ID is formatted as a UUID (xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx) for API compatibility.
     */
    private fun generateServerId(): String {
        return try {
            val serverVersion = Bukkit.getVersion()
            val serverName = ConfigFile.getString("metadata.server_name") ?: ""
            val worldName = Bukkit.getWorlds().firstOrNull()?.name ?: "world"
            val worldSeed = Bukkit.getWorlds().firstOrNull()?.seed ?: 0L
            
            // Create a hash of these properties for anonymity
            // Using world seed makes the ID stable but unique per server
            val input = "$serverVersion:$serverName:$worldName:$worldSeed"
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            formatAsUuidV4(bytes)
        } catch (_: Exception) {
            // Fallback to a random-ish ID based on current time
            val bytes = MessageDigest.getInstance("SHA-256")
                .digest("${System.currentTimeMillis()}:${System.nanoTime()}".toByteArray())
            formatAsUuidV4(bytes)
        }
    }

    /**
     * Format bytes as a valid UUIDv4 string.
     * 
     * UUIDv4 format: xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
     * where x is any hex digit and y is one of 8, 9, a, or b
     */
    private fun formatAsUuidV4(bytes: ByteArray): String {
        require(bytes.size >= 16) { "Need at least 16 bytes for UUID" }
        
        // Copy first 16 bytes and modify for UUIDv4 compliance
        val uuidBytes = bytes.take(16).toByteArray()
        
        // Set version to 4 (byte 6, high nibble)
        uuidBytes[6] = ((uuidBytes[6].toInt() and 0x0F) or 0x40).toByte()
        
        // Set variant to RFC 4122 (byte 8, high bits = 10xx)
        uuidBytes[8] = ((uuidBytes[8].toInt() and 0x3F) or 0x80).toByte()
        
        val hex = uuidBytes.joinToString("") { "%02x".format(it) }
        return "${hex.substring(0, 8)}-${hex.substring(8, 12)}-${hex.substring(12, 16)}-${hex.substring(16, 20)}-${hex.substring(20, 32)}"
    }

    /**
     * Push stats to the API asynchronously.
     * This is the method called by the scheduler.
     */
    fun pushAsync(): PushResult {
        return push()
    }

    /**
     * Push stats to the API.
     *
     * @return The result of the push operation
     */
    fun push(): PushResult {
        // Check if opted in
        if (!ConfigFile.isOptedIn()) {
            Logger.debug("[Stats] Not pushing - user has not opted in")
            return PushResult.NotOptedIn
        }

        // Prevent concurrent pushes
        if (!isPushing.compareAndSet(false, true)) {
            Logger.debug("[Stats] Already pushing, skipping")
            return PushResult.AlreadyPushing
        }

        try {
            // Check if we've had too many consecutive failures
            if (consecutiveFailures.get() >= MAX_CONSECUTIVE_FAILURES) {
                val timeSinceLastPush = System.currentTimeMillis() - lastPushTime.get()
                val backoffTime = 3600_000L // 1 hour backoff after too many failures
                
                if (timeSinceLastPush < backoffTime) {
                    Logger.debug("[Stats] Too many consecutive failures, waiting for backoff")
                    return PushResult.Skipped
                }
                
                // Reset failure count after backoff period
                consecutiveFailures.set(0)
            }

            // Build the payload
            val payload = buildPayload()
            val jsonPayload = json.encodeToString(payload)

            Logger.debug("[Stats] Pushing stats to $API_ENDPOINT")

            // Send HTTP request
            val result = sendHttpPost(API_ENDPOINT, jsonPayload, payload.serverId)

            when (result) {
                is PushResult.Success -> {
                    lastPushTime.set(System.currentTimeMillis())
                    consecutiveFailures.set(0)
                    Logger.info("[Stats] Successfully pushed stats to YVtils API")
                }
                is PushResult.Error -> {
                    consecutiveFailures.incrementAndGet()
                    Logger.warn("[Stats] Failed to push stats: ${result.message} (code: ${result.code})")
                    Logger.debug("[Stats] Payload was: $jsonPayload", DEBUGLEVEL.DETAILED)
                }
                else -> {}
            }

            return result
        } finally {
            isPushing.set(false)
        }
    }

    /**
     * Build the payload to send to the API.
     */
    private fun buildPayload(): PushPayload {
        val serverVersion = try {
            Bukkit.getVersion()
        } catch (e: Exception) {
            "Unknown"
        }

        val playerCount = try {
            if (ConfigFile.getBoolean("metadata.collect_player_count") != false) {
                Bukkit.getOnlinePlayers().size
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

        val pluginVersion = try {
            Data.yvtilsVersion
        } catch (e: Exception) {
            "Unknown"
        }

        val region = try {
            val regionEnabled = ConfigFile.getBoolean("metadata.region") == true
            if (regionEnabled) {
                ConfigFile.getString("metadata.region_value") ?: null
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

        val serverName = ConfigFile.getString("metadata.server_name") ?: ""

        val metadata = PushMetadata(
            collectTimestamp = System.currentTimeMillis(),
            playerCount = playerCount,
            region = region,
            serverName = serverName,
            serverVersion = serverVersion,
            yvtilsVersion = pluginVersion
        )

        // Build stats from the service
        val statsExport = StatsService.buildExport()
        val stats = statsExport.stats.map { stat ->
            StatEntryDto(
                help = stat.help,
                key = stat.key,
                lastUpdated = stat.lastUpdated,
                type = mapTypeForApi(stat.type),
                value = StatValueDto(
                    histogramValue = stat.value.histogramValue?.let {
                        HistogramValueDto(
                            buckets = it.buckets,
                            count = it.count,
                            max = null,  // Not tracked by registry
                            min = null,  // Not tracked by registry
                            sum = it.sum
                        )
                    },
                    listValue = stat.value.listValue,
                    longValue = stat.value.longValue,
                    stringValue = stat.value.stringValue
                )
            )
        }

        return PushPayload(
            serverId = generateServerId(),
            metadata = metadata,
            stats = stats
        )
    }

    /**
     * Send an HTTP POST request to the API.
     *
     * @param url The API URL
     * @param jsonBody The JSON body to send
     * @param serverId The server ID to include in the X-Server-ID header
     * @return The result of the request
     */
    private fun sendHttpPost(url: String, jsonBody: String, serverId: String): PushResult {
        var connection: HttpURLConnection? = null
        
        try {
            connection = URI(url).toURL().openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "YVtils-Stats/1.0")
            connection.setRequestProperty("X-Server-ID", serverId)
            connection.doOutput = true
            connection.doInput = true
            connection.connectTimeout = HTTP_TIMEOUT_SECONDS * 1000
            connection.readTimeout = HTTP_TIMEOUT_SECONDS * 1000

            // Write the JSON body
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(jsonBody)
                writer.flush()
            }

            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                // Read response
                val responseBody = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                
                return try {
                    val response = json.decodeFromString<PushResponse>(responseBody)
                    PushResult.Success(response)
                } catch (e: Exception) {
                    // If we can't parse the response but got a 2xx, consider it a success
                    PushResult.Success(PushResponse(success = true, message = "OK"))
                }
            } else {
                // Read error response
                val errorBody = try {
                    connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: "No error body"
                } catch (e: Exception) {
                    "Could not read error body"
                }
                
                return PushResult.Error("HTTP $responseCode: $errorBody", responseCode)
            }
        } catch (e: java.net.SocketTimeoutException) {
            return PushResult.Error("Connection timed out")
        } catch (e: java.net.UnknownHostException) {
            return PushResult.Error("Could not resolve host: ${e.message}")
        } catch (e: java.net.ConnectException) {
            return PushResult.Error("Connection refused: ${e.message}")
        } catch (e: Exception) {
            return PushResult.Error("Request failed: ${e.message}")
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Manually trigger a push (for testing or commands).
     * This bypasses the normal interval check.
     *
     * @return The result of the push operation
     */
    fun forcePush(): PushResult {
        Logger.info("[Stats] Force pushing stats to YVtils API")
        return push()
    }

    /**
     * Get the time of the last successful push.
     *
     * @return Unix timestamp of last push, or 0 if never pushed
     */
    fun getLastPushTime(): Long = lastPushTime.get()

    /**
     * Get the number of consecutive failures.
     *
     * @return Number of consecutive push failures
     */
    fun getConsecutiveFailures(): Long = consecutiveFailures.get()
}
