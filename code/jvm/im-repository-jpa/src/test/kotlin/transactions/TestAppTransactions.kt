package transactions

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@EntityScan("model")
@ComponentScan(basePackages = ["repositories", "transactions"])
open class TestAppTransactions