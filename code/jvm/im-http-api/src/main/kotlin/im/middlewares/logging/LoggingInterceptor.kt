package im.middlewares.logging

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class LoggingInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod) {
            val controllerName = handler.beanType.simpleName
            val methodName = handler.method.name
            request.setAttribute("controllerName", controllerName)
            request.setAttribute("handlerMethod", methodName)
        }
        return true
    }
}