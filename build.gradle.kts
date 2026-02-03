import dev.monosoul.jooq.RecommendedVersions
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val ktorVersion = "3.3.1"
val logbackVersion = "1.4.14"
val postgresqlVersion = "42.7.3"
val hikaricpVersion = "5.0.1"
val jacksonVersion = "2.15.1"
val flywayVersion = "10.13.0"
val testcontainersVersion = "1.19.7"
val kotestVersion = "5.9.1"
val kotestVersionArrow = "1.4.0"
val mockkVersion = "1.14.6"
val arrowKtVersion = "2.2.0"

plugins {
    kotlin("jvm") version "2.2.21"
    id("io.ktor.plugin") version "3.3.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.21"
    id("dev.monosoul.jooq-docker") version "5.0.6"
    id("java")
    id("org.flywaydb.flyway") version "9.22.0"
    id("com.avast.gradle.docker-compose") version "0.17.4"
    id("org.jmailen.kotlinter") version "5.1.1"
}

group = "com.tt"
version = "0.0.1"

application {
    mainClass.set("com.tt.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs =
        listOf(
            "-Dio.ktor.development=$isDevelopment",
        )
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor server
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-resources:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-sessions-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-hsts-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-default-headers-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.insert-koin:koin-ktor:3.4.3")

    // Ktor client
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-resources:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")

    // Db
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("com.zaxxer:HikariCP:$hikaricpVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")

    // Jooq
    implementation("org.jooq:jooq:${RecommendedVersions.JOOQ_VERSION}")
    implementation("org.jooq:jooq-meta:${RecommendedVersions.JOOQ_VERSION}")
    implementation("org.jooq:jooq-codegen:${RecommendedVersions.JOOQ_VERSION}")
    implementation("org.jooq:jooq-kotlin-coroutines:${RecommendedVersions.JOOQ_VERSION}")
    jooqCodegen("org.postgresql:postgresql:$postgresqlVersion")

    // Other
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.7")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Arrow.kt
    implementation("io.arrow-kt:arrow-core:$arrowKtVersion")
    implementation("io.arrow-kt:arrow-fx-coroutines:$arrowKtVersion")

    // Xml parsing
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.0")
    testImplementation("io.ktor:ktor-server-test-host:${ktorVersion}")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-framework-datatest:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:$kotestVersionArrow")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
}

dockerCompose {
    useComposeFiles.add("docker-compose.build.yml")
}

flyway {
    url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/"
    user = "postgre"
    password = "postgre"
    baselineOnMigrate = true
}

tasks {
    named("flywayMigrate") {
        dependsOn("composeUp")
    }
    generateJooqClasses {
        schemas.set(listOf("public", "other_schema"))
        basePackageName.set("org.jooq.generated")
        migrationLocations.setFromFilesystem("src/main/resources/db/migration")
        outputDirectory.set(project.layout.buildDirectory.dir("generated-jooq"))
        flywayProperties.put("flyway.placeholderReplacement", "false")
        includeFlywayTable.set(true)
        outputSchemaToDefault.add("public")
        schemaToPackageMapping.put("public", "tt")
        usingJavaConfig {
            // "this" here is the org.jooq.meta.jaxb.Generator configure it as you please
        }
    }
    named("run") {
        dependsOn("flywayMigrate")
        finalizedBy("composeDownForced")
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true

            // set options for log level DEBUG and INFO
            debug {
                exceptionFormat = TestExceptionFormat.FULL
            }
            info.exceptionFormat = debug.exceptionFormat
        }
        jvmArgs("-XX:+EnableDynamicAgentLoading")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xcontext-parameters"))
        jvmTarget.set(JvmTarget.JVM_21)
    }
    sourceSets.all {
        languageSettings {
            languageVersion = "2.0"
        }
    }
}
