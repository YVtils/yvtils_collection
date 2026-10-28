/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */

package yv.tils.stats.logic

import com.sun.net.httpserver.HttpServer
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import yv.tils.stats.configs.ConfigFile
import yv.tils.stats.configs.StatsConfigState
import yv.tils.stats.registry.StatsRegistry
import yv.tils.stats.api.RegistryStatsBackend
import yv.tils.utils.stats.ModuleTelemetry
import yv.tils.utils.stats.StatsApi
import java.net.InetSocketAddress
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class StatsPusherTest {
    private lateinit var server: HttpServer
    private var requests = AtomicInteger()
    private var code = 200
    private var body = "{\"success\":true}"
    private var receivedBody = ""
    private var receivedId: String? = null

    @BeforeEach
    fun setup() {
        ConfigFile.state = StatsConfigState(opt_in = true, server_id = UUID.randomUUID().toString())
        StatsRegistry.clear()
        StatsPusher.setEnabled(true)
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/stats") { exchange ->
            requests.incrementAndGet()
            receivedBody = exchange.requestBody.use { it.readBytes().toString(Charsets.UTF_8) }
            receivedId = exchange.requestHeaders.getFirst("X-Server-ID")
            if (code in 300..399) exchange.responseHeaders.add("Location", "/redirect")
            val bytes = body.toByteArray(Charsets.UTF_8)
            exchange.sendResponseHeaders(code, if (code == 204) -1 else bytes.size.toLong())
            exchange.responseBody.use { if (code != 204) it.write(bytes) }
            exchange.close()
        }
        server.createContext("/redirect") { exchange ->
            requests.incrementAndGet()
            exchange.sendResponseHeaders(204, -1)
            exchange.close()
        }
        server.start()
    }

    @AfterEach
    fun cleanup() {
        StatsPusher.setEnabled(false)
        server.stop(0)
        StatsRegistry.clear()
        ConfigFile.state = StatsConfigState()
    }

    private fun send(): StatsPusher.PushResult = StatsPusher.sendHttpPost(
        "http://127.0.0.1:${server.address.port}/stats", "{\"name\":\"Grüße\"}", ConfigFile.state.server_id,
    )

    @Test
    fun `HTTP payload uses UTF-8 and the persisted identifier`() {
        assertInstanceOf(StatsPusher.PushResult.Success::class.java, send())
        assertEquals("{\"name\":\"Grüße\"}", receivedBody)
        assertEquals(ConfigFile.state.server_id, receivedId)
    }

    @Test
    fun `API rejection in a successful HTTP response is an error`() {
        body = "{\"success\":false,\"message\":\"rejected\"}"
        val result = assertInstanceOf(StatsPusher.PushResult.Error::class.java, send())
        assertEquals("rejected", result.message)
    }

    @Test
    fun `all successful HTTP statuses including no content are supported`() {
        code = 202
        assertInstanceOf(StatsPusher.PushResult.Success::class.java, send())
        code = 204
        body = ""
        assertInstanceOf(StatsPusher.PushResult.Success::class.java, send())
    }

    @Test
    fun `malformed successful response is not counted as success`() {
        body = "<html>bad gateway</html>"
        assertInstanceOf(StatsPusher.PushResult.Error::class.java, send())
    }

    @Test
    fun `oversized successful responses are rejected`() {
        body = "x".repeat(20000)
        assertInstanceOf(StatsPusher.PushResult.Error::class.java, send())
    }

    @Test
    fun `redirects are not followed with identifying telemetry`() {
        code = 307
        assertInstanceOf(StatsPusher.PushResult.Error::class.java, send())
        assertEquals(1, requests.get())
    }

    @Test
    fun `HTTP errors preserve status and bound diagnostic text`() {
        code = 503
        body = "unavailable\n" + "x".repeat(20000)
        val result = assertInstanceOf(StatsPusher.PushResult.Error::class.java, send())
        assertEquals(503, result.code)
        assertFalse(result.message.contains('\n'))
        assertTrue(result.message.length < 17000)
    }

    @Test
    fun `declined consent prevents both automatic and forced requests`() {
        ConfigFile.state = ConfigFile.state.copy(opt_in = false)
        assertEquals(StatsPusher.PushResult.NotOptedIn, StatsPusher.push())
        assertEquals(StatsPusher.PushResult.NotOptedIn, StatsPusher.forcePush())
        assertEquals(StatsPusher.PushResult.Skipped, send())
        assertNull(StatsPusher.startScheduledPush())
        assertEquals(0, requests.get())
    }

    @Test
    fun `disabled module never sends despite persisted consent`() {
        StatsPusher.setEnabled(false)
        assertEquals(StatsPusher.PushResult.Skipped, send())
        assertEquals(StatsPusher.PushResult.Skipped, StatsPusher.forcePush())
        assertNull(StatsPusher.startScheduledPush())
        assertEquals(0, requests.get())
    }

    @Test
    fun `scheduler start is idempotent and can restart after cancellation`() {
        assertNotNull(StatsPusher.startScheduledPush())
        val first = StatsPusher.pushJob!!
        StatsPusher.startScheduledPush()
        assertSame(first, StatsPusher.pushJob)
        StatsPusher.stopScheduledPush()
        assertTrue(first.isCancelled)
        assertNull(StatsPusher.pushJob)
        assertNotNull(StatsPusher.startScheduledPush())
        assertNotSame(first, StatsPusher.pushJob)
        assertTrue(StatsPusher.pushJob!!.isActive)
    }

    @Test
    fun `disabled player counts are omitted from metadata and metric entries`() {
        ConfigFile.state = ConfigFile.state.copy(metadata = StatsConfigState.Metadata(collect_player_count = false))
        StatsRegistry.registerGauge("player_count", "")!!.set(10L)
        StatsRegistry.registerString("server_version", "")!!.set("Paper test")
        val payload = StatsPusher.buildPayload()
        assertNull(payload.metadata.playerCount)
        assertFalse(payload.stats.any { it.key == "player_count" })
        assertEquals("Paper test", payload.metadata.serverVersion)
        assertEquals(ConfigFile.state.server_id, payload.serverId)
        assertNull(payload.metadata.region)
    }

    @Test
    fun `payload preserves decimal values and the API list type mapping`() {
        StatsRegistry.registerGauge("load", "")!!.set(0.75)
        StatsRegistry.registerList("modules", "")!!.set(listOf("stats"))
        val entries = StatsPusher.buildPayload().stats.associateBy { it.key }
        assertEquals(0.75, entries.getValue("load").value.doubleValue)
        assertEquals("LIST", entries.getValue("modules").type)
    }

    @Test
    fun `integer payloads do not add a decimal field to the existing wire format`() {
        val json = Json { encodeDefaults = true }
        assertFalse(json.encodeToString(StatsPusher.StatValueDto(longValue = 1)).contains("doubleValue"))
        assertTrue(json.encodeToString(StatsPusher.StatValueDto(doubleValue = 0.75)).contains("\"doubleValue\":0.75"))
    }

    @Test
    fun `module usage reaches the normal HTTP payload and optout stops transmission`() {
        val backend = RegistryStatsBackend { ConfigFile.isOptedIn() }
        ModuleTelemetry.clear()
        StatsApi.installBackend(backend)
        try {
            ModuleTelemetry.register("sit", "26.10.01-test")
            assertTrue(ModuleTelemetry.record(ModuleTelemetry.Event.SIT_SESSIONS_STARTED))
            val payload = StatsPusher.buildPayload()
            val result = StatsPusher.sendHttpPost(
                "http://127.0.0.1:${server.address.port}/stats", Json.encodeToString(payload), payload.serverId,
            )
            assertInstanceOf(StatsPusher.PushResult.Success::class.java, result)
            val received = Json.decodeFromString<StatsPusher.PushPayload>(receivedBody)
            assertEquals(1L, received.stats.single { it.key == "yvtils_module_sit__sessions_started_total" }.value.longValue)
            assertEquals("26.10.01-test", received.stats.single { it.key == "yvtils_module_sit__version" }.value.stringValue)
            assertEquals(payload.serverId, receivedId)
            ConfigFile.state = ConfigFile.state.copy(opt_in = false)
            assertFalse(ModuleTelemetry.record(ModuleTelemetry.Event.SIT_SESSIONS_STARTED))
            assertEquals(StatsPusher.PushResult.Skipped, send())
            assertEquals(1, requests.get())
        } finally {
            ModuleTelemetry.clear()
            backend.close()
            StatsApi.uninstallBackend(backend)
        }
    }
}
