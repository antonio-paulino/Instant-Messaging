package im.repositories

import im.TestApp
import im.channel.Channel
import im.channel.ChannelRole
import im.invitations.ChannelInvitation
import im.invitations.ChannelInvitationStatus
import im.messages.Message
import im.repository.jpa.transactions.TransactionManagerJpa
import im.repository.pagination.PaginationRequest
import im.repository.pagination.Sort
import im.repository.repositories.transactions.TransactionManager
import im.repository.mem.transactions.MemTransactionManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import im.user.User
import im.wrappers.toIdentifier
import im.wrappers.toName
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ContextConfiguration(classes = [TestApp::class])
open class ChannelRepositoryTest(
    @Autowired
    private val transactionManagerJpa: TransactionManagerJpa,
) {

    private lateinit var testChannel1: Channel
    private lateinit var testChannel2: Channel
    private lateinit var testOwner: User
    private lateinit var testInvitation: ChannelInvitation
    private lateinit var testMessage: Message
    private lateinit var testMember: User


    private fun transactionManagers(): Stream<TransactionManager> =
        Stream.of(
            MemTransactionManager().also { cleanup(it) },
            transactionManagerJpa.also { cleanup(it) }
        )

    private fun cleanup(transactionManager: TransactionManager) {
        transactionManager.run({
            refreshTokenRepository.deleteAll()
            accessTokenRepository.deleteAll()
            imInvitationRepository.deleteAll()
            channelInvitationRepository.deleteAll()
            messageRepository.deleteAll()
            channelInvitationRepository.deleteAll()
            channelRepository.deleteAll()
            sessionRepository.deleteAll()
            userRepository.deleteAll()
        })
    }

    private fun setup(transactionManager: TransactionManager) {
        transactionManager.run({
            testOwner = userRepository.save(User(1, "Owner", "password", "user1@daw.isel.pt"))
            testMember = userRepository.save(User(2, "Member", "password", "user2@daw.isel.pt"))

            testChannel1 = Channel(
                id = 1L,
                name = "General",
                owner = testOwner,
                isPublic = true,
                createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            )

            testChannel2 = Channel(
                id = 2L,
                name = "Gaming",
                owner = testOwner,
                isPublic = false,
                createdAt = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS)
            )

            testInvitation = ChannelInvitation(
                id = 1L,
                channel = testChannel1,
                inviter = testOwner,
                invitee = testOwner,
                status = ChannelInvitationStatus.PENDING,
                role = ChannelRole.MEMBER,
                expiresAt = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS)
            )

            testMessage = Message(
                id = 1L,
                channel = testChannel1,
                content = "Hello",
                createdAt = LocalDateTime.now(),
                user = testOwner,
                editedAt = null
            )
        })
    }


    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should save a channel`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testOwner = userRepository.save(testOwner)
            testMember = userRepository.save(testMember)
            testChannel1 = testChannel1.copy(owner = testOwner)

            val savedChannel = channelRepository.save(testChannel1)
            assertNotNull(savedChannel.id)
            assertEquals(testChannel1.name, savedChannel.name)
            assertEquals(testChannel1.isPublic, savedChannel.isPublic)
            assertEquals(testChannel1.owner, savedChannel.owner)
            assertEquals(testChannel1.createdAt, savedChannel.createdAt)
            assertEquals(testChannel1.members, savedChannel.members)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `save with same name should throw exception`(transactionManager: TransactionManager) {
        setup(transactionManager)
        assertThrows<Exception> {
            transactionManager.run({
                channelRepository.save(testChannel1)
                testChannel2 = testChannel2.copy(name = testChannel1.name)
                channelRepository.save(testChannel2)
            })
        }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should save multiple channels`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val channels = listOf(testChannel1, testChannel2)
            val savedChannels = channelRepository.saveAll(channels)
            assertEquals(2, savedChannels.size)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find channel by id`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedChannel = channelRepository.save(testChannel1)
            val foundChannel = channelRepository.findById(savedChannel.id)
            assertNotNull(foundChannel)
            assertEquals(savedChannel.id, foundChannel?.id)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find channel by name`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            channelRepository.save(testChannel1)
            val foundChannel = channelRepository.findByName("General".toName(), false)
            assertNotNull(foundChannel)
            assertEquals("General", foundChannel!!.name.value)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `find filter public should return only public channels`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            channelRepository.save(testChannel1)
            channelRepository.save(testChannel2)
            val (channels) = channelRepository.find(PaginationRequest(1, 10), true)
            assertEquals(1, channels.size)
            assertEquals(testChannel1.name, channels.first().name)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete on owner delete`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            channelRepository.save(testChannel1)
            assertEquals(1, channelRepository.count())
            userRepository.delete(testOwner)

            userRepository.flush()
            channelRepository.flush()

            assertEquals(0, channelRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should return empty for non-existent channel name`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val foundChannel = channelRepository.findByName("NonExistentChannel".toName(), false)
            assertNull(foundChannel)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find channels by partial name`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            channelRepository.save(testChannel1)
            channelRepository.save(testChannel2)
            val (foundChannels) = channelRepository.findByPartialName(
                "Gen", false,
                PaginationRequest(1, 10)
            )
            assertEquals(1, foundChannels.count())
            assertEquals(testChannel1.name, foundChannels.first().name)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find all channels`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            channelRepository.save(testChannel1)
            channelRepository.save(testChannel2)
            val channels = channelRepository.findAll()
            assertEquals(2, channels.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find first page of channels`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            channelRepository.save(testChannel1)
            channelRepository.save(testChannel2)

            val res = channelRepository.find(PaginationRequest(1, 1))

            val (firstChannels, pagination) = res

            assertEquals(2, pagination.total)
            assertEquals(1, pagination.currentPage)
            assertEquals(2, pagination.nextPage)
            assertEquals(2, pagination.totalPages)
            assertEquals(null, pagination.prevPage)

            assertEquals(1, firstChannels.size)
            assertEquals(testChannel1.name, firstChannels.first().name)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find last page of channels ordered by id desc`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            channelRepository.save(testChannel1)
            channelRepository.save(testChannel2)

            val (lastChannels, pagination) = channelRepository.find(
                PaginationRequest(
                    1,
                    2,
                    Sort.DESC
                )
            )

            assertEquals(2, pagination.total)
            assertEquals(1, pagination.currentPage)
            assertEquals(null, pagination.nextPage)
            assertEquals(1, pagination.totalPages)
            assertEquals(null, pagination.prevPage)

            assertEquals(2, lastChannels.size)
            assertEquals(testChannel2.name, lastChannels.first().name)
            assertEquals(testChannel1.name, lastChannels.last().name)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should update channel`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedChannel = channelRepository.save(testChannel1)
            val updatedChannel = savedChannel.copy(name = "UpdatedName".toName(), isPublic = false)
            val result = channelRepository.save(updatedChannel)
            assertEquals("UpdatedName", result.name.value)
            assertFalse(result.isPublic)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `get joined channels should return empty list`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testChannel1 = testChannel1.copy(owner = testOwner)
            testChannel2 = testChannel2.copy(owner = testOwner)

            testChannel1 = channelRepository.save(testChannel1)
            testChannel2 = channelRepository.save(testChannel2)

            val channels = channelRepository.findByMember(testMember)
            assertTrue(channels.none())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `get joined channels should return 1 channel`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testChannel1 = testChannel1.addMember(testMember, ChannelRole.MEMBER)
            testChannel1 = testChannel1.copy(owner = testOwner)
            testChannel2 = testChannel2.copy(owner = testOwner)

            testChannel1 = channelRepository.save(testChannel1)
            testChannel2 = channelRepository.save(testChannel2)

            val channels = channelRepository.findByMember(testMember)
            assertTrue(channels.size == 1)
            assertEquals(testChannel1, channels.keys.first())
            assertEquals(ChannelRole.MEMBER, channels.values.first())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `get owned channels should be empty`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testChannel1 =
                testChannel1.copy(owner = testOwner, membersLazy = lazy { mapOf(testMember to ChannelRole.MEMBER) })
            testChannel2 =
                testChannel2.copy(owner = testOwner, membersLazy = lazy { mapOf(testMember to ChannelRole.MEMBER) })

            channelRepository.save(testChannel1)
            channelRepository.save(testChannel2)

            val channels = channelRepository.findByOwner(testMember)
            assertTrue(channels.none())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `get owned channels should return 1 channel`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testChannel1 = testChannel1.copy(owner = testOwner)
            testChannel2 = testChannel2.copy(owner = testMember)

            testChannel1 = channelRepository.save(testChannel1)
            testChannel2 = channelRepository.save(testChannel2)

            val channels = channelRepository.findByOwner(testOwner)
            assertEquals(1, channels.size)
            assertEquals(testChannel1, channels.first())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `get owned channels should return empty list`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val channels = channelRepository.findByOwner(testOwner)
            assertTrue(channels.none())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `get member should return empty`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedChannel = channelRepository.save(testChannel1)
            val member = channelRepository.getMember(savedChannel, testMember)
            assertNull(member)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `get member should return member`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedChannel = channelRepository.save(testChannel1)
            val newChannel = savedChannel.addMember(testMember, ChannelRole.MEMBER)
            val updatedChannel = channelRepository.save(newChannel)
            val member = channelRepository.getMember(updatedChannel, testMember)
            assertNotNull(member)
            assertEquals(ChannelRole.MEMBER, member!!.second)
            assertEquals(testMember, member.first)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should add member to channel with role Member`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedChannel = channelRepository.save(testChannel1)
            val newChannel = savedChannel.addMember(testMember, ChannelRole.MEMBER)
            val updatedChannel = channelRepository.save(newChannel)
            assertEquals(2, updatedChannel.members.size) // Owner + Member
            assertEquals(ChannelRole.MEMBER, updatedChannel.members[testMember])
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should add member to channel with role Guest`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedChannel = channelRepository.save(testChannel1)
            val newChannel = savedChannel.addMember(testMember, ChannelRole.GUEST)
            val updatedChannel = channelRepository.save(newChannel)
            assertEquals(2, updatedChannel.members.size) // Owner + Guest
            assertEquals(ChannelRole.GUEST, updatedChannel.members[testMember])
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should remove member from channel`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedChannel = channelRepository.save(testChannel1)

            val newChannel = savedChannel.addMember(testMember, ChannelRole.MEMBER)
            val updatedChannel = channelRepository.save(newChannel)
            assertEquals(2, updatedChannel.members.size)
            assertEquals(ChannelRole.MEMBER, updatedChannel.members[testMember])
            // Remove Member
            val newChannel2 = updatedChannel.removeMember(testMember)
            val updatedChannel2 = channelRepository.save(newChannel2)

            assertEquals(1, updatedChannel2.members.size) // Owner
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete channel by id`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedChannel = channelRepository.save(testChannel1)
            channelRepository.deleteById(savedChannel.id)
            assertEquals(0, channelRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete multiple channels by ids`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedChannel1 = channelRepository.save(testChannel1)
            val savedChannel2 = channelRepository.save(testChannel2)
            channelRepository.deleteAllById(listOf(savedChannel1.id, savedChannel2.id))
            assertEquals(0, channelRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete channel entity`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedChannel = channelRepository.save(testChannel1)
            channelRepository.delete(savedChannel)
            assertEquals(0, channelRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete all channels`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            channelRepository.save(testChannel1)
            channelRepository.save(testChannel2)
            channelRepository.deleteAll()
            assertEquals(0, channelRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `exists by id should return true for existing channel`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedChannel = channelRepository.save(testChannel1)
            assertTrue(channelRepository.existsById(savedChannel.id))
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `exists by id should return false for non-existing channel`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            assertFalse(channelRepository.existsById((999L).toIdentifier()))
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `count should return correct number of channels`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            channelRepository.save(testChannel1)
            channelRepository.save(testChannel2)
            assertEquals(2, channelRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should handle save of empty list`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val result = channelRepository.saveAll(emptyList())
            assertTrue(result.isEmpty())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should handle delete of empty list`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            channelRepository.deleteAll(emptyList())
            assertEquals(0, channelRepository.count())
        })
    }
}
