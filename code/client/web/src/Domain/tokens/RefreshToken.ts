import { RefreshTokenOutputModel } from '../../Dto/output/credentials/RefreshTokenOutputModel';

/**
 * Represents a refresh token.
 *
 * A refresh token is used to refresh a user's session.
 *
 * @param token The token.
 * @param expiresAt The date and time when the token expires.
 */
export class RefreshToken {
    constructor(
        readonly token: string,
        readonly expiresAt: Date,
    ) {}

    static fromDto(dto: RefreshTokenOutputModel): RefreshToken {
        return new RefreshToken(dto.token, new Date(dto.expiresAt));
    }

    isExpired(): boolean {
        return this.expiresAt < new Date();
    }
}
