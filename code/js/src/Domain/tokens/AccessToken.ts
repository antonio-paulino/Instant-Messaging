import { AccessTokenOutputModel } from '../../Dto/output/credentials/AccessTokenOutputModel';

/**
 * Represents an access token.
 *
 * An access token is used to authenticate a user in the system.
 *
 * @param token The token.
 * @param expiresAt The date and time when the token expires.
 */
export class AccessToken {
    constructor(
        readonly token: string,
        readonly expiresAt: Date,
    ) {}

    static fromDto(dto: AccessTokenOutputModel): AccessToken {
        return new AccessToken(dto.token, new Date(dto.expiresAt));
    }

    isExpired(): boolean {
        return this.expiresAt < new Date();
    }
}
