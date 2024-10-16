package im.api.controllers

import im.api.middlewares.authentication.Authenticated
import im.api.model.input.body.AuthenticationInputModel
import im.api.model.input.body.ImInvitationCreationInputModel
import im.api.model.input.body.UserCreationInputModel
import im.api.model.output.credentials.CredentialsOutputModel
import im.api.model.output.invitations.ImInvitationOutputModel
import im.api.model.output.users.UserCreationOutputModel
import im.api.model.problems.Problem
import im.api.utils.RequestHelper
import im.domain.Failure
import im.domain.Success
import im.domain.user.AuthenticatedUser
import im.services.auth.AuthService
import im.services.invitations.InvitationService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val invitationService: InvitationService,
    private val reqHelper: RequestHelper,
    private val errorHandler: ErrorHandler,
) {
    private val handleAuthFailure = errorHandler::handleAuthFailure
    private val handleInvitationFailure = errorHandler::handleInvitationFailure

    /**
     * Register a new user.
     *
     *
     *  Possible status codes:
     *  - 201 Created: User successfully registered.
     *  - 400 Bad Request: Invalid input.
     *  - 409 Conflict: User already exists.
     *
     *
     * @param userCredentials The user's credentials.
     *
     * @return The response entity.
     *
     * @see UserCreationInputModel
     * @see CredentialsOutputModel
     */
    @PostMapping("/register")
    fun register(
        @RequestBody @Valid userCredentials: UserCreationInputModel,
    ): ResponseEntity<Any> {
        val (username, password, email, invitation) = userCredentials
        return when (
            val res =
                authService.register(
                    username.toDomain(),
                    password.toDomain(),
                    email.toDomain(),
                    UUID.fromString(invitation),
                )
        ) {
            is Failure -> handleAuthFailure(res.value)
            is Success -> {
                val user = res.value
                ResponseEntity
                    .created(URI("/api/users/${user.id}"))
                    .body(UserCreationOutputModel.fromDomain(user))
            }
        }
    }

    /**
     * Login a user.
     *
     *  Possible status codes:
     *   - 200 OK: User successfully logged in.
     *   - 400 Bad Request: Invalid input data.
     *   - 401 Unauthorized: Invalid credentials.
     *
     * @param userAuth The user's authentication data.
     * @param response The HTTP response.
     *
     * @return The response entity.
     *
     * @see AuthenticationInputModel
     * @see CredentialsOutputModel
     *
     */
    @PostMapping("/login")
    fun login(
        @RequestBody @Valid userAuth: AuthenticationInputModel,
        response: HttpServletResponse,
    ): ResponseEntity<Any> {
        val (username, password, email) = userAuth
        return when (
            val res =
                authService.login(
                    username?.toDomain(),
                    password.toDomain(),
                    email?.toDomain(),
                )
        ) {
            is Failure -> handleAuthFailure(res.value)
            is Success -> {
                val (accessToken, refreshToken) = res.value
                reqHelper.setCookie(response, accessToken)
                reqHelper.setCookie(response, refreshToken)
                ResponseEntity.ok(
                    CredentialsOutputModel.fromDomain(accessToken, refreshToken),
                )
            }
        }
    }

    /**
     * Refresh the session.
     *
     * @param request The HTTP request.
     * @param response The HTTP response.
     *
     * @return The response entity.
     *
     * Possible status codes:
     * - 200 OK: Session successfully refreshed.
     * - 401 Unauthorized: Invalid refresh token provided or session expired.
     */
    @PostMapping("/refresh")
    fun refresh(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Any> {
        val refreshToken =
            reqHelper.getRefreshToken(request)
                ?: return Problem.UnauthorizedProblem.response(
                    HttpStatus.UNAUTHORIZED,
                    "No refresh token provided",
                )
        return when (val res = authService.refreshSession(refreshToken)) {
            is Failure -> handleAuthFailure(res.value)
            is Success -> {
                val (accessToken, newRefreshToken) = res.value
                reqHelper.setCookie(response, accessToken)
                reqHelper.setCookie(response, newRefreshToken)
                ResponseEntity.ok(
                    CredentialsOutputModel.fromDomain(accessToken, newRefreshToken),
                )
            }
        }
    }

    /**
     * Logout a user.
     *
     * Possible status codes:
     *  - 200 OK: User successfully logged out.
     *  - 401 Unauthorized: Invalid token.
     *
     * @param response The HTTP response.
     * @param user The authenticated user.
     *
     * @return The response entity.
     *
     * @see AuthenticatedUser
     */
    @PostMapping("/logout")
    @Authenticated
    fun logout(
        response: HttpServletResponse,
        user: AuthenticatedUser,
    ): ResponseEntity<Any> {
        val accessToken: UUID = user.usedAccessToken
        val result =
            when (val res = authService.logout(accessToken)) {
                is Failure -> handleAuthFailure(res.value)
                is Success -> {
                    reqHelper.clearAccessToken(response)
                    reqHelper.clearRefreshToken(response)
                    ResponseEntity.ok().build()
                }
            }
        return result
    }

    /**
     * Create an invitation.
     *
     * Possible status codes:
     * - 201 Created: Invitation successfully created.
     * - 400 Bad Request: Invalid invitation expiration.
     *
     * @param invitation The invitation creation data.
     *
     * @return The response entity.
     *
     * @see ImInvitationCreationInputModel
     * @see ImInvitationOutputModel
     *
     */
    @PostMapping("/invitations")
    @Authenticated
    fun createInvitation(
        @RequestBody @Valid invitation: ImInvitationCreationInputModel?,
    ): ResponseEntity<Any> =
        when (val res = invitationService.createImInvitation(invitation?.expiresAt)) {
            is Failure -> handleInvitationFailure(res.value)
            is Success -> {
                val invite = res.value
                ResponseEntity
                    .created(URI("/api/invitations/${invite.token}"))
                    .body(ImInvitationOutputModel.fromDomain(invite))
            }
        }
}
