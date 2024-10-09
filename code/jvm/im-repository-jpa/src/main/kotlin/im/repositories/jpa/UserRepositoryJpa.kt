package im.repositories.jpa

import im.model.channel.ChannelDTO
import im.model.channel.ChannelMemberDTO
import im.model.invitation.ChannelInvitationDTO
import im.model.session.SessionDTO
import im.model.user.UserDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRepositoryJpa : JpaRepository<UserDTO, Long> {
    @Query("SELECT u FROM UserDTO u WHERE u.name = :name")
    fun findByName(name: String): List<UserDTO>

    @Query("SELECT u FROM UserDTO u WHERE u.email = :email")
    fun findByEmail(email: String): List<UserDTO>

    @Query(
        countQuery = "SELECT COUNT(u) FROM UserDTO u WHERE u.name LIKE CONCAT(:name, '%')",
        value = "SELECT u FROM UserDTO u WHERE u.name LIKE CONCAT(:name, '%')"
    )
    fun findByPartialName(name: String, page: Pageable): Page<UserDTO>

    @Query("SELECT u FROM UserDTO u WHERE u.name = :name AND u.password = :password")
    fun findByNameAndPassword(name: String, password: String): List<UserDTO>

    @Query("SELECT u FROM UserDTO u WHERE u.email = :email AND u.password = :password")
    fun findByEmailAndPassword(email: String, password: String): List<UserDTO>

    @Query("SELECT c FROM ChannelDTO c WHERE c.owner.id = :userId")
    fun getOwnedChannels(userId: Long): List<ChannelDTO>

    @Query("SELECT r FROM ChannelDTO c JOIN ChannelMemberDTO r ON c.id = r.id.channelID WHERE r.id.userID = :userId")
    fun getJoinedChannels(userId: Long): List<ChannelMemberDTO>

    @Query("SELECT i FROM ChannelInvitationDTO i WHERE i.invitee.id = :userId")
    fun getInvitations(userId: Long): List<ChannelInvitationDTO>

    @Query("SELECT s FROM SessionDTO s WHERE s.user.id = :userId")
    fun getSessions(userId: Long): List<SessionDTO>
}