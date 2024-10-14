package im.api.middlewares.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered.LOWEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(LOWEST_PRECEDENCE)
class RequestLogger : HttpFilter() {
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain,
    ) {
        val req = request as HttpServletRequest
        val res = response as HttpServletResponse

        logger.info(
            "Incoming Request: uri={}, method={} ",
            req.requestURI,
            req.method,
        )

        val startTime = System.nanoTime()
        chain.doFilter(request, response)
        val endTime = System.nanoTime()
        val method = req.getAttribute("handlerMethod")
        val controller = req.getAttribute("controllerName")

        logger.info(
            "Outgoing Response: uri={}, method={}, controller={}, handler={}, status={}, duration={}ms",
            req.requestURI,
            req.method,
            controller,
            method,
            res.status,
            (endTime - startTime) / 1_000_000,
        )
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(RequestLogger::class.java)
    }
}
