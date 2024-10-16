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
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")
    implementation(kotlin("reflect"))

    // for testing
    testImplementation("org.postgresql:postgresql:42.7.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.4")
    testImplementation(project(":im-repository-mem"))
    testImplementation(project(":im-repository-jpa"))

    testImplementation(kotlin("test"))
}

tasks.test {
    environment("DB_URL", "jdbc:postgresql://localhost:5432/iseldawdev?user=isel&password=isel")
    useJUnitPlatform()
    dependsOn(":im-repository-jpa:dbTestsWait")
    finalizedBy(":im-repository-jpa:dbTestsDown")
}

kotlin {
    jvmToolchain(21)
}
