plugins {
    kotlin("jvm") version "1.9.25" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("com.adarshr.test-logger") version "4.0.0"
}

repositories {
    mavenCentral()
}

allprojects {
    if (name != "im-database") {
        apply(plugin = "org.jlleitschuh.gradle.ktlint")
        apply(plugin = "com.adarshr.test-logger")
    }
}
