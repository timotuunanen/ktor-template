package com.tt.plugins

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                @OptIn(ExperimentalSerializationApi::class)
                namingStrategy = JsonNamingStrategy.SnakeCase
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
        )
    }
}
