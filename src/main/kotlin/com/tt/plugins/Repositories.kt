package com.tt.plugins

import com.tt.repositories.CompanyRepository
import org.koin.dsl.module

val myRepositories = module {
    single { initDsl(get()) }
    single { CompanyRepository() }
}
