package im.model.output

import im.tokens.AccessToken
import im.tokens.RefreshToken

data class CredentialsOutputModel(
    val user: UserOutputModel,
    val accessToken: AccessTokenOutputModel,
    val refreshToken: RefreshTokenOutputModel
) {
    companion object {
        fun fromDomain(accessToken: AccessToken, refreshToken: RefreshToken): CredentialsOutputModel {
            return CredentialsOutputModel(
                user = UserOutputModel.fromDomain(accessToken.session.user),
                accessToken = AccessTokenOutputModel.fromDomain(accessToken),
                refreshToken = RefreshTokenOutputModel.fromDomain(refreshToken)
            )
        }
    }
}