package im.repositories.mem

import im.repositories.UserRepositoryTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("inMem")
class UserRepositoryTestMem : UserRepositoryTest()
