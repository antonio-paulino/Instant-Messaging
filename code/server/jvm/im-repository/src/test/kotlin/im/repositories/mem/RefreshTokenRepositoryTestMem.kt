package im.repositories.mem

import im.repositories.RefreshTokenRepositoryTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("inMem")
class RefreshTokenRepositoryTestMem : RefreshTokenRepositoryTest()
