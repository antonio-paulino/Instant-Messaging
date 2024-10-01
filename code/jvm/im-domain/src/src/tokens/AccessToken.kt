package tokens

import java.time.LocalDateTime

interface AccessToken {
    val expirationDate: LocalDateTime
}