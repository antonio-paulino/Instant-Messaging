package im.repository.repositories

import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest

/**
 * Repository interface for CRUD operations, used to abstract data access layer from the business logic.
 *
 * If operations are executed in a transaction, the repository only performs the operations
 * and does not persist the changes to the database until the transaction is committed.
 *
 * If the changes should be persisted immediately, the [flush] method should be called.
 */
interface Repository<T, ID> {
    /**
     * Saves the entity to the database.
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    fun save(entity: T): T

    /**
     * Saves all entities to the database.
     *
     * @param entities the entities to save
     * @return the saved entities
     */
    fun saveAll(entities: Iterable<T>): List<T>

    /**
     * Finds an entity by its ID.
     *
     * @param id the ID of the entity
     * @return an entity with the given ID, or `null` if no entity with the given ID exists
     */
    fun findById(id: ID): T?

    /**
     * Finds all entities.
     *
     * @return all entities
     */
    fun findAll(): List<T>

    /**
     * Finds entities based on the pagination request.
     *
     * Use this method to implement pagination.
     *
     * @param pagination the pagination request
     * @param sortRequest the sort request
     * @return the entities and the pagination information.
     */
    fun find(
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<T>

    /**
     * Finds all entities by their IDs.
     *
     * @param ids the IDs of the entities
     * @return the entities with the given IDs
     */
    fun findAllById(ids: Iterable<ID>): List<T>

    /**
     * Deletes an entity by its ID.
     *
     * @param id the ID of the entity to delete
     */
    fun deleteById(id: ID)

    /**
     * Checks if an entity with the given ID exists.
     *
     * @param id the ID of the entity
     * @return `true` if an entity with the given ID exists, `false` otherwise
     */
    fun existsById(id: ID): Boolean

    /**
     * Counts the number of entities.
     *
     * @return the number of entities
     */
    fun count(): Long

    /**
     * Deletes all entities.
     */
    fun deleteAll()

    /**
     * Deletes an entity.
     *
     * @param entity the entity to delete
     */
    fun delete(entity: T)

    /**
     * Deletes all provided entities.
     *
     * @param entities the entities to delete
     */
    fun deleteAll(entities: Iterable<T>)

    /**
     * Deletes all entities by their IDs.
     *
     * @param ids the IDs of the entities to delete
     */
    fun deleteAllById(ids: Iterable<ID>)

    /**
     * Persists the changes to the database.
     *
     * Should only be used in a transaction context.
     */
    fun flush()
}
