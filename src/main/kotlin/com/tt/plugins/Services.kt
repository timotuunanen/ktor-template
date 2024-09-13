package com.tt.plugins

import com.tt.services.CompanyService
import org.koin.dsl.module

val myServices = module {
    single { CompanyService(get(), get(), get()) }
}
