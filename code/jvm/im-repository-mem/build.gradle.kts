plugins {
    kotlin("jvm")
    id("io.spring.dependency-management") version "1.1.6"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":im-repository"))
    implementation(project(":im-domain"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
