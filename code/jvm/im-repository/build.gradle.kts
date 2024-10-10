plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":im-domain"))
    testImplementation(kotlin("test"))

    // for testing
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa:3.3.4")
    testImplementation("org.postgresql:postgresql:42.7.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.4")
    testImplementation(project(":im-repository-mem"))
    testImplementation(project(":im-repository-jpa"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}