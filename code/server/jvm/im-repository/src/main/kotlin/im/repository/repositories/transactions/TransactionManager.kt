package im.repository.repositories.transactions

/**
 * Transaction manager interface.
 * Used to execute a block of code inside a transaction with a specified isolation level.
 *
 * @see Transaction
 * @see TransactionIsolation
 */
interface TransactionManager {
    /**
     * Executes the given [Transaction] with the specified [TransactionIsolation].
     *
     * @param block The block to execute.
     * @param isolation The isolation level of the transaction
     * @return The result of the block.
     */
    fun <T> run(
        isolation: TransactionIsolation = TransactionIsolation.DEFAULT,
        block: Transaction.() -> T,
    ): T
}
