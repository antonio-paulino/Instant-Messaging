package im

import im.repository.jpa.transactions.TransactionManagerJpa
import im.repository.mem.transactions.MemTransactionManager
import im.repository.repositories.transactions.TransactionManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
class TestConfig(
    private val transactionManagerJpa: TransactionManagerJpa,
) {
    @Profile("Jpa")
    @Bean
    @Primary
    fun trxManagerJpa(): TransactionManager = transactionManagerJpa

    @Profile("inMem")
    @Bean
    @Primary
    fun trxManagerInMem(): TransactionManager = MemTransactionManager()
}
