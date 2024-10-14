package im.mem

import im.AuthControllerTests
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("inMem")
class AuthControllerTestsMem : AuthControllerTests()
