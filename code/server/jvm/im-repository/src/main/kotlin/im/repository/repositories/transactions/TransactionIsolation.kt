package im.repository.repositories.transactions

/**
 * Represents the transaction isolation levels.
 *
 * [PostgreSQL Transaction Isolation Levels](https://www.postgresql.org/docs/current/transaction-iso.html)
 *
 * [Oracle Transaction Isolation Levels](https://docs.oracle.com/en/database/other-databases/timesten/22.1/introduction/transaction-isolation.html)
 *
 * @see TransactionManager
 *
 */
enum class TransactionIsolation {
    DEFAULT,
    READ_UNCOMMITTED,
    READ_COMMITTED,
    REPEATABLE_READ,
    SERIALIZABLE,
}
