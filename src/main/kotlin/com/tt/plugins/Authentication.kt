package com.tt.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.basic
import io.ktor.server.routing.Route
import mu.KotlinLogging
import org.koin.ktor.ext.inject

private val logger = KotlinLogging.logger {}

private const val BASIC_AUTH = "ttBasicAuth"

fun Application.installAuthentication() {
    val basicAuthenticationProps by inject<BasicAuthenticationProps>()

    install(Authentication) {
        basic(name = BASIC_AUTH) {
            realm = "Basic"
            validate { credentials ->
                if (checkBasicCredentials(credentials.name, credentials.password, basicAuthenticationProps)) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
}

private fun checkBasicCredentials(username: String, password: String, basicAuthenticationProps: BasicAuthenticationProps): Boolean =
    username == basicAuthenticationProps.username && password == basicAuthenticationProps.password

fun Route.checkBasicAuthenticated(build: Route.() -> Unit): Route =
    authenticate(BASIC_AUTH, build = build)
