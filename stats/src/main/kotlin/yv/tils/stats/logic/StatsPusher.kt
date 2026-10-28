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

package yv.tils.stats.logic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import yv.tils.stats.configs.ConfigFile
import yv.tils.stats.registry.StatsRegistry
import yv.tils.utils.logger.Logger
import yv.tils.utils.modules.Core
import yv.tils.utils.stats.ModuleTelemetry
import java.io.OutputStreamWriter
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URI
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Service for pushing stats to the YVtils API at api.yvtils.net/stats.
 *
 * ## Remote Push Behavior
 * - Only pushes if opt-in is accepted
 * - Uses HTTPS POST to send JSON payload
 * - Push interval: 1 hour (not configurable)
 * - Automatically retries on failure with exponential backoff
 * - Scheduled requests run on IO workers; synchronous APIs must also run on a worker
 *
 * ## Privacy
 * - Server ID is a persisted random UUID, never derived from world data
 * - Default metrics contain no player identities; custom metric authors must avoid PII
 * - The receiver can see the connection IP address; its retention is server-side policy
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
    private val backoff = PushBackoff()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val generation = AtomicLong(0)
    private val activeConnection = AtomicReference<HttpURLConnection?>()
    @Volatile private var enabled = false

    // Hardcoded configuration - not user-configurable
    private const val API_ENDPOINT = "https://api.yvtils.net/stats"
    private const val PUSH_INTERVAL_SECONDS = 3600 // 1 hour
    private const val HTTP_TIMEOUT_SECONDS = 30
    private const val MAX_RESPONSE_BYTES = 16 * 1024

    // Owned job, independent of the shared task registry's millisecond IDs.
    @Volatile internal var pushJob: Job? = null
        private set

    /**
     * Data class representing the push payload sent to the API.
     */
    @Serializable
    data class PushPayload(
        /** Random installation identifier (UUID format) */
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
        /** Reserved for wire compatibility; this module does not perform geolocation. */
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
        val stringValue: String? = null,
        // Keep the existing wire shape unchanged for integer/default metrics.
        @OptIn(ExperimentalSerializationApi::class)
        @EncodeDefault(EncodeDefault.Mode.NEVER)
        val doubleValue: Double? = null,
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
     * @return An opaque scheduler identifier, or null when disabled/not opted in.
     * Use [stopScheduledPush] to cancel it, not the shared coroutine task registry.
     */
    @Synchronized
    fun startScheduledPush(): String? {
        if (!enabled || !ConfigFile.isOptedIn()) {
            Logger.debug("[Stats] Not starting push scheduler - user has not opted in")
            return null
        }

        if (pushJob?.isActive == true) return "yvtils-stats-push"

        Logger.debug("[Stats] Starting stats push scheduler (interval: ${PUSH_INTERVAL_SECONDS}s)")

        pushJob = scope.launch {
            delay(60_000L)
            while (isActive && enabled && ConfigFile.isOptedIn()) {
                val result = push()
                val retryDelay = synchronized(backoff) { backoff.remainingMillis() }
                delay(if (result is PushResult.Error || result == PushResult.Skipped) {
                    retryDelay.coerceAtLeast(1000L)
                } else PUSH_INTERVAL_SECONDS * 1000L)
            }
        }

        return "yvtils-stats-push"
    }

    /**
     * Stop the automatic push scheduler.
     * This should be called when the module is disabled.
     */
    @Synchronized
    fun stopScheduledPush() {
        generation.incrementAndGet()
        pushJob?.cancel()
        pushJob = null
        activeConnection.getAndSet(null)?.disconnect()
    }

    internal fun setEnabled(value: Boolean) {
        enabled = value
        if (!value) stopScheduledPush()
    }

    /**
     * Legacy synchronous alias. Call from a worker, or use [pushOnIo].
     */
    fun pushAsync(): PushResult {
        return push()
    }

    suspend fun pushOnIo(): PushResult = withContext(Dispatchers.IO) { push() }

    /**
     * Push stats to the API.
     *
     * @return The result of the push operation
     */
    fun push(): PushResult = pushInternal(force = false)

    private fun pushInternal(force: Boolean): PushResult {
        // Check if opted in
        if (!ConfigFile.isOptedIn()) {
            Logger.debug("[Stats] Not pushing - user has not opted in")
            return PushResult.NotOptedIn
        }
        if (!enabled) return PushResult.Skipped
        if (runCatching { Bukkit.isPrimaryThread() }.getOrDefault(false)) {
            return PushResult.Error("Remote stats push must run on a worker thread; use pushOnIo()")
        }
        val requestGeneration = generation.get()

        // Prevent concurrent pushes
        if (!isPushing.compareAndSet(false, true)) {
            Logger.debug("[Stats] Already pushing, skipping")
            return PushResult.AlreadyPushing
        }

        try {
            if (!force && synchronized(backoff) { backoff.isWaiting() }) return PushResult.Skipped

            // Build the payload
            val payload = buildPayload()
            val jsonPayload = json.encodeToString(payload)

            Logger.debug("[Stats] Pushing stats to $API_ENDPOINT")

            // Send HTTP request
            val result = sendHttpPost(API_ENDPOINT, jsonPayload, payload.serverId, requestGeneration)
            if (!canSend(requestGeneration)) return PushResult.Skipped

            when (result) {
                is PushResult.Success -> {
                    lastPushTime.set(System.currentTimeMillis())
                    synchronized(backoff) { backoff.reset() }
                    ModuleTelemetry.record(ModuleTelemetry.Event.STATS_PUSH_SUCCESSES)
                    Logger.info("[Stats] Successfully pushed stats to YVtils API")
                }
                is PushResult.Error -> {
                    synchronized(backoff) { backoff.failed() }
                    Logger.warn("[Stats] Failed to push stats: ${result.message} (code: ${result.code})")
                }
                else -> {}
            }

            return result
        } catch (e: Exception) {
            if (!canSend(requestGeneration)) return PushResult.Skipped
            synchronized(backoff) { backoff.failed() }
            Logger.warn("[Stats] Could not prepare stats push: ${e.message}")
            return PushResult.Error("Could not prepare stats push: ${e.message}")
        } finally {
            isPushing.set(false)
        }
    }

    /**
     * Build the payload to send to the API.
     */
    internal fun buildPayload(): PushPayload {
        val settings = ConfigFile.state
        val serverVersion = StatsRegistry.getValue("server_version")?.stringValue ?: "Unknown"
        val playerCount = if (settings.metadata.collect_player_count) {
            StatsRegistry.getValue("player_count")?.longValue?.toInt()
        } else null

        val pluginVersion = try {
            Core.yvtilsVersion
        } catch (e: Exception) {
            "Unknown"
        }

        val metadata = PushMetadata(
            collectTimestamp = System.currentTimeMillis(),
            playerCount = playerCount,
            region = null,
            serverName = settings.metadata.server_name,
            serverVersion = serverVersion,
            yvtilsVersion = pluginVersion
        )

        // Build stats from the service
        val statsExport = StatsService.buildExport()
        val stats = statsExport.stats.filter { settings.metadata.collect_player_count || it.key != "player_count" }.map { stat ->
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
                    doubleValue = stat.value.doubleValue,
                    stringValue = stat.value.stringValue
                )
            )
        }

        return PushPayload(
            serverId = settings.server_id,
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
    internal fun sendHttpPost(url: String, jsonBody: String, serverId: String, requestGeneration: Long = generation.get()): PushResult {
        var connection: HttpURLConnection? = null
        
        try {
            if (!canSend(requestGeneration)) return PushResult.Skipped
            connection = URI(url).toURL().openConnection() as HttpURLConnection
            activeConnection.set(connection)
            if (!canSend(requestGeneration)) return PushResult.Skipped
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "YVtils-Stats/1.0")
            connection.setRequestProperty("X-Server-ID", serverId)
            connection.doOutput = true
            connection.doInput = true
            // Never forward the installation ID or payload to a redirect target.
            connection.instanceFollowRedirects = false
            connection.connectTimeout = HTTP_TIMEOUT_SECONDS * 1000
            connection.readTimeout = HTTP_TIMEOUT_SECONDS * 1000

            // Write the JSON body
            if (!canSend(requestGeneration)) return PushResult.Skipped
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                if (!canSend(requestGeneration)) return PushResult.Skipped
                writer.write(jsonBody)
                writer.flush()
            }

            val responseCode = connection.responseCode

            if (responseCode in 200..299) {
                // Read response
                val responseBody = connection.inputStream.use {
                    val bytes = it.readNBytes(MAX_RESPONSE_BYTES + 1)
                    if (bytes.size > MAX_RESPONSE_BYTES) return PushResult.Error("API response exceeded size limit", responseCode)
                    bytes.toString(Charsets.UTF_8)
                }
                if (responseBody.isBlank()) return PushResult.Success(PushResponse(true))
                
                return try {
                    val response = json.decodeFromString<PushResponse>(responseBody)
                    if (response.success) PushResult.Success(response)
                    else PushResult.Error(response.message ?: "API rejected stats", responseCode)
                } catch (e: Exception) {
                    PushResult.Error("Invalid API response", responseCode)
                }
            } else {
                // Read error response
                val errorBody = try {
                    connection.errorStream?.use { it.readNBytes(MAX_RESPONSE_BYTES).toString(Charsets.UTF_8) } ?: "No error body"
                } catch (e: Exception) {
                    "Could not read error body"
                }
                
                return PushResult.Error("HTTP $responseCode: ${errorBody.replace('\n', ' ').replace('\r', ' ')}", responseCode)
            }
        } catch (e: SocketTimeoutException) {
            return PushResult.Error("Connection timed out")
        } catch (e: UnknownHostException) {
            return PushResult.Error("Could not resolve host: ${e.message}")
        } catch (e: ConnectException) {
            return PushResult.Error("Connection refused: ${e.message}")
        } catch (e: Exception) {
            return PushResult.Error("Request failed: ${e.message}")
        } finally {
            activeConnection.compareAndSet(connection, null)
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
        return pushInternal(force = true)
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
    fun getConsecutiveFailures(): Long = synchronized(backoff) { backoff.failures }

    private fun canSend(requestGeneration: Long): Boolean =
        enabled && ConfigFile.isOptedIn() && requestGeneration == generation.get()
}
