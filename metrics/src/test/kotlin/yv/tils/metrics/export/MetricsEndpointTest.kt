/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.metrics.export

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class MetricsEndpointTest {
    @Test fun `non loopback bind without authentication is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { MetricsEndpoint("0.0.0.0", 0, null) { byteArrayOf() } }
    }

    @Test fun `endpoint authenticates and only serves GET metrics`() {
        MetricsEndpoint("127.0.0.1", 0, "secret") { "example 1\n".toByteArray() }.use { endpoint ->
            val client = HttpClient.newHttpClient()
            fun request(path: String = "/metrics", token: String? = null, method: String = "GET"): HttpResponse<String> {
                val builder = HttpRequest.newBuilder(URI("http://127.0.0.1:${endpoint.port}$path"))
                    .method(method, HttpRequest.BodyPublishers.noBody())
                token?.let { builder.header("Authorization", "Bearer $it") }
                return client.send(builder.build(), HttpResponse.BodyHandlers.ofString())
            }
            assertEquals(401, request().statusCode())
            assertEquals(401, request(token = "wrong").statusCode())
            assertEquals("example 1\n", request(token = "secret").body())
            assertEquals(405, request(token = "secret", method = "POST").statusCode())
            assertEquals(404, request(path = "/metrics/other", token = "secret").statusCode())
        }
    }
}
