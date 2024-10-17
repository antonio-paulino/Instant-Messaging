FROM gradle:jdk21 AS build

WORKDIR /app

COPY ./code/jvm/gradle /app/gradle
COPY ./code/jvm/gradlew /app/gradlew
COPY ./code/jvm/build.gradle.kts /app/
COPY ./code/jvm/settings.gradle.kts /app/

COPY ./code/jvm /app/src

RUN ./gradlew build --no-daemon

FROM openjdk:21

WORKDIR /app

COPY --from=build /app/src/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]