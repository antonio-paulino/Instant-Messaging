package im.api.middlewares.ratelimit

import im.api.utils.RequestHelper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RateLimiter(
    private val reqHelper: RequestHelper,
) : HandlerInterceptor, HttpFilter() {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val controller = if (handler is HandlerMethod) handler.beanType else null
        val method = if (handler is HandlerMethod) handler.method else null

        val rateLimit =
            method?.getAnnotation(RateLimit::class.java)
                ?: controller?.getAnnotation(RateLimit::class.java)

        if (rateLimit != null) {
            val clientIp = reqHelper.getClientIp(request)
            val key = "$clientIp:${method?.name ?: controller?.name}"
            val currentTime = System.currentTimeMillis()

            request.setAttribute(RATE_LIMIT_KEY, key)

            val (count, lastTime) = requestCounts.getOrPut(key) { 0L } to timeWindows.getOrPut(key) { 0L }

            if (currentTime - lastTime > TimeUnit.SECONDS.toMillis(1)) {
                requestCounts[key] = 0L
                timeWindows[key] = currentTime
            }

            if (count >= rateLimit.limitSeconds) {
                response.status = HttpStatus.TOO_MANY_REQUESTS.value()
                response.addHeader("Retry-After", "1")
                return false
            }
        }
        return true
    }

    override fun doFilter(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        chain.doFilter(request, response)
        val key = request.getAttribute(RATE_LIMIT_KEY) as String?
        key?.let { requestCounts.computeIfPresent(it) { _, v -> v + 1 } }
    }

    companion object {
        private val requestCounts = ConcurrentHashMap<String, Long>()
        private val timeWindows = ConcurrentHashMap<String, Long>()
        private const val RATE_LIMIT_KEY = "rateLimitKey"
    }
}
