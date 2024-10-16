package im.configs

import im.api.middlewares.authentication.AuthInterceptor
import im.api.middlewares.logging.LoggingInterceptor
import im.api.middlewares.resolvers.AuthenticatedUserArgumentResolver
import im.api.utils.RequestHelper
import im.services.auth.AuthService
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
open class WebMvcConfig(
    private val authService: AuthService,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(LoggingInterceptor())
        registry.addInterceptor(AuthInterceptor(authService, RequestHelper()))
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(AuthenticatedUserArgumentResolver())
    }
}
