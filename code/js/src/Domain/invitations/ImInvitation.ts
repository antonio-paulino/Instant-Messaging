import { ImInvitationOutputModel } from '../../Dto/output/invitations/ImInvitationOutputModel';

/**
 * Represents an invitation to create an instant messaging account.
 *
 * @param token The one-time use token.
 * @param expiresAt The date and time when the token expires.
 */
export interface ImInvitation {
    token: string;
    expiresAt: Date;
}

export function imInvitationFromDto(
    dto: ImInvitationOutputModel,
): ImInvitation {
    return {
        token: dto.token,
        expiresAt: new Date(dto.expiresAt),
    };
}
