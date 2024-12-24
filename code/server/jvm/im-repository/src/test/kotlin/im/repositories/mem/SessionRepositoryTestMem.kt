package im.repositories.mem

import im.repositories.SessionRepositoryTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("inMem")
class SessionRepositoryTestMem : SessionRepositoryTest()
