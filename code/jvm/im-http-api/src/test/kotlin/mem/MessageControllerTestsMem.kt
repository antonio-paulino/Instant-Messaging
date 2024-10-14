package im.mem

import im.MessageControllerTests
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("inMem")
class MessageControllerTestsMem : MessageControllerTests()
