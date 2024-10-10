package im.repository.jpa.repositories.jpa

import im.repository.jpa.model.channel.ChannelMemberDTO
import im.repository.jpa.model.channel.ChannelMemberId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository


@Repository
interface ChannelMemberRepositoryJpa : JpaRepository<ChannelMemberDTO, ChannelMemberId> {
    @Query("SELECT m FROM ChannelMemberDTO m WHERE m.channel.id = :channelId AND m.user.id = :userId")
    fun findMemberByChannel(channelId: Long, userId: Long): ChannelMemberDTO?
}