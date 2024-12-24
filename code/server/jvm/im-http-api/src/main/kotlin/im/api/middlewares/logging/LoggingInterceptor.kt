package im.api.middlewares.logging

import im.api.utils.RequestHelper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class LoggingInterceptor(
    private val reqHelper: RequestHelper,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (handler is HandlerMethod) {
            val controllerName = handler.beanType.simpleName
            val methodName = handler.method.name
            reqHelper.setMethodAttribute(request, methodName)
            reqHelper.setControllerAttribute(request, controllerName)
        }
        return true
    }
}
