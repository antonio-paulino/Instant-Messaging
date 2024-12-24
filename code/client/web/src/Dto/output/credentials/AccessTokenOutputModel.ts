/**
 * Access token output model.
 *
 * @param token The token.
 * @param expiresAt The date and time when the token expires.
 */
export interface AccessTokenOutputModel {
    token: string;
    expiresAt: string;
}
