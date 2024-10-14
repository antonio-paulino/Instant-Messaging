package im.api.middlewares.authentication

import im.api.utils.RequestHelper
import im.services.Failure
import im.services.Success
import im.services.auth.AuthService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Authenticates the incoming requests.
 */
@Component
class AuthFilter(
    private val authService: AuthService,
    private val reqHelper: RequestHelper,
) : HttpFilter() {
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain,
    ) {
        request as HttpServletRequest
        response as HttpServletResponse
        val uri = request.requestURI

        logger.info("Authenticating request to $uri")

        if (uri in reqHelper.getNoAuthRoutes()) {
            logger.info("Skipping authentication for $uri")
            chain.doFilter(request, response)
            return
        }

        val token = getAccessToken(request)

        if (token == null) {
            logger.info("Failed to authenticate request")
            response.status = HttpStatus.UNAUTHORIZED.value()
            return
        }

        val result = authService.authenticate(token)

        if (result is Failure) {
            logger.info("Failed to authenticate request")
            response.status = HttpStatus.UNAUTHORIZED.value()
            return
        }

        result as Success
        logger.info("Authenticated request for user with id ${result.value.id}")
        reqHelper.setAuthenticatedUser(request, (result).value, token)
        chain.doFilter(request, response)
    }

    private fun getAccessToken(request: HttpServletRequest): UUID? {
        val token =
            request.getHeader("Authorization")?.removePrefix("Bearer ")
                ?: request.cookies?.find { it.name == "access_token" }?.value
        return token?.let { UUID.fromString(it) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthFilter::class.java)
    }
}
