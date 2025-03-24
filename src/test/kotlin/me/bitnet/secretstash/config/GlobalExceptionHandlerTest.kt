package me.bitnet.secretstash.config

import me.bitnet.secretstash.note.exception.NoteNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

class GlobalExceptionHandlerTest {
    private val exceptionHandler = GlobalExceptionHandler()

    @Test
    fun `test handleMethodArgumentNotValidException returns validation errors`() {
        // Arrange
        val fieldError = FieldError("testObject", "testField", "Test validation message")
        val bindingResult = mock(BindingResult::class.java)
        whenever(bindingResult.fieldErrors).thenReturn(listOf(fieldError))

        val exception = mock(MethodArgumentNotValidException::class.java)
        whenever(exception.bindingResult).thenReturn(bindingResult)

        // Act
        val responseEntity = exceptionHandler.handleValidationExceptions(exception)

        // Assert
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)

        val responseBody = responseEntity.body!!
        assertThat(responseBody["status"]).isEqualTo(HttpStatus.BAD_REQUEST.value())

        val errors = responseBody["errors"] as Map<*, *>
        assertThat(errors["testField"]).isEqualTo("Test validation message")
    }

    @Test
    fun `test handleHttpMessageNotReadableException returns generic message`() {
        // Arrange
        val exception = mock(HttpMessageNotReadableException::class.java)

        // Act
        val responseEntity = exceptionHandler.handleHttpMessageNotReadableException(exception)

        // Assert
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)

        val responseBody = responseEntity.body!!
        assertThat(responseBody["status"]).isEqualTo(HttpStatus.BAD_REQUEST.value())

        val errors = responseBody["errors"] as Map<*, *>
        assertThat(errors["message"]).isEqualTo("Invalid request format or missing required fields")
    }

    @Test
    fun `test handleEntityNotFoundException returns exception message`() {
        // Arrange
        val errorMessage = "Entity with ID 123 not found"
        val exception = NoteNotFoundException(errorMessage)

        // Act
        val responseEntity = exceptionHandler.handleNoteNotFoundException(exception)

        // Assert
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.NOT_FOUND)

        val responseBody = responseEntity.body!!
        assertThat(responseBody["status"]).isEqualTo(HttpStatus.NOT_FOUND.value())

        val errors = responseBody["errors"] as Map<*, *>
        assertThat(errors["message"]).isEqualTo(errorMessage)
    }
}
