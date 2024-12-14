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

On the root of the code/jvm directory, run the following command:

```bash
./gradlew deploy
```

This command will build the application, and deploy both the API and the SPA.

To scale the api instances, run the following commands:

```bash
docker compose up -d --scale deploy-api=[number of instances]
docker exec -ti im-nginx bash    
nginx -s reload
```

If the number of instances is greater than 1, SSE will not work properly.

Events are handled with JPA Entity Listeners, which depend on the JPA Context specific to the instance.

If the instance with the event listener is not the one that makes the change to the database, the listener will not be triggered.

(Possible future improvement: use a static event server that receives events pushed by the instances and triggers the listeners)

## Test Data

The database will be populated with test data, including the following users:

- Username: Instant Messaging, Password: Iseldaw-g07
- Users from user_1 to user_1000000, with the password: Password123