plugins {
    application
    kotlin("jvm")
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "im"
version = "0.0.1-SNAPSHOT"

application {
    mainClass.set("im.AppKt")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":im-services"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // For tests
    testImplementation("org.postgresql:postgresql:42.7.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.4")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")

    testImplementation(project(":im-repository-mem"))
    testImplementation(project(":im-repository-jpa"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    environment("DB_URL", "jdbc:postgresql://localhost:5432/iseldawdev?user=isel&password=isel")
    useJUnitPlatform()
    dependsOn(":im-repository-jpa:dbTestsWait")
    finalizedBy(":im-repository-jpa:dbTestsDown")
}
