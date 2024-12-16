package im.configs

import im.api.middlewares.authentication.AuthInterceptor
import im.api.middlewares.logging.LoggingInterceptor
import im.api.middlewares.ratelimit.RateLimiter
import im.api.middlewares.resolvers.AuthenticatedUserArgumentResolver
import im.api.utils.RequestHelper
import im.services.auth.AuthService
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
open class WebMvcConfig(
    private val authService: AuthService,
) : WebMvcConfigurer {
    private val requestHelper = RequestHelper()

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(LoggingInterceptor(requestHelper))
        registry.addInterceptor(RateLimiter(requestHelper))
        registry.addInterceptor(AuthInterceptor(authService, requestHelper))
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(AuthenticatedUserArgumentResolver(requestHelper))
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:8000")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}
