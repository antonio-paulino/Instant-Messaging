package im.repositories.jpa

import im.invitations.ChannelInvitationStatus
import im.model.channel.ChannelDTO
import im.model.invitation.ChannelInvitationDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChannelRepositoryJpa : JpaRepository<ChannelDTO, Long> {
    @Query(
        "SELECT c FROM ChannelDTO c WHERE c.name = :name AND " +
                "(((c.isPublic = true ) AND (:filterPublic = true)) OR (:filterPublic = false))"
    )
    fun findByName(name: String, filterPublic: Boolean): ChannelDTO?

    @Query(
        countQuery = "SELECT COUNT(c) FROM ChannelDTO c WHERE c.name LIKE CONCAT(:name, '%') AND " +
                "(((c.isPublic = true ) AND (:filterPublic = true)) OR (:filterPublic = false))",
        value = "SELECT c FROM ChannelDTO c WHERE c.name LIKE CONCAT(:name, '%') AND " +
                "(((c.isPublic = true ) AND (:filterPublic = true)) OR (:filterPublic = false))"
    )
    fun findByPartialName(name: String, filterPublic: Boolean, page: Pageable): Page<ChannelDTO>

    @Query(
        countQuery = "SELECT COUNT(c) FROM ChannelDTO c WHERE " +
                "(((c.isPublic = true ) AND (:filterPublic = true)) OR (:filterPublic = false))",
        value = "SELECT c FROM ChannelDTO c WHERE " +
                "(((c.isPublic = true ) AND (:filterPublic = true)) OR (:filterPublic = false))"
    )
    fun find(filterPublic: Boolean, page: Pageable): Page<ChannelDTO>

    @Query("SELECT i FROM ChannelInvitationDTO i WHERE i.channel.id = :channelId AND i.status = :status")
    fun findInvitationsByChannel(channelId: Long, status: ChannelInvitationStatus): List<ChannelInvitationDTO>
}