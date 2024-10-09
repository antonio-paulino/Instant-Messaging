package im.repositories.jpa

import im.model.message.MessageDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MessageRepositoryJpa : JpaRepository<MessageDTO, Long> {
    @Query(
        countQuery = "SELECT COUNT(m) FROM MessageDTO m WHERE m.channel.id = :channelId",
        value = "SELECT m FROM MessageDTO m WHERE m.channel.id = :channelId"
    )
    fun findByChannelId(channelId: Long, page: Pageable): Page<MessageDTO>

    @Query("SELECT m FROM MessageDTO m WHERE m.channel.id = :channelId")
    fun findByChannel(channelId: Long): List<MessageDTO>
}