package com.tt.plugins

import com.tt.client.TestClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.KoinApplication
import org.koin.dsl.module

fun KoinApplication.clientModules(engine: HttpClientEngine) = modules(
    module {
        single { TestClient(get(), engine) }
    }
)
