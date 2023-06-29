package es.in2.wallet.controller

import es.in2.wallet.exception.EmailAlreadyExistsException
import es.in2.wallet.exception.UsernameAlreadyExistsException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig

@SpringJUnitConfig
@SpringBootTest
@AutoConfigureMockMvc
class AppUserExceptionHandlerTest {

    @Test
    fun testHandleEmailAlreadyExistsException() {
        val exception = EmailAlreadyExistsException("Email already exists")
        val response = AppUserExceptionHandler().handleEmailAlreadyExistsException(exception)
        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun testHandleUsernameAlreadyExistsException() {
        val exception = UsernameAlreadyExistsException("Username already exists")
        val response = AppUserExceptionHandler().handleUsernameAlreadyExistsException(exception)
        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

}