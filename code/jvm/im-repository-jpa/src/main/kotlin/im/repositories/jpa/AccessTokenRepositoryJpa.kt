package im.repositories.jpa

import im.model.token.AccessTokenDTO
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AccessTokenRepositoryJpa : JpaRepository<AccessTokenDTO, UUID>