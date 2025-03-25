package me.bitnet.secretstash.config

import io.github.oshai.kotlinlogging.KotlinLogging
import me.bitnet.secretstash.note.exception.NoteNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.util.UUID

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

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatchException(ex: MethodArgumentTypeMismatchException): ResponseEntity<Map<String, Any>> {
        val errorMessage =
            if (ex.requiredType != null && ex.requiredType == UUID::class.java) {
                "Invalid UUID format for parameter '${ex.name}'"
            } else {
                "Invalid parameter value: '${ex.name}' must be of type ${ex.requiredType?.simpleName ?: "unknown"}"
            }

        // Log at DEBUG level as this is a client error
        logger.debug { "Type mismatch for parameter ${ex.name}: ${ex.message}" }

        val response =
            mapOf(
                "status" to HttpStatus.BAD_REQUEST.value(),
                "errors" to mapOf("message" to errorMessage),
            )

        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MissingPathVariableException::class)
    fun handleMissingPathVariableException(ex: MissingPathVariableException): ResponseEntity<Map<String, Any>> {
        // Log at DEBUG level since this is a client error
        logger.debug { "Missing path variable: ${ex.variableName}" }

        val response =
            mapOf(
                "status" to HttpStatus.BAD_REQUEST.value(),
                "errors" to mapOf("message" to "Missing required path variable: '${ex.variableName}'"),
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

    @ExceptionHandler(NoteNotFoundException::class)
    fun handleNoteNotFoundException(ex: NoteNotFoundException): ResponseEntity<Map<String, Any>> {
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
