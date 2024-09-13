package com.tt

import com.tt.plugins.clientModules
import com.tt.plugins.configureCors
import com.tt.plugins.configureRouting
import com.tt.plugins.configureSerialization
import com.tt.plugins.installAuthentication
import com.tt.plugins.installDefaultHeaders
import com.tt.plugins.installStatusPages
import com.tt.plugins.migrateDatabase
import com.tt.plugins.myDb
import com.tt.plugins.myRepositories
import com.tt.plugins.myServices
import com.tt.plugins.readConfiguration
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.resources.Resources
import mu.KotlinLogging
import org.koin.ktor.plugin.Koin

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module(engine: HttpClientEngine = OkHttp.create()) {
    configureCors()
    installDefaultHeaders()
    install(Koin) {
        modules(readConfiguration(environment.config))
        modules(myDb)
        modules(myRepositories)
        clientModules(engine)
        modules(myServices)
    }
    installAuthentication()
    install(Resources)
    migrateDatabase()
    configureSerialization()

    configureRouting()
    installStatusPages()
}
