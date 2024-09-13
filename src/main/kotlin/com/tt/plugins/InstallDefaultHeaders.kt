package com.tt.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.defaultheaders.DefaultHeaders

fun Application.installDefaultHeaders() =
    install(DefaultHeaders) {
        header("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload")
    }
