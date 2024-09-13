package com.tt

import com.tt.models.BusinessId
import com.tt.models.Company
import com.tt.routes.CompanyRoute
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.DslDrivenSpec
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestEngine
import io.ktor.server.testing.createTestEnvironment
import io.ktor.util.encodeBase64
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class ApplicationTest : FunSpec(
    {
        val user = "postgres"
        val pass = "postgres"

        val postgres = PostgreSQLContainer<Nothing>("postgres:14.1").apply {
            withUsername(user)
            withPassword(pass)
        }

        val mockEngine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/testurl/test/12345-1" -> respond(
                    content = "{\"value\": 1}",
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
                else -> respond(
                    content = "{\"value\": 1}",
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }
        }

        val engine = ktor(
            mockEngine = mockEngine,
            extraProperties = {
                put(
                    "ktor.datasource.jdbcUrl",
                    "jdbc:postgresql://${postgres.host}:${postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)}/"
                )
                put("ktor.datasource.username", user)
                put("ktor.datasource.password", pass)
            },
            postgres = postgres,
            userName = user,
            passWd = pass
        )

        fun getClient() = engine.client.config {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        @OptIn(ExperimentalSerializationApi::class)
                        namingStrategy = JsonNamingStrategy.SnakeCase
                    }
                )
            }
            install(Resources)
        }

        test("testCompanyFlow") {
            val client = getClient()
            val businessId = client.postCompany()

            client.get(CompanyRoute(businessId = businessId)) {
                basicAuthForTest()
            }.apply {
                status shouldBe HttpStatusCode.OK
                println(bodyAsText())
                val company = call.body<Company>()
                company.name shouldBe "Test company"
                company.businessId shouldBe businessId
                company.createdAt shouldNotBe null
                company.updatedAt shouldNotBe null
            }

            client.get(CompanyRoute(businessId = "bad")) {
                basicAuthForTest()
            }.apply {
                status shouldBe HttpStatusCode.NotFound
            }

            client.delete(CompanyRoute.Id(id = businessId)) {
                basicAuthForTest()
            }.apply {
                status shouldBe HttpStatusCode.OK
            }

            client.get(CompanyRoute(businessId = businessId)) {
                basicAuthForTest()
            }.apply {
                status shouldBe HttpStatusCode.NotFound
            }
        }
    }
)

suspend fun HttpClient.postCompany(
    businessId: BusinessId = "12345-1",
    name: String = "Test company"
): String {
    post(CompanyRoute()) {
        basicAuthForTest()
        contentType(ContentType.Application.Json)
        setBody(Company(businessId = businessId, name = name))
    }.apply {
        status shouldBe HttpStatusCode.Created
    }
    return businessId
}

fun DslDrivenSpec.ktor(
    engine: TestApplicationEngine = createEngine(),
    extraProperties: MapApplicationConfig.() -> Unit = { },
    mockEngine: MockEngine,
    postgres: PostgreSQLContainer<Nothing>,
    userName: String,
    passWd: String
): TestApplicationEngine {
    register(
        object : TestListener {
            override suspend fun beforeSpec(spec: Spec) {
                postgres.start()
                engine.start(wait = true)
                with(engine.application) {
                    setApplicationProperties(extraProperties)
                    module(mockEngine)
                }
            }

            override suspend fun afterEach(testCase: TestCase, result: TestResult) {
                super.afterEach(testCase, result)
                HikariDataSource(
                    HikariConfig().apply {
                        driverClassName = "org.postgresql.Driver"
                        username = userName
                        password = passWd
                        jdbcUrl =
                            "jdbc:postgresql://${postgres.host}:${postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)}/"
                    }
                ).run {
                    DSL.using(this, SQLDialect.POSTGRES).apply {
                        truncate(org.jooq.generated.tt.tables.Company.COMPANY).cascade().execute()
                    }
                }
            }

            override suspend fun afterSpec(spec: Spec) {
                engine.stop(1000, 1000)
                postgres.stop()
            }
        }
    )
    return engine
}

fun createEngine() = TestEngine.create(createTestEnvironment()) {}
private fun Application.setApplicationProperties(extraProperties: MapApplicationConfig.() -> Unit) {
    (environment.config as MapApplicationConfig).apply {
        put("ktor.datasource.driverClassName", "org.postgresql.Driver")
        put("ktor.datasource.schema", "public")
        put("ktor.customDatasourceCreationRetryLogic.maxRetries", "10")
        put("ktor.customDatasourceCreationRetryLogic.retryInterval", "2000")
        put("ktor.cors.allowedHost", "localhost:3000")
        put("ktor.environment", "unit_test")
        put("basicAuth.username", "username")
        put("basicAuth.password", "password")
        put("testClient.url", "testurl")
        put("testClient.username", "username")
        put("testClient.password", "password")
        put("testClient.logging", "TRACE")
        extraProperties()
    }
}

fun HttpRequestBuilder.basicAuthForTest(): Unit =
    header(HttpHeaders.Authorization, "Basic ${"username:password".encodeBase64()}")
