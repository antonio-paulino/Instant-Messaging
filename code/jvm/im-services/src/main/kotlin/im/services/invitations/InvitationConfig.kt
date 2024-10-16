package im.services.invitations

import kotlin.time.Duration

class InvitationConfig(
    val minImInvitationTTL: Duration,
    val defaultImInvitationTTL: Duration,
    val maxImInvitationTTL: Duration,
    val minChannelInvitationTTL: Duration,
    val maxChannelInvitationTTL: Duration,
)
