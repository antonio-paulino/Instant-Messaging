package im.utils

import im.model.input.AuthenticatedUser
import im.tokens.AccessToken
import im.tokens.RefreshToken
import im.user.User
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.util.*

private const val ACCESS_TOKEN_COOKIE_NAME = "access_token"
private const val REFRESH_TOKEN_COOKIE_NAME = "refresh_token"
private const val AUTHENTICATED_USER_ATTRIBUTE = "user"

@Component
class RequestHelper {

    fun setAuthenticatedUser(req: HttpServletRequest, user: User, accessToken: UUID) {
        req.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, AuthenticatedUser(user, accessToken))
    }

    fun getRefreshToken(req: HttpServletRequest): UUID? {
        val refreshToken = req.getHeader("Authorization")?.removePrefix("Bearer ")
            ?: req.cookies?.find { it.name == REFRESH_TOKEN_COOKIE_NAME }?.value
        return refreshToken?.let { UUID.fromString(it) }
    }

    fun setCookie(res: HttpServletResponse, accessToken: AccessToken) {
        val cookie = Cookie(ACCESS_TOKEN_COOKIE_NAME, accessToken.token.toString())
            .apply {
                maxAge = accessToken.expiresAt.toEpochSecond(ZoneOffset.UTC).toInt()
                isHttpOnly = true
                secure = true
                path = "/"
            }
        res.addCookie(cookie)
    }

    fun setCookie(res: HttpServletResponse, refreshToken: RefreshToken) {
        val cookie = Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken.token.toString())
            .apply {
                maxAge = refreshToken.session.expiresAt.toEpochSecond(ZoneOffset.UTC).toInt()
                isHttpOnly = true
                secure = true
                path = "/"
            }
        res.addCookie(cookie)
    }

    fun clearRefreshToken(res: HttpServletResponse) {
        val cookie = Cookie(REFRESH_TOKEN_COOKIE_NAME, "")
            .apply {
                maxAge = 0
                isHttpOnly = true
                secure = true
                path = "/"
            }
        res.addCookie(cookie)
    }

    fun clearAccessToken(res: HttpServletResponse) {
        val cookie = Cookie(ACCESS_TOKEN_COOKIE_NAME, "")
            .apply {
                maxAge = 0
                isHttpOnly = true
                secure = true
                path = "/"
            }
        res.addCookie(cookie)
    }

}