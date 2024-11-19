/**
 * Represents the input model for updating a channel invitation.
 *
 * @param role The role of the user in the channel.
 * @param expiresAt The date and time when the invitation expires.
 */
export interface ChannelInvitationUpdateInputModel {
    role: string | null;
    expiresAt: Date | null;
}
