package im.controllers

import im.model.input.AuthenticatedUser
import im.model.input.body.AuthenticationInputModel
import im.model.input.body.ImInvitationInputModel
import im.model.input.body.UserCredentialsInputModel
import im.model.output.CredentialsOutputModel
import im.model.output.ImInvitationOutputModel
import im.model.problems.Problem
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import im.services.Failure
import im.services.Success
import im.services.auth.AuthError
import im.services.auth.AuthService
import im.utils.RequestHelper
import jakarta.servlet.http.HttpServletResponse
import java.net.URI
import java.util.UUID


@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val reqHelper: RequestHelper,
) {

    @PostMapping("/register")
    fun register(
        @RequestBody @Valid userCredentials: UserCredentialsInputModel,
        response: HttpServletResponse,
    ): ResponseEntity<Any> {
        val (username, password, email, invitation) = userCredentials
        return when (val res = authService.register(
            username.toDomain(),
            password.toDomain(),
            email.toDomain(),
            UUID.fromString(invitation)
        )) {
            is Failure -> handleAuthFailure(res.value)
            is Success -> {
                val (accessToken, refreshToken) = res.value
                reqHelper.setCookie(response, accessToken)
                reqHelper.setCookie(response, refreshToken)
                ResponseEntity.created(URI("/users/${accessToken.session.user.id}"))
                    .body(CredentialsOutputModel.fromDomain(accessToken, refreshToken))
            }
        }
    }

    @PostMapping("/login")
    fun login(
        @RequestBody @Valid userAuth: AuthenticationInputModel,
        response: HttpServletResponse,
    ): ResponseEntity<Any> {
        val (username, password, email) = userAuth
        return when (val res = authService.login(
            username?.toDomain(),
            password.toDomain(),
            email?.toDomain()
        )) {
            is Failure -> handleAuthFailure(res.value)
            is Success -> {
                val (accessToken, refreshToken) = res.value
                reqHelper.setCookie(response, accessToken)
                reqHelper.setCookie(response, refreshToken)
                ResponseEntity.ok(
                    CredentialsOutputModel.fromDomain(accessToken, refreshToken)
                )
            }
        }
    }

    @PostMapping("/refresh")
    fun refresh(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Any> {
        val refreshToken = reqHelper.getRefreshToken(request)
            ?: return Problem.UnauthorizedProblem.response(
                HttpStatus.UNAUTHORIZED,
                "No refresh token provided"
            )
        return when (val res = authService.refreshSession(refreshToken)) {
            is Failure -> handleAuthFailure(res.value)
            is Success -> {
                val (accessToken, newRefreshToken) = res.value
                reqHelper.setCookie(response, accessToken)
                reqHelper.setCookie(response, newRefreshToken)
                ResponseEntity.ok(
                    CredentialsOutputModel.fromDomain(accessToken, newRefreshToken)
                )
            }
        }
    }

    @PostMapping("/logout")
    fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> {
        val accessToken: UUID = user.usedAccessToken
        val result = when (val res = authService.logout(accessToken)) {
            is Failure -> handleAuthFailure(res.value)
            is Success -> {
                reqHelper.clearAccessToken(response)
                reqHelper.clearRefreshToken(response)
                ResponseEntity.ok().build()
            }
        }
        return result
    }

    @PostMapping("/invitations")
    fun createInvitation(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @RequestBody @Valid invitation: ImInvitationInputModel?,
    ): ResponseEntity<Any> {
        return when (val res = authService.createInvitation(invitation?.expiration)) {
            is Failure -> handleAuthFailure(res.value)
            is Success -> {
                val invite = res.value
                ResponseEntity.created(URI("/invitations/${invite.token}"))
                    .body(ImInvitationOutputModel.fromDomain(invite))
            }
        }
    }

    private fun handleAuthFailure(error: AuthError): ResponseEntity<Any> {
        return when (error) {
            AuthError.InvalidCredentials -> Problem.UnauthorizedProblem.response(
                HttpStatus.UNAUTHORIZED,
                "Invalid credentials provided"
            )

            is AuthError.UserAlreadyExists -> Problem.UserAlreadyExistsProblem.response(
                HttpStatus.BAD_REQUEST,
                "User with that ${error.conflict} already exists."
            )

            AuthError.InvalidToken -> Problem.UnauthorizedProblem.response(
                HttpStatus.UNAUTHORIZED,
                "Invalid token"
            )

            AuthError.SessionExpired -> Problem.UnauthorizedProblem.response(
                HttpStatus.UNAUTHORIZED,
                "Session expired"
            )

            AuthError.TokenExpired -> Problem.UnauthorizedProblem.response(
                HttpStatus.UNAUTHORIZED,
                "Access token expired"
            )

            AuthError.InvalidInvitationCode -> Problem.InvalidInvitationProblem.response(
                HttpStatus.BAD_REQUEST,
                "Invalid invitation code"
            )

            AuthError.InvitationAlreadyUsed -> Problem.InvalidInvitationProblem.response(
                HttpStatus.BAD_REQUEST,
                "Invitation code already used"
            )

            AuthError.InvitationExpired -> Problem.InvalidInvitationProblem.response(
                HttpStatus.BAD_REQUEST,
                "Invitation code expired"
            )

            is AuthError.InvalidInvitationExpiration -> Problem.InvalidInvitationProblem.response(
                HttpStatus.BAD_REQUEST,
                error.message
            )
        }
    }
}