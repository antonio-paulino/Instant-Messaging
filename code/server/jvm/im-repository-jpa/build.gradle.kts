plugins {
    kotlin("jvm")
    id("io.spring.dependency-management") version "1.1.6"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.3.4")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation(project(":im-repository"))
    implementation(project(":im-domain"))
    implementation(kotlin("reflect"))

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.4")
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

val composeFileDir: Directory = rootProject.layout.projectDirectory.dir("../..")
val dockerComposePath = composeFileDir.file("docker-compose.yml").toString()

task<Exec>("dbTestsUp") {
    commandLine("docker", "compose", "-f", dockerComposePath, "up", "-d", "--build", "--force-recreate", "db-test")
}

task<Exec>("dbTestsWait") {
    commandLine("docker", "exec", "db-test", "/app/bin/wait-for-postgres.sh", "localhost")
    dependsOn("dbTestsUp")
}

task<Exec>("dbTestsDown") {
    commandLine("docker", "compose", "-f", dockerComposePath, "down", "db-test")
}
