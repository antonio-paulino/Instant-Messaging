package im.middlewares.handlers

import im.model.problems.InputValidationProblem
import jakarta.servlet.http.HttpServletRequest
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
     * Handles validation exceptions.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun notValid(err: MethodArgumentNotValidException, request: HttpServletRequest?): ResponseEntity<*> {
        return InputValidationProblem.InvalidInput.response(
            HttpStatus.BAD_REQUEST,
            err.bindingResult.allErrors.mapNotNull { it.defaultMessage }
        )
    }

    /**
     * Handles exceptions thrown when the request body is not readable.
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun notReadable(err: HttpMessageNotReadableException, request: HttpServletRequest?): ResponseEntity<*> {
        return InputValidationProblem.InvalidInput.response(
            HttpStatus.BAD_REQUEST,
            listOf("Invalid request body")
        )
    }
}