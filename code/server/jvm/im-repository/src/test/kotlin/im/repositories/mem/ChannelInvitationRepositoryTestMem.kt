package im.repositories.mem

import im.repositories.ChannelInvitationRepositoryTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("inMem")
class ChannelInvitationRepositoryTestMem : ChannelInvitationRepositoryTest()
