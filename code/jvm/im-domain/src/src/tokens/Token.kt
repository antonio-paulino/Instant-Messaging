package tokens

import sessions.Session
import java.util.UUID

interface Token {
    val token: UUID
    val session: Session
}

