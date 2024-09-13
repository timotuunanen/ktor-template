package com.tt.plugins

import arrow.core.Either
import com.tt.models.Failure
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import mu.KotlinLogging

val clientResponseLogger = KotlinLogging.logger {}

suspend inline fun <reified T> ApplicationCall.makeResponse(
    result: Either<Failure, T>,
    httpStatusCode: HttpStatusCode = HttpStatusCode.OK,
    logFailureAsError: Boolean = true
) {
    result.onLeft {
        logFailure(it, logFailureAsError)
        respond(it.code, it.message)
    }
    result.onRight {
        clientResponseLogger.info("Responding success for ${request.uri}")
        respond(httpStatusCode, it as Any)
    }
}

fun ApplicationCall.logFailure(failure: Failure, logFailureAsError: Boolean = true) =
    if (logFailureAsError) {
        clientResponseLogger.error(
            "Responding failure to ${request.uri} msg: " + failure.message +
                if (failure.cause != null) " cause: " + failure.cause else ""
        )
    } else {
        clientResponseLogger.info(
            "Responding failure to ${request.uri} msg: " + failure.message +
                if (failure.cause != null) " cause: " + failure.cause else ""
        )
    }
