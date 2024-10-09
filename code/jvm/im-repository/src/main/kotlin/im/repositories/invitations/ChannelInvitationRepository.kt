package im.repositories.invitations

import im.repositories.Repository
import im.invitations.ChannelInvitation
import im.wrappers.Identifier


/**
 * [Repository] for [ChannelInvitation] entities.
 */
interface ChannelInvitationRepository : Repository<ChannelInvitation, Identifier>