package com.tt.client

import com.tt.models.BusinessId
import com.tt.plugins.TestClientProps
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun HttpClientConfig<*>.configureLogging(logging: String) =
    install(Logging) {
        logger = Logger.DEFAULT
        level = when (logging) {
            "ALL" -> LogLevel.ALL
            "BODY" -> LogLevel.BODY
            "HEADERS" -> LogLevel.HEADERS
            "INFO" -> LogLevel.INFO
            else -> LogLevel.NONE
        }
    }

@Serializable
data class TestReply(
    val value: Int
)

class TestClient(
    private val props: TestClientProps,
    engine: HttpClientEngine
) {
    private val client: HttpClient = HttpClient(engine) {
        expectSuccess = true
        configureLogging(props.logging)
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    prettyPrint = true
                }
            )
        }
    }

    suspend fun testFetch(businessId: BusinessId): TestReply =
        client.get(props.url) {
            url {
                appendPathSegments(TEST, businessId)
            }
        }.body<TestReply>()

    companion object {
        const val TEST = "test"
    }
}
