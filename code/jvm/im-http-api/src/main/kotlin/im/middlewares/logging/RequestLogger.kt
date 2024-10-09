package im.middlewares.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class RequestLogger : HttpFilter() {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
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

        logger.info(
            "Outgoing Response: uri={}, method={}, status={}, time={}ms",
            req.requestURI, req.method, res.status, (endTime - startTime) / 1_000_000
        )
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(RequestLogger::class.java)
    }
}