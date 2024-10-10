package im.repository.mem.transactions

import im.repository.mem.repositories.MemAccessTokenRepositoryImpl
import im.repository.mem.repositories.MemChannelInvitationRepositoryImpl
import im.repository.mem.repositories.MemChannelRepositoryImpl
import im.repository.mem.repositories.MemImInvitationRepositoryImpl
import im.repository.mem.repositories.MemMessageRepositoryImpl
import im.repository.mem.repositories.MemRefreshTokenRepositoryImpl
import im.repository.mem.repositories.MemRepoUtils
import im.repository.mem.repositories.MemSessionRepositoryImpl
import im.repository.mem.repositories.MemUserRepositoryImpl
import im.repository.repositories.transactions.Transaction
import im.repository.repositories.transactions.TransactionIsolation
import im.repository.repositories.transactions.TransactionManager

class MemTransactionManager : TransactionManager {

    private val utils = MemRepoUtils()

    private val messageRepository = MemMessageRepositoryImpl(utils)
    private val accessTokenRepository = MemAccessTokenRepositoryImpl(utils)
    private val refreshTokenRepository = MemRefreshTokenRepositoryImpl(utils)
    private val imInvitationRepository = MemImInvitationRepositoryImpl(utils)
    private val channelInvitationRepository = MemChannelInvitationRepositoryImpl(utils)
    private val sessionRepository = MemSessionRepositoryImpl(utils, accessTokenRepository, refreshTokenRepository)
    private val channelRepository = MemChannelRepositoryImpl(utils, messageRepository, channelInvitationRepository)
    private val userRepository = MemUserRepositoryImpl(
        utils,
        channelInvitationRepository,
        channelRepository,
        messageRepository,
        sessionRepository
    )

    override fun <T> run(block: Transaction.() -> T, isolation: TransactionIsolation): T {
        return newTransaction().block()
    }

    private fun newTransaction(): Transaction {
        return MemTransaction(
            channelRepository,
            userRepository,
            sessionRepository,
            messageRepository,
            accessTokenRepository,
            refreshTokenRepository,
            imInvitationRepository,
            channelInvitationRepository
        )
    }
}