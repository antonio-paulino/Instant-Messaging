package im

import im.domain.Failure
import im.domain.Success
import im.domain.channel.Channel
import im.domain.channel.ChannelRole
import im.domain.user.User
import im.domain.wrappers.identifier.Identifier
import im.domain.wrappers.name.toName
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.repository.repositories.transactions.TransactionManager
import im.services.channels.ChannelError
import im.services.channels.ChannelService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@SpringBootTest
abstract class ChannelServiceTest {
    @Autowired
    private lateinit var channelService: ChannelService

    @Autowired
    private lateinit var transactionManager: TransactionManager

    private var testUser1: User = User(1L, "testUser1", "testPassword1", "test@isel.pt")
    private var testUser2: User = User(2L, "testUser2", "testPassword2", "test2@isel.pt")

    @BeforeEach
    fun setup() {
        transactionManager.run {
            refreshTokenRepository.deleteAll()
            accessTokenRepository.deleteAll()
            imInvitationRepository.deleteAll()
            channelInvitationRepository.deleteAll()
            messageRepository.deleteAll()
            channelInvitationRepository.deleteAll()
            channelRepository.deleteAll()
            sessionRepository.deleteAll()
            userRepository.deleteAll()
        }
        transactionManager.run {
            testUser1 = userRepository.save(testUser1)
            testUser2 = userRepository.save(testUser2)
        }
    }

