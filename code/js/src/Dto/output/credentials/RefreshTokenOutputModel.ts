/**
 * Refresh token output model
 *
 * @param token The token.
 * @param expiresAt The date and time when the token expires.
 */
export interface RefreshTokenOutputModel {
    token: string;
    expiresAt: string;
}
