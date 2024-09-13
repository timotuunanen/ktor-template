package com.tt.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module
import javax.sql.DataSource

val myDb = module {
    single<DataSource> { createHikariDataSource(get(), get()) }
    single { initDsl(get()) }
}

private fun buildFlyway(dataSource: DataSource) = Flyway.configure().dataSource(dataSource).load()

fun migrateDatabase(): MigrateResult = object : KoinComponent {
    val dataSource: DataSource by inject()
    fun migrate() = buildFlyway(dataSource).migrate()
}.migrate()

fun initDsl(ds: DataSource): DSLContext =
    DSL.using(ds, SQLDialect.POSTGRES)

private fun createHikariDataSource(
    config: DbConfigProps,
    retryConfig: RetryConfigProps
): DataSource {
    var retries = 0

    while (true) {
        try {
            return HikariDataSource(HikariConfig(config.getConfig()))
        } catch (exception: Exception) {
            if (retries >= retryConfig.maxRetries) {
                throw exception
            }
            println("Retry hikari data source creation, ${exception.javaClass}")
            Thread.sleep(retryConfig.retryInterval)
            retries += 1
        }
    }
}
