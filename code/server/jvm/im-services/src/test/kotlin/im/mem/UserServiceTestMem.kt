package im.mem

import im.UserServiceTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("inMem")
class UserServiceTestMem : UserServiceTest()
