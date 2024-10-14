package im.mem

import im.AuthServiceTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("inMem")
class AuthServiceTestMem : AuthServiceTest()
