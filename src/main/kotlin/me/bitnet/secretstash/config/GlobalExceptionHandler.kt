package me.bitnet.secretstash.config

import io.github.oshai.kotlinlogging.KotlinLogging
import me.bitnet.secretstash.exception.DomainEntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = mutableMapOf<String, String>()

        ex.bindingResult.fieldErrors.forEach { error ->
            val fieldName = error.field
            val errorMessage = error.defaultMessage ?: "Validation failed"
            errors[fieldName] = errorMessage
        }

        // Log validation errors at DEBUG level since these are expected client errors
        logger.debug { "Validation failed: $errors" }

        val response =
            mapOf(
                "status" to HttpStatus.BAD_REQUEST.value(),
                "errors" to errors,
            )

        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<Map<String, Any>> {
        // Log at WARN level as this is a client error, but might indicate issues with API documentation or clients
        logger.warn { "Invalid request format: $ex.message" }

        val response =
            mapOf(
                "status" to HttpStatus.BAD_REQUEST.value(),
                "errors" to mapOf("message" to "Invalid request format or missing required fields"),
            )

        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(DomainEntityNotFoundException::class)
    fun handleEntityNotFoundException(ex: DomainEntityNotFoundException): ResponseEntity<Map<String, Any>> {
        // Log at INFO level since this is an expected application state, not an error
        logger.info { "Entity not found: $ex.message" }

        val response =
            mapOf(
                "status" to HttpStatus.NOT_FOUND.value(),
                "errors" to mapOf("message" to ex.message),
            )

        return ResponseEntity(response, HttpStatus.NOT_FOUND)
    }
}
