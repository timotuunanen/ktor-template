package com.tt.plugins

import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun Application.configureCors() {
    install(CORS) {
        val allowedHost = this@configureCors.environment.config.config("ktor.cors").property("allowedHost").getString()
        logger.info("allowedHost: $allowedHost")
        allowNonSimpleContentTypes = true
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHost(allowedHost, schemes = listOf("http", "https"))
        allowCredentials = true
    }
}
