import { AccessToken } from '../tokens/AccessToken';
import { RefreshToken } from '../tokens/RefreshToken';
import { User, userFromDto } from '../user/User';
import { Identifier } from '../wrappers/identifier/Identifier';
import { CredentialsOutputModel } from '../../Dto/output/credentials/CredentialsOutputModel';

/**
 * Session Domain model
 *
 * @param id The unique identifier of the session.
 * @param user The user associated with the session.
 * @param accessToken The access token.
 * @param refreshToken The refresh token.
 * @param expiresAt The date and time when the session expires.
 */
export class Session {
    constructor(
        readonly id: Identifier,
        readonly user: User,
        readonly accessToken: AccessToken,
        readonly refreshToken: RefreshToken,
        readonly expiresAt: Date,
    ) {}

    static fromDto(dto: CredentialsOutputModel): Session {
        return new Session(
            new Identifier(dto.sessionID),
            userFromDto(dto.user),
            AccessToken.fromDto(dto.accessToken),
            RefreshToken.fromDto(dto.refreshToken),
            new Date(dto.refreshToken.expiresAt),
        );
    }

    isExpired(): boolean {
        return this.expiresAt < new Date();
    }
}
