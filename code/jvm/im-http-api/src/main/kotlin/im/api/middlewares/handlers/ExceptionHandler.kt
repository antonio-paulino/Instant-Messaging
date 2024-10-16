package im.api.middlewares.handlers

import im.api.model.problems.InputValidationProblem
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

/**
 * Overrides the default exception handling done by Spring.
 */
@ControllerAdvice
class ExceptionHandler {
    /**
     * Handles validation exceptions thrown by the `@Valid` annotation.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun notValid(err: MethodArgumentNotValidException): ResponseEntity<*> =
        InputValidationProblem.InvalidInput.response(
            HttpStatus.BAD_REQUEST,
            err.bindingResult.allErrors.mapNotNull { it.defaultMessage },
        )

    /**
     * Handles exceptions thrown when the request body is not readable.
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun notReadable(): ResponseEntity<*> =
        InputValidationProblem.InvalidInput.response(
            HttpStatus.BAD_REQUEST,
            listOf("Invalid request body"),
        )
}
