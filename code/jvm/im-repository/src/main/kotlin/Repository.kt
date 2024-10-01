import java.util.*

interface Repository<T, ID> {
    fun save(entity: T): T

    fun saveAll(entities: Iterable<T>): List<T>

    fun findById(id: ID): Optional<T>

    fun findAll(): Iterable<T>

    fun findFirst(page: Int, pageSize: Int): List<T>

    fun findLast(page: Int, pageSize: Int): List<T>

    fun findAllById(ids: Iterable<ID>): Iterable<T>

    fun deleteById(id: ID)

    fun existsById(id: ID): Boolean

    fun count(): Long

    fun deleteAll()

    fun delete(entity: T)

    fun deleteAll(entities: Iterable<T>)

    fun deleteAllById(ids: Iterable<ID>)
}