package com.glavatskikh.aiadvent2025.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class HttpApiClient {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 60000
        }

        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun post(
        url: String,
        body: Any,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse {
        return httpClient.post(url) {
            setBody(body)
            headers.forEach { (key, value) ->
                header(key, value)
            }
        }
    }

    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse {
        return httpClient.get(url) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
        }
    }

    suspend fun put(
        url: String,
        body: Any,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse {
        return httpClient.put(url) {
            setBody(body)
            headers.forEach { (key, value) ->
                header(key, value)
            }
        }
    }

    suspend fun delete(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse {
        return httpClient.delete(url) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
        }
    }

    fun close() {
        httpClient.close()
    }
}