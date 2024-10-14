package im.mem

import im.ChannelServiceTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("inMem")
class ChannelServiceTestMem : ChannelServiceTest()
