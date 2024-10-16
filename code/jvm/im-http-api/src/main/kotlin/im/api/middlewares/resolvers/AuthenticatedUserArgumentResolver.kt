package im.api.middlewares.resolvers

import im.api.utils.RequestHelper
import im.domain.user.AuthenticatedUser
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * Resolves the [AuthenticatedUser] object from the request.
 *
 * Used to inject the authenticated user into the controller methods.
 *
 */
@Component
class AuthenticatedUserArgumentResolver(
    private val reqHelper: RequestHelper,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.parameterType == AuthenticatedUser::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any? {
        val request = webRequest.nativeRequest as HttpServletRequest
        return reqHelper.getAuthenticatedUser(request)
    }
}
