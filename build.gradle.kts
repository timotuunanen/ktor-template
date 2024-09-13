import dev.monosoul.jooq.RecommendedVersions
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val ktor_version = "2.3.11"
val kotlin_version = "1.9.10"
val logback_version = "1.4.14"
val postgresql_version = "42.7.3"
val hikaricp_version = "5.0.1"
val jackson_version = "2.15.1"
val flyway_version = "10.13.0"
val testcontainers_version = "1.19.7"
val kotest_version = "5.7.2"
val kotest_version_arrow = "1.4.0"
val mockk_version = "1.13.12"
val arrow_kt_version = "1.2.0"

plugins {
    kotlin("jvm") version "2.0.0"
    id("io.ktor.plugin") version "2.3.4"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
    id("dev.monosoul.jooq-docker") version "5.0.6"
    id("java")
    id("org.flywaydb.flyway") version "9.22.0"
    id("com.avast.gradle.docker-compose") version "0.17.4"
    id("org.jlleitschuh.gradle.ktlint") version "11.5.1"
}

group = "com.tt"
version = "0.0.1"

application {
    mainClass.set("com.tt.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf(
        "-Dio.ktor.development=$isDevelopment",
    )
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor server
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-resources:$ktor_version")
    implementation("io.ktor:ktor-server-cors:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-sessions-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-hsts-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-default-headers-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("io.insert-koin:koin-ktor:3.4.3")

    // Ktor client
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-client-resources:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-okhttp:$ktor_version")
    implementation("io.ktor:ktor-client-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-auth:$ktor_version")

    // Db
    implementation("org.postgresql:postgresql:$postgresql_version")
    implementation("com.zaxxer:HikariCP:$hikaricp_version")
    implementation("org.flywaydb:flyway-core:$flyway_version")
    implementation("org.flywaydb:flyway-database-postgresql:$flyway_version")

    // Jooq
    implementation("org.jooq:jooq:${RecommendedVersions.JOOQ_VERSION}")
    implementation("org.jooq:jooq-meta:${RecommendedVersions.JOOQ_VERSION}")
    implementation("org.jooq:jooq-codegen:${RecommendedVersions.JOOQ_VERSION}")
    implementation("org.jooq:jooq-kotlin-coroutines:${RecommendedVersions.JOOQ_VERSION}")
    jooqCodegen("org.postgresql:postgresql:$postgresql_version")

    // Other
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // Arrow.kt
    implementation("io.arrow-kt:arrow-core:$arrow_kt_version")
    implementation("io.arrow-kt:arrow-fx-coroutines:$arrow_kt_version")

    // Xml parsing
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jackson_version")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jackson_version")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jackson_version")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.0")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainers_version")
    testImplementation("org.testcontainers:testcontainers:$testcontainers_version")
    testImplementation("org.testcontainers:postgresql:$testcontainers_version")
    testImplementation("io.kotest:kotest-runner-junit5:$kotest_version")
    testImplementation("io.kotest:kotest-framework-datatest:$kotest_version")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:$kotest_version_arrow")
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("io.mockk:mockk:$mockk_version")
    testImplementation("io.ktor:ktor-client-mock:$ktor_version")
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
            /* "this" here is the org.jooq.meta.jaxb.Generator configure it as you please */
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
        freeCompilerArgs.set(listOf("-Xcontext-receivers"))
        jvmTarget.set(JvmTarget.JVM_21)
    }
    sourceSets.all {
        languageSettings {
            languageVersion = "2.0"
        }
    }
}
