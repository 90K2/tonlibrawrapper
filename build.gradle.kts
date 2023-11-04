plugins {
    id("org.springframework.boot") version "2.7.8" apply false
    id("io.spring.dependency-management") version "1.1.0" apply false
    kotlin("jvm") version "1.7.22" apply false
    kotlin("plugin.spring") version "1.7.22" apply false
    kotlin("plugin.jpa") version "1.7.22" apply false
}

subprojects {
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")

    dependencies {
        val implementation by configurations
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.2")
    }

    extra["springBootVersion"] = "2.7.0"
    extra["springSecurityVersion"] = "5.7.1"
}
