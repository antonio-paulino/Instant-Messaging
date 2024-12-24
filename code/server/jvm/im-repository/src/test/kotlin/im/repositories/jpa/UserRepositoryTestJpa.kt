package im.repositories.jpa

import im.repositories.UserRepositoryTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("Jpa")
class UserRepositoryTestJpa : UserRepositoryTest()
