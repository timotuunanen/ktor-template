package com.tt.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun Application.installStatusPages() {
    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            logger.error("Bad request: ", cause)
            call.respondText(status = HttpStatusCode.BadRequest, text = cause.message ?: "")
        }

        exception<Throwable> { call, _ ->
            call.respondText(status = HttpStatusCode.InternalServerError, text = "Unexpected error")
        }
    }
}
