package im.repository.jpa.repositories.jpa

import im.repository.jpa.model.channel.ChannelMemberDTO
import im.repository.jpa.model.channel.ChannelMemberId
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChannelMemberRepositoryJpa : JpaRepository<ChannelMemberDTO, ChannelMemberId> {
    fun findByChannelIdAndUserId(
        channelId: Long,
        userId: Long,
    ): ChannelMemberDTO?

    @Query(
        value =
            "SELECT cm FROM ChannelDTO c " +
                "JOIN ChannelMemberDTO cm ON c.id = cm.channel.id " +
                "WHERE cm.user.id = :userId",
    )
    fun findByMember(
        userId: Long,
        sort: Sort,
    ): List<ChannelMemberDTO>
}
