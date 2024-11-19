package im.repository.repositories

/**
 * Represents an event that occurred in the repository.
 *
 * @param T the type of the entity
 */
sealed class RepositoryEvent<T> {
    data class EntityPersisted<T>(val entity: T) : RepositoryEvent<T>()

    data class EntityUpdated<T>(val entity: T) : RepositoryEvent<T>()

    data class EntityRemoved<T>(val entity: T) : RepositoryEvent<T>()
}
