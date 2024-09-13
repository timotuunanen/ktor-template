package com.tt.plugins

import io.ktor.server.config.ApplicationConfig
import mu.KotlinLogging
import org.koin.dsl.module
import java.util.Properties

private val logger = KotlinLogging.logger {}

fun readConfiguration(config: ApplicationConfig) = module {
    single { RetryConfigProps(config) }
    single { DbConfigProps(config) }
    single { BasicAuthenticationProps(config) }
    single { TestClientProps(config) }
}

class RetryConfigProps(config: ApplicationConfig) {
    val maxRetries: Int
    val retryInterval: Long

    init {
        config.config("ktor.customDatasourceCreationRetryLogic").toMap().let {
            maxRetries = (it["maxRetries"].toString()).toInt()
            retryInterval = (it["retryInterval"].toString()).toLong()
        }
    }
}

class DbConfigProps(private val config: ApplicationConfig) {
    fun getConfig(): Properties = Properties().apply {
        config.config("ktor.datasource").toMap().forEach {
            setProperty(it.key, it.value.toString())
        }
    }
}

class BasicAuthenticationProps(config: ApplicationConfig) {
    val username: String
    val password: String

    init {
        config.config("basicAuth").toMap().let {
            username = it["username"].toString()
            password = it["password"].toString()
        }
    }
}

class TestClientProps(config: ApplicationConfig) {
    val username: String
    val password: String
    val url: String
    val logging: String

    init {
        config.config("testClient").toMap().let {
            username = it["username"].toString()
            password = it["password"].toString()
            url = it["url"].toString()
            logging = it["logging"].toString()
        }
    }
}

class KtorProps(config: ApplicationConfig) {
    val environment: String

    init {
        config.config("ktor").toMap().let {
            environment = it["environment"].toString()
        }
    }

    fun isUnitTest() = environment == "unit_test"

    fun isLocal() = environment == "local"
}
