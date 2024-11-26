package im.api.middlewares.authentication

import im.api.utils.RequestHelper
import im.domain.Failure
import im.domain.Success
import im.services.auth.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

/**
 * Authenticates the incoming requests.
 */
@Component
class AuthInterceptor(
    private val authService: AuthService,
    private val reqHelper: RequestHelper,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val controller = if (handler is HandlerMethod) handler.beanType else null
        val method = if (handler is HandlerMethod) handler.method else null

        if (controller?.isAnnotationPresent(Authenticated::class.java) == false &&
            method?.isAnnotationPresent(Authenticated::class.java) == false ||
            request.method == "OPTIONS"
        ) {
            return true
        }

        val token = reqHelper.getAccessToken(request)

        if (token == null) {
            logger.info("Failed to authenticate request")
            response.status = HttpStatus.UNAUTHORIZED.value()
            return false
        }

        val result = authService.authenticate(token)

        if (result is Failure) {
            logger.info("Failed to authenticate request")
            response.status = HttpStatus.UNAUTHORIZED.value()
            return false
        }

        result as Success
        logger.info("Authenticated request for user with id ${result.value.id}")
        reqHelper.setAuthenticatedUser(request, (result).value, token)
        return true
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthInterceptor::class.java)
    }
}
