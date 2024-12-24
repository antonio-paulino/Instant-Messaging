package im.api.model.output.credentials

import im.api.model.output.users.UserOutputModel
import im.domain.tokens.AccessToken
import im.domain.tokens.RefreshToken

data class CredentialsOutputModel(
    val sessionID: Long,
    val user: UserOutputModel,
    val accessToken: AccessTokenOutputModel,
    val refreshToken: RefreshTokenOutputModel,
) {
    companion object {
        fun fromDomain(
            accessToken: AccessToken,
            refreshToken: RefreshToken,
        ): CredentialsOutputModel =
            CredentialsOutputModel(
                sessionID = accessToken.session.id.value,
                user = UserOutputModel.fromDomain(accessToken.session.user),
                accessToken = AccessTokenOutputModel.fromDomain(accessToken),
                refreshToken =
                    RefreshTokenOutputModel
                        .fromDomain(refreshToken),
            )
    }
}
