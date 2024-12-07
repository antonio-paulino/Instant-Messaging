# Build and run instructions for the entire project

## Prerequisites

- Docker
- Gradle
- Java 21

## Build and Run

To build and run the application, navigate to the `code/jvm` directory and run the following command:

```shell
./gradlew deploy 
```

This will build the application and deploy it to a local Docker container. The application will be available at `http://localhost:8000`.

Documentation for the Frontend can be found [here](../../docs/README.md).
```
