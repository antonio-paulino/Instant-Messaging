package im.repositories.mem

import im.repositories.ChannelRepositoryTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("inMem")
class ChannelRepositoryTestMem : ChannelRepositoryTest()
