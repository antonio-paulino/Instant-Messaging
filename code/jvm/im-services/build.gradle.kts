plugins {
    kotlin("jvm")
    id("io.spring.dependency-management") version "1.1.6"
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":im-repository"))
    api(project(":im-domain"))
    implementation(project(":im-repository-jpa"))
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}