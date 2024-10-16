package im

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["im"])
@EntityScan("im")
@EnableScheduling
open class App {
    companion object {
        private val logger = LoggerFactory.getLogger(App::class.java)
    }

    @Bean
    open fun beanPostProcessor() =
        object : BeanPostProcessor {
            override fun postProcessAfterInitialization(
                bean: Any,
                beanName: String,
            ): Any {
                val beanPackage = bean::class.java.packageName
                if (beanPackage.startsWith("im")) {
                    logger.info("Bean $beanName of package $beanPackage initialized")
                }
                return bean
            }
        }
}

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
