package im.repository.jpa.repositories.jpa.channels

import im.repository.jpa.model.channel.ChannelDTO
import im.repository.jpa.model.channel.ChannelMemberDTO
import im.repository.jpa.model.channel.ChannelMemberId
import im.repository.jpa.model.channel.ChannelRoleDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChannelMemberRepositoryJpa : JpaRepository<ChannelMemberDTO, ChannelMemberId> {
    fun findByChannelIdAndUserId(
        channelId: Long,
        userId: Long,
    ): ChannelMemberDTO?

    @Query(
        value = "SELECT cm.channel FROM ChannelMemberDTO cm WHERE cm.user.id = :userId",
    )
    fun findByUserId(
        userId: Long,
        page: Pageable,
    ): Page<ChannelDTO>

    @Query(
        value = "SELECT cm.channel FROM ChannelMemberDTO cm WHERE cm.user.id = :userId",
    )
    fun findByUserIdSliced(
        userId: Long,
        page: Pageable,
    ): Slice<ChannelDTO>

    @Modifying
    @Query(
        value = "INSERT INTO channel_member (channel_id, user_id, role) VALUES (:channelId, :userId, :role)",
        nativeQuery = true,
    )
    fun addMember(
        channelId: Long,
        userId: Long,
        role: String,
    )

    @Modifying
    @Query(
        value =
            "UPDATE ChannelMemberDTO cm " +
                "SET cm.role = :role " +
                "WHERE cm.id = :memberId",
    )
    fun updateById(
        memberId: ChannelMemberId,
        role: ChannelRoleDTO,
    )
}
