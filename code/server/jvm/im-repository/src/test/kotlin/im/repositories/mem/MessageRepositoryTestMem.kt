package im.repositories.mem

import im.repositories.MessageRepositoryTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("inMem")
class MessageRepositoryTestMem : MessageRepositoryTest()
