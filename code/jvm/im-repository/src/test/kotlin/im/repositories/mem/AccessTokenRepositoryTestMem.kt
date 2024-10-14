package im.repositories.mem

import im.repositories.AccessTokenRepositoryTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("inMem")
class AccessTokenRepositoryTestMem : AccessTokenRepositoryTest()
