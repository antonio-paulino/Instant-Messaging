package im.middlewares.authentication

import im.services.Failure
import im.services.Success
import im.services.auth.AuthService
import im.utils.RequestHelper
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.util.*

@Component
class AuthFilter(
    private val authService: AuthService,
    private val reqHelper: RequestHelper
) : HttpFilter() {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        request as HttpServletRequest
        response as HttpServletResponse
        val uri = request.requestURI

        if (uri.startsWith("/auth/register") || uri.startsWith("/auth/login") || uri.startsWith("/auth/refresh")) {
            chain.doFilter(request, response)
            return
        }

        val token = getAccessToken(request)

        if (token == null) {
            response.status = HttpStatus.UNAUTHORIZED.value()
            return
        }

        val result = authService.authenticate(token)

        if (result is Failure) {
            response.status = HttpStatus.UNAUTHORIZED.value()
            return
        }

        reqHelper.setAuthenticatedUser(request, (result as Success).value, token)
        chain.doFilter(request, response)
    }

    private fun getAccessToken(request: HttpServletRequest): UUID? {
        val token = request.getHeader("Authorization")?.removePrefix("Bearer ")
            ?: request.cookies?.find { it.name == "access_token" }?.value
        return token?.let { UUID.fromString(it) }
    }

}