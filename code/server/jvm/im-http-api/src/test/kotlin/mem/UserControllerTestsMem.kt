package im.mem

import im.UserControllerTests
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("inMem")
class UserControllerTestsMem : UserControllerTests()
