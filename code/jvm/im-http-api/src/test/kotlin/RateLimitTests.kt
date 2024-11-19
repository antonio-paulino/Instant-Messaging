package im

import im.api.controllers.AuthController
import im.api.controllers.ChannelsController
import im.api.controllers.InvitationsController
import im.api.controllers.MessagesController
import im.api.controllers.UserController
import im.api.middlewares.ratelimit.RateLimit
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ActiveProfiles("rateLimit")
open class RateLimitTests {
    @LocalServerPort
    protected var port: Int = 0

    private fun getBaseUrl(): String = "http://localhost:$port"

    private fun getClient() = WebTestClient.bindToServer().baseUrl(getBaseUrl()).build()

    @Test
    fun `auth controller rate limit test`() {
        assertHasAnnotation(AuthController::class.java, RateLimit::class.java)

        val client = getClient()

        repeat(10) {
            client.post().uri("api/auth/register").exchange()
        }

        client.post()
            .uri("api/auth/register")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
            .expectHeader()
            .valueEquals("Retry-After", "1")

        Thread.sleep(1000)

        client.post().uri("api/auth/register").exchange().expectStatus().value { it != HttpStatus.TOO_MANY_REQUESTS.value() }
    }

    @Test
    fun `user controller rate limit test`() {
        assertHasAnnotation(UserController::class.java, RateLimit::class.java)

        val client = getClient()

        repeat(10) {
            client.get().uri("api/users").exchange()
        }

        client.get()
            .uri("api/users")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
            .expectHeader()
            .valueEquals("Retry-After", "1")

        Thread.sleep(1000)

        client.get().uri("api/users").exchange().expectStatus().value { it != HttpStatus.TOO_MANY_REQUESTS.value() }
    }

    @Test
    fun `channel controller rate limit test`() {
        assertHasAnnotation(ChannelsController::class.java, RateLimit::class.java)

        val client = getClient()

        repeat(10) {
            client.post().uri("api/channels").exchange()
        }

        client.post()
            .uri("api/channels")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
            .expectHeader()
            .valueEquals("Retry-After", "1")

        Thread.sleep(1000)

        client.post().uri("api/channels").exchange().expectStatus().value { it != HttpStatus.TOO_MANY_REQUESTS.value() }
    }

    @Test
    fun `message controller rate limit test`() {
        assertHasAnnotation(MessagesController::class.java, RateLimit::class.java)

        val client = getClient()

        repeat(10) {
            client.get().uri("api/channels/1/messages/1").exchange()
        }

        client.get()
            .uri("api/channels/1/messages/1")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
            .expectHeader()
            .valueEquals("Retry-After", "1")
        Thread.sleep(1000)

        client.get().uri("api/channels/1/messages/1").exchange().expectStatus().value { it != HttpStatus.TOO_MANY_REQUESTS.value() }
    }

    @Test
    fun `invitation controller rate limit test`() {
        assertHasAnnotation(InvitationsController::class.java, RateLimit::class.java)

        val client = getClient()

        repeat(10) {
            client.get().uri("api/channels/1/invitations").exchange()
        }

        client.get()
            .uri("api/channels/1/invitations")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
            .expectHeader()
            .valueEquals("Retry-After", "1")

        Thread.sleep(1000)

        client.get().uri("api/channels/1/invitations").exchange().expectStatus().value { it != HttpStatus.TOO_MANY_REQUESTS.value() }
    }

    private fun assertHasAnnotation(
        clazz: Class<*>,
        annotation: Class<out Annotation>,
    ) {
        assertTrue(clazz.isAnnotationPresent(annotation))
    }
}
