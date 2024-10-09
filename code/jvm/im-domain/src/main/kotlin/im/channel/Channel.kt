package im.channel

import im.user.User
import im.wrappers.Identifier
import im.wrappers.Name
import im.wrappers.toIdentifier
import im.wrappers.toName
import java.time.LocalDateTime

data class Channel(
    val id: Identifier = Identifier(0),
    val name: Name,
    val owner: User,
    val isPublic: Boolean,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    private val membersLazy: Lazy<Map<User, ChannelRole>> = lazy { mapOf(owner to ChannelRole.OWNER) },
) {
    val members
        get() = membersLazy.value

    companion object {
        operator fun invoke(
            id: Long = 0,
            name: String,
            owner: User,
            isPublic: Boolean,
            createdAt: LocalDateTime = LocalDateTime.now(),
            members: Map<User, ChannelRole> = mapOf(owner to ChannelRole.OWNER),
        ) = Channel(id.toIdentifier(), name.toName(), owner, isPublic, createdAt, lazy { members })
    }

    fun updateChannel(name: Name, isPublic: Boolean) = copy(name = name, isPublic = isPublic)

    fun addMember(user: User, role: ChannelRole) = copy(membersLazy = lazy { members + (user to role) })

    fun removeMember(user: User) = copy(membersLazy = lazy { members - user })

    fun hasMember(user: User): Boolean = members.containsKey(user)

    override fun equals(other: Any?): Boolean {
        return other is Channel
                && id == other.id && name == other.name && owner == other.owner
                && isPublic == other.isPublic && createdAt == other.createdAt
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + isPublic.hashCode()
        result = 31 * result + createdAt.hashCode()
        return result
    }
}