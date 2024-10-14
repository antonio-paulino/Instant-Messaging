package im

import im.api.middlewares.logging.LoggingInterceptor
import im.api.middlewares.resolvers.AuthenticatedUserArgumentResolver
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@SpringBootApplication(scanBasePackages = ["im"])
@EntityScan("im")
@EnableScheduling
open class App : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        super.addInterceptors(registry)
        registry.addInterceptor(LoggingInterceptor())
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        super.addArgumentResolvers(resolvers)
        resolvers.add(AuthenticatedUserArgumentResolver())
    }
}

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
