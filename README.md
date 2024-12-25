# Instant Messaging - DAW

> Instant Messaging is a web application that allows its users to send messages in real time through channels.

---

This project currently features a Web API, developed in Kotlin with the use of Spring Boot 
and a Single Page Application, developed in TypeScript with the use of React and Material-UI.

## Documentation

The documentation for this project can be found in the `docs` directory.
- [Application Documentation](docs/README.md)
- [API Specification](docs/instant-messaging-api-spec.yml)
- [Problems](docs/problems)
---

## Authors

- [50493 Bernardo Pereira](https://github.com/BernardoPe)
- [50512 António Paulino](https://github.com/antonio-paulino)

# Discussion Information

## Deployment

To deploy the application, Docker and Gradle are required.

On the root of the code/server/jvm directory, run the following command:

```bash
./gradlew deploy
```

This command will build the application, and deploy both the API and the SPA.

You can also try the application at `https://fleet-stud-easy.ngrok-free.app`. 

It has a demo user with the following credentials:
- Username: `Instant Messaging`
- Password: `Iseldaw-g07`

(Do not submit any sensitive information while using the application)
