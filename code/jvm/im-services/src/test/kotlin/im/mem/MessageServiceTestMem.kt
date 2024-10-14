package im.mem

import im.MessageServiceTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("inMem")
class MessageServiceTestMem : MessageServiceTest()
