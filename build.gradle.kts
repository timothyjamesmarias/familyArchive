buildscript {
    dependencies {
        classpath("org.postgresql:postgresql:42.7.5")
        classpath("org.flywaydb:flyway-database-postgresql:11.14.1")
    }
}

plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.2.21"
    id("com.github.node-gradle.node") version "7.1.0"
    id("org.flywaydb.flyway") version "11.14.1"
}

group = "com.timothymarias"
version = "0.0.1-SNAPSHOT"
description = "familyArchive"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Storage and media processing
    implementation(platform("software.amazon.awssdk:bom:2.29.29"))
    implementation("software.amazon.awssdk:s3")
    implementation("net.coobird:thumbnailator:0.4.20")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("com.h2database:h2")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Node.js configuration for Vite frontend build
node {
    download = true
    version = "22.12.0"
    npmVersion = "10.9.2"
    workDir = file("${project.projectDir}/.gradle/nodejs")
    npmWorkDir = file("${project.projectDir}/.gradle/npm")
    nodeProjectDir = file("${project.projectDir}")
}

// Install npm dependencies
val npmInstall by tasks.getting(com.github.gradle.node.npm.task.NpmInstallTask::class)

// Build frontend with Vite
val buildFrontend by tasks.registering(com.github.gradle.node.npm.task.NpmTask::class) {
    dependsOn(npmInstall)
    args.set(listOf("run", "build"))
    inputs.dir("src/main/frontend")
    inputs.files("vite.config.ts", "tailwind.config.js", "postcss.config.js")
    outputs.dir("src/main/resources/static/dist")
}

// Ensure frontend is built before processing resources
tasks.named("processResources") {
    dependsOn(buildFrontend)
}

// Clean frontend build outputs
tasks.named("clean") {
    doLast {
        delete("src/main/resources/static/dist")
        delete(".gradle/nodejs")
        delete(".gradle/npm")
    }
}

// Flyway configuration for Gradle tasks
flyway {
    url = "jdbc:postgresql://localhost:5432/familyarchive"
    user = "familyarchive"
    password = "familyarchive"
    locations = arrayOf("classpath:db/migration")
}
