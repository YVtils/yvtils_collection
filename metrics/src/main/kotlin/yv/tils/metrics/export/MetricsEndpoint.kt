/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.metrics.export

import com.sun.net.httpserver.HttpServer
import io.prometheus.metrics.expositionformats.PrometheusTextFormatWriter
import java.net.InetAddress
import java.net.InetSocketAddress
import java.security.MessageDigest
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/** Serves pre-rendered bytes only. No Bukkit or SQL access, even if the server thread is frozen. */
class MetricsEndpoint(host: String, port: Int, token: String?, private val body: () -> ByteArray) : AutoCloseable {
    private val executor = ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, ArrayBlockingQueue(16), { runnable ->
        Thread(runnable, "yvtils-metrics-http").apply { isDaemon = true }
    })
    private val server: HttpServer
    val port: Int get() = server.address.port

    init {
        val address = InetAddress.getByName(host)
        require(address.isLoopbackAddress || !token.isNullOrBlank()) {
            "A non-loopback metrics endpoint requires bearerTokenEnvironment. Use TLS or a private network."
        }
        require(token == null || token.isNotBlank()) { "The metrics bearer token must not be blank" }
        val expected = token?.let { "Bearer $it".toByteArray(Charsets.UTF_8) }
        server = HttpServer.create(InetSocketAddress(address, port), 16)
        try {
            server.executor = executor
            server.createContext("/metrics") { exchange ->
                exchange.use {
                    val authorization = exchange.requestHeaders.getFirst("Authorization").orEmpty()
                    val authenticated = expected == null || (authorization.length <= 4096 &&
                        MessageDigest.isEqual(expected, authorization.toByteArray(Charsets.UTF_8)))
                    val status: Int
                    val payload: ByteArray
                    when {
                        !authenticated -> {
                            exchange.responseHeaders.set("WWW-Authenticate", "Bearer")
                            status = 401; payload = "Unauthorized\n".toByteArray()
                        }
                        exchange.requestURI.path != "/metrics" -> {
                            status = 404; payload = "Not found\n".toByteArray()
                        }
                        exchange.requestMethod != "GET" -> {
                            exchange.responseHeaders.set("Allow", "GET")
                            status = 405; payload = "Method not allowed\n".toByteArray()
                        }
                        else -> {
                            status = 200; payload = body()
                        }
                    }
                    exchange.responseHeaders.set("Content-Type", PrometheusTextFormatWriter.CONTENT_TYPE)
                    exchange.responseHeaders.set("Cache-Control", "no-store")
                    exchange.sendResponseHeaders(status, payload.size.toLong())
                    exchange.responseBody.use { it.write(payload) }
                }
            }
            server.start()
        } catch (failure: Throwable) {
            server.stop(0)
            executor.shutdownNow()
            throw failure
        }
    }

    override fun close() {
        server.stop(0)
        executor.shutdownNow()
    }
}
