package im.domain.channel

import im.domain.user.User
import im.domain.wrappers.identifier.Identifier
import im.domain.wrappers.identifier.toIdentifier
import im.domain.wrappers.name.Name
import im.domain.wrappers.name.toName
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Represents a channel in the application.
 *
 * @property id the unique identifier of the channel
 * @property name the name of the channel
 * @property owner the user that created the channel
 * @property isPublic whether the channel is public or private
 * @property createdAt the date and time when the channel was created
 * @property members the users that are members of the channel and their roles
 */
data class Channel(
    val id: Identifier = Identifier(0),
    val name: Name,
    val owner: User,
    val isPublic: Boolean,
    val createdAt: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
    private val membersLazy: Lazy<Map<User, ChannelRole>> = lazy { mapOf(owner to ChannelRole.OWNER) },
) {
    val members
        get() = membersLazy.value

    constructor(
        id: Long = 0,
        name: String,
        owner: User,
        isPublic: Boolean,
        createdAt: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
        members: Map<User, ChannelRole> = mapOf(owner to ChannelRole.OWNER),
    ) : this(
        id = id.toIdentifier(),
        name = name.toName(),
        owner = owner,
        isPublic = isPublic,
        createdAt = createdAt,
        membersLazy = lazy { members },
    )

    /**
     * Updates the channel with the new name and public status.
     *
     * @param name the new name of the channel
     * @param isPublic whether the channel is public or private
     * @return a new channel with the updated values
     */
    fun updateChannel(
        name: Name,
        isPublic: Boolean,
    ) = copy(name = name, isPublic = isPublic)

    /**
     * Adds a member to the channel with the specified role.
     *
     * @param user the user to add to the channel
     * @param role the role of the user in the channel
     * @return a new channel with the added member
     */
    fun addMember(
        user: User,
        role: ChannelRole,
    ) = copy(membersLazy = lazy { members + (user to role) })

    /**
     * Removes a member from the channel.
     *
     * @param user the user to remove from the channel
     * @return a new channel with the removed member
     */
    fun removeMember(user: User) = copy(membersLazy = lazy { members - user })

    /**
     * Checks if the user is a member of the channel.
     *
     * @param user the user to check
     * @return true if the user is a member of the channel, false otherwise
     */
    fun hasMember(user: User): Boolean = members.containsKey(user)

    override fun equals(other: Any?): Boolean =
        other is Channel &&
            id == other.id &&
            name == other.name &&
            owner == other.owner &&
            isPublic == other.isPublic &&
            createdAt == other.createdAt

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + isPublic.hashCode()
        result = 31 * result + createdAt.hashCode()
        return result
    }
}