    @Test
    fun `create channel should create a channel`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)

        assertIs<Success<Channel>>(result)
        val channel = result.value

        assertEquals("testChannel", channel.name.value)
        assertEquals(true, channel.isPublic)
        assertEquals(testUser1, channel.owner)
        assertEquals(ChannelRole.GUEST, channel.defaultRole)
    }

    @Test
    fun `create channel channel already exists`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)

        val result2 = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Failure<ChannelError>>(result2)
    }

    @Test
    fun `get channel by id should return the channel`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.getChannelById(channel.id, testUser1)
        assertIs<Success<Channel>>(result2)
        val channel2 = result2.value

        assertEquals(channel, channel2)
    }

    @Test
    fun `get channel by id non existing channel`() {
        val result = channelService.getChannelById(Identifier(1L), testUser1)
        assertIs<Failure<ChannelError>>(result)
        assertIs<ChannelError.ChannelNotFound>(result.value)
    }

    @Test
    fun `get public channel by id not a member of the channel`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value
        val result2 = channelService.getChannelById(channel.id, testUser2)
        assertIs<Success<Channel>>(result2)
        assertEquals(channel, result2.value)
    }

    @Test
    fun `get private channel by id not a member of the channel`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.MEMBER, false, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value
        val result2 = channelService.getChannelById(channel.id, testUser2)
        assertIs<Failure<ChannelError>>(result2)
        assertIs<ChannelError.CannotAccessChannel>(result2.value)
    }

    @Test
    fun `update channel should update the channel`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.updateChannel(channel.id, "newName".toName(), ChannelRole.MEMBER, false, testUser1)
        assertIs<Success<Unit>>(result2)

        val result3 = channelService.getChannelById(channel.id, testUser1)
        assertIs<Success<Channel>>(result3)
        val updatedChannel = result3.value

        assertEquals("newName", updatedChannel.name.value)
        assertEquals(false, updatedChannel.isPublic)
        assertEquals(ChannelRole.MEMBER, updatedChannel.defaultRole)
    }

    @Test
    fun `update channel same name should succeed`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.updateChannel(channel.id, "testChannel".toName(), ChannelRole.MEMBER, false, testUser1)
        assertIs<Success<Unit>>(result2)
    }

    @Test
    fun `update channel, channel with name already exists`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)

        val result2 = channelService.createChannel("testChannel2".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result2)
        val channel2 = result2.value

        val result3 = channelService.updateChannel(channel2.id, "testChannel".toName(), ChannelRole.MEMBER, false, testUser1)
        assertIs<Failure<ChannelError>>(result3)
        assertIs<ChannelError.ChannelAlreadyExists>(result3.value)
    }

    @Test
    fun `update channel non existing channel`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)

        val result2 = channelService.createChannel("testChannel2".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result2)

        val result3 = channelService.updateChannel(result.value.id, "testChannel2".toName(), ChannelRole.MEMBER, false, testUser1)
        assertIs<Failure<ChannelError>>(result3)
    }

    @Test
    fun `update channel not the owner of the channel`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.updateChannel(channel.id, "newName".toName(), ChannelRole.MEMBER, false, testUser2)
        assertIs<Failure<ChannelError>>(result2)
        assertIs<ChannelError.CannotUpdateChannel>(result2.value)
    }

    @Test
    fun `delete channel should delete the channel`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.deleteChannel(channel.id, testUser1)
        assertIs<Success<Unit>>(result2)

        val result3 = channelService.getChannelById(channel.id, testUser1)
        assertIs<Failure<ChannelError>>(result3)
        assertIs<ChannelError.ChannelNotFound>(result3.value)
    }

    @Test
    fun `delete channel non existing channel`() {
        val result = channelService.deleteChannel(Identifier(1L), testUser1)
        assertIs<Failure<ChannelError>>(result)
        assertIs<ChannelError.ChannelNotFound>(result.value)
    }

    @Test
    fun `delete channel not the owner of the channel`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.deleteChannel(channel.id, testUser2)
        assertIs<Failure<ChannelError>>(result2)
        assertIs<ChannelError.CannotDeleteChannel>(result2.value)
    }

    @Test
    fun `join channel should join the channel`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.joinChannel(channel.id, testUser2.id, testUser2)
        assertIs<Success<Unit>>(result2)

        val result3 = channelService.getChannelById(channel.id, testUser2)
        assertIs<Success<Channel>>(result3)
        val joinedChannel = result3.value

        assertEquals(channel, joinedChannel)
    }

    @Test
    fun `join channel non existing channel`() {
        val result = channelService.joinChannel(Identifier(1L), testUser2.id, testUser2)
        assertIs<Failure<ChannelError>>(result)
        assertIs<ChannelError.ChannelNotFound>(result.value)
    }

    @Test
    fun `join channel cannot add other user`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.joinChannel(channel.id, testUser2.id, testUser1)
        assertIs<Failure<ChannelError>>(result2)
        assertIs<ChannelError.CannotAddMember>(result2.value)
    }

    @Test
    fun `join channel private channel`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, false, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.joinChannel(channel.id, testUser2.id, testUser2)
        assertIs<Failure<ChannelError>>(result2)
        assertIs<ChannelError.CannotJoinPrivateChannel>(result2.value)
    }

    @Test
    fun `join channel user already in the channel`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.joinChannel(channel.id, testUser1.id, testUser1)
        assertIs<Failure<ChannelError>>(result2)
        assertIs<ChannelError.UserAlreadyMember>(result2.value)
    }

    @Test
    fun `remove channel member user should leave channel`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.joinChannel(channel.id, testUser2.id, testUser2)
        assertIs<Success<Unit>>(result2)

        val result3 = channelService.removeChannelMember(channel.id, testUser2.id, testUser2)
        assertIs<Success<Unit>>(result3)

        val result4 = channelService.getChannelById(channel.id, testUser2)
        assertIs<Success<Channel>>(result4)
    }

    @Test
    fun `remove channel member owner should kick member and cannot access private channel`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.joinChannel(channel.id, testUser2.id, testUser2)
        assertIs<Success<Unit>>(result2)

        val result3 = channelService.removeChannelMember(channel.id, testUser2.id, testUser1)
        assertIs<Success<Unit>>(result3)

        val result4 = channelService.updateChannel(channel.id, "testChannel2".toName(), ChannelRole.GUEST, false, testUser1)
        assertIs<Success<Unit>>(result4)

        val result5 = channelService.getChannelById(channel.id, testUser2)
        assertIs<Failure<ChannelError>>(result5)
        assertIs<ChannelError.CannotAccessChannel>(result5.value)
    }

    @Test
    fun `remove channel member, member not in channel`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.removeChannelMember(channel.id, testUser2.id, testUser1)
        assertIs<Failure<ChannelError>>(result2)
        assertIs<ChannelError.UserNotMember>(result2.value)
    }

    @Test
    fun `remove channel member non existing channel`() {
        val result = channelService.removeChannelMember(Identifier(1L), testUser2.id, testUser1)
        assertIs<Failure<ChannelError>>(result)
        assertIs<ChannelError.ChannelNotFound>(result.value)
    }

    @Test
    fun `remove channel member user not found`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.removeChannelMember(channel.id, Identifier(1L), testUser1)
        assertIs<Failure<ChannelError>>(result2)
        assertIs<ChannelError.UserNotFound>(result2.value)
    }

    @Test
    fun `remove channel member cannot remove user`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.removeChannelMember(channel.id, testUser1.id, testUser2)
        assertIs<Failure<ChannelError>>(result2)
        assertIs<ChannelError.CannotRemoveMember>(result2.value)
    }

    @Test
    fun `update member role should update the member role`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.joinChannel(channel.id, testUser2.id, testUser2)
        assertIs<Success<Unit>>(result2)

        val result3 = channelService.updateMemberRole(channel.id, testUser2.id, ChannelRole.GUEST, testUser1)
        assertIs<Success<Unit>>(result3)

        val result4 = channelService.getUserChannels(testUser2.id, SortRequest("id"), testUser2)
        assertIs<Success<Map<Channel, ChannelRole>>>(result4)
        assertEquals(ChannelRole.GUEST, result4.value[channel])
    }

    @Test
    fun `update member role non existing channel`() {
        val result = channelService.updateMemberRole(Identifier(1L), testUser2.id, ChannelRole.GUEST, testUser1)
        assertIs<Failure<ChannelError>>(result)
        assertIs<ChannelError.ChannelNotFound>(result.value)
    }

    @Test
    fun `update member role user not found`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.updateMemberRole(channel.id, Identifier(1L), ChannelRole.GUEST, testUser1)
        assertIs<Failure<ChannelError>>(result2)
        assertIs<ChannelError.UserNotFound>(result2.value)
    }

    @Test
    fun `update member role, cannot update member role to owner`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.joinChannel(channel.id, testUser2.id, testUser2)
        assertIs<Success<Unit>>(result2)

        val result3 = channelService.updateMemberRole(channel.id, testUser1.id, ChannelRole.OWNER, testUser2)
        assertIs<Failure<ChannelError>>(result3)
        assertIs<ChannelError.CannotUpdateMemberRole>(result3.value)
    }

    @Test
    fun `update member role, not owner of the channel`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.joinChannel(channel.id, testUser2.id, testUser2)
        assertIs<Success<Unit>>(result2)

        val result3 = channelService.updateMemberRole(channel.id, testUser2.id, ChannelRole.GUEST, testUser2)
        assertIs<Failure<ChannelError>>(result3)
        assertIs<ChannelError.CannotUpdateMemberRole>(result3.value)
    }

    @Test
    fun `get channels should return channels`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.getChannels(null, PaginationRequest(1, 10), SortRequest("id"))
        assertIs<Success<Pagination<Channel>>>(result2)
        val channels = result2.value

        assertEquals(1, channels.items.size)
        assertEquals(channel, channels.items[0])
    }

    @Test
    fun `get channels invalid sort`() {
        val result = channelService.getChannels(null, PaginationRequest(1, 10), SortRequest("invalid"))
        assertIs<Failure<ChannelError>>(result)
        assertIs<ChannelError.InvalidSortField>(result.value)
    }

    @Test
    fun `get channels should return empty`() {
        val result = channelService.getChannels(null, PaginationRequest(1, 10), SortRequest("id"))
        assertIs<Success<Pagination<Channel>>>(result)
        val channels = result.value
        assertEquals(0, channels.items.size)
    }

    @Test
    fun `get channels should return channels with name`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.createChannel("testChannel2".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result2)
        val channel2 = result2.value

        val result3 = channelService.getChannels("tes", PaginationRequest(1, 10), SortRequest("id"))
        assertIs<Success<Pagination<Channel>>>(result3)
        val channels = result3.value.items
        val pagination = result3.value.info

        assertEquals(2, channels.size)
        assertEquals(channel, channels.first())
        assertEquals(channel2, channels.last())
        assertEquals(2, pagination!!.total)
        assertEquals(1, pagination.totalPages)
        assertEquals(1, pagination.currentPage)
        assertEquals(null, pagination.nextPage)
        assertEquals(null, pagination.prevPage)
    }

    @Test
    fun `get channels with name should return empty`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)

        val result2 = channelService.getChannels("NonExisting", PaginationRequest(1, 10), SortRequest("id"))
        assertIs<Success<Pagination<Channel>>>(result2)
        val channels = result2.value

        assertEquals(0, channels.items.size)
    }

    @Test
    fun `get channels with pagination should return channels`() {
        val result = channelService.createChannel("testChannel".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result)
        val channel = result.value

        val result2 = channelService.createChannel("testChannel2".toName(), ChannelRole.GUEST, true, testUser1)
        assertIs<Success<Channel>>(result2)

        val result3 = channelService.getChannels(null, PaginationRequest(1, 1), SortRequest("id"))
        assertIs<Success<Pagination<Channel>>>(result3)
        val channels = result3.value.items
        val pagination = result3.value.info

        assertEquals(1, channels.size)
        assertEquals(channel, channels[0])
        assertEquals(2, pagination!!.total)
        assertEquals(2, pagination.totalPages)
        assertEquals(1, pagination.currentPage)
        assertEquals(2, pagination.nextPage)
        assertEquals(null, pagination.prevPage)
    }

    @Test
    fun `get user channels should return user channels`() {
        val testChannel1 =
            transactionManager.run {
                channelRepository.save(Channel(1L, "testChannel1", ChannelRole.GUEST, testUser1, true))
            }
        val testChannel2 =
            transactionManager.run {
                channelRepository.save(Channel(2L, "testChannel2", ChannelRole.MEMBER, testUser2, true))
            }
        val result = channelService.getUserChannels(testUser1.id, SortRequest("id"), testUser1)
        assertIs<Success<Map<Channel, ChannelRole>>>(result)
        assertTrue(result.value.containsKey(testChannel1))
        assertFalse(result.value.containsKey(testChannel2))
    }

    @Test
    fun `get user channels different user`() {
        val result = channelService.getUserChannels(testUser2.id, SortRequest("id"), testUser1)
        assertIs<Failure<ChannelError>>(result)
        assert(result.value == ChannelError.CannotAccessUserChannels)
    }
}
