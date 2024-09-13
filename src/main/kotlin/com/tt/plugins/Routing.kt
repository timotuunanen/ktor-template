package com.tt.plugins

import com.tt.routes.companyRouting
import io.ktor.server.application.Application

fun Application.configureRouting() {
    companyRouting()
}
