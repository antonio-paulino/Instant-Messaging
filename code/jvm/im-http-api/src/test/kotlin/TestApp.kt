package im

import im.api.middlewares.resolvers.AuthenticatedUserArgumentResolver
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@SpringBootApplication
open class TestApp : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        super.addArgumentResolvers(resolvers)
        resolvers.add(AuthenticatedUserArgumentResolver())
    }
}
