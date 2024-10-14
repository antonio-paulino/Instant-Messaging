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
    testImplementation("org.postgresql:postgresql:42.7.2")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa:3.3.4")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.4")
    testImplementation(project(":im-repository-mem"))
    testImplementation(project(":im-repository-jpa"))
}

tasks.test {
    useJUnitPlatform()
    dependsOn(":im-repository-jpa:dbTestsWait")
    finalizedBy(":im-repository-jpa:dbTestsDown")
}

kotlin {
    jvmToolchain(21)
}
