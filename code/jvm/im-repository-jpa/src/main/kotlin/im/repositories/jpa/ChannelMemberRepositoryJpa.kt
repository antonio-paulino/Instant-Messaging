package im.repositories.jpa

import im.model.channel.ChannelMemberDTO
import im.model.channel.ChannelMemberId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository


@Repository
interface ChannelMemberRepositoryJpa : JpaRepository<ChannelMemberDTO, ChannelMemberId> {
    @Query("SELECT m FROM ChannelMemberDTO m WHERE m.channel.id = :channelId AND m.user.id = :userId")
    fun findMemberByChannel(channelId: Long, userId: Long): ChannelMemberDTO?
}