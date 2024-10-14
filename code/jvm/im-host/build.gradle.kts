plugins {
    application
    kotlin("jvm")
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "im"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":im-http-api"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    dependsOn(":im-repository-jpa:dbTestsWait")
    finalizedBy(":im-repository-jpa:dbTestsDown")
    useJUnitPlatform()
}

val composeFileDir: Directory = rootProject.layout.projectDirectory
val dockerComposePath = composeFileDir.file("docker-compose.yml").toString()

tasks.withType<Jar> {
    dependsOn(":im-repository-jpa:dbTestsWait")
    dependsOn(":im-repository-jpa:dbTestsDown")
    finalizedBy("deploy")
}

task<Exec>("dbUp") {
    commandLine("docker", "compose", "-f", dockerComposePath, "up", "-d", "--build", "--force-recreate", "production")
}

task<Exec>("deploy") {
    commandLine("docker", "exec", "production", "/app/bin/wait-for-postgres.sh", "localhost")
    dependsOn("dbUp")
}
