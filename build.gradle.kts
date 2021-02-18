import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("org.springframework.boot") version "2.2.0.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    id("org.jmailen.kotlinter") version "2.1.1"
    id("org.owasp.dependencycheck") version "5.3.0"
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.spring") version "1.4.30"
}

group = "io.emiliendev"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

object DependencyVersions {

}

dependencies {
    implementation("io.fabric8:kubernetes-client:5.1.0") {
        exclude(group = "com.fasterxml.jackson.core", module = "jackson-annotations")
        exclude(group = "com.fasterxml.jackson.core", module = "jackson-databind")
        exclude(group = "com.fasterxml.jackson.datatype", module = "jackson-datatype-jsr310")
    }
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.12.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.0")

    implementation("ch.qos.logback:logback-classic:1.0.0")
    implementation("ch.qos.logback:logback-core:1.0.0")
    implementation("io.github.microutils:kotlin-logging:1.11.5")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    "lintKotlinMain"(LintTask::class) {
        exclude("**/generated/**")
    }
}

dependencyCheck {
    format = org.owasp.dependencycheck.reporting.ReportGenerator.Format.ALL
    suppressionFile = "ignore_cve.xml"
    outputDirectory = "target"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlinter {
    version = "0.34.2"
    reporters = arrayOf("checkstyle", "plain")
}
