package im

import im.middlewares.logging.LoggingInterceptor
import im.middlewares.resolvers.AuthenticatedUserArgumentResolver
import im.repository.repositories.user.UserRepository
import im.user.User
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@SpringBootApplication
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

@Configuration
open class StartupConfig(
    private val userRepository: UserRepository,
) {
    @Bean
    open fun init() = CommandLineRunner {
        if (userRepository.findAll().isEmpty()) {
            logger.info("No users found, creating test user")
            userRepository.save(
                User(
                    0L,
                    "testUser",
                    "dB0fnKFopK61h3ebruE1Sw==:pM85KqazkHyBn7iUW1-ndtm-EMIxBIAFpPMPnl9n7N8=",
                    "iseldaw@isel.pt"
                )
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(StartupConfig::class.java)
    }
}

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
