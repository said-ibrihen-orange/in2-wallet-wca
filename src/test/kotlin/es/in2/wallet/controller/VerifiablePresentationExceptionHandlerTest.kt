package es.in2.wallet.controller

import es.in2.wallet.exception.DidVerificationException
import es.in2.wallet.exception.InvalidTokenException
import es.in2.wallet.exception.JwtInvalidFormatException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@SpringJUnitConfig
@SpringBootTest
@AutoConfigureMockMvc
class VerifiablePresentationExceptionHandlerTest {

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(VerifiablePresentationExceptionHandlerTest::class.java)
        mockMvc = MockMvcBuilders.standaloneSetup().build()
    }

    @Test
    fun `testHandleDidVerificationException should return 404 NOT_FOUND`() {
        val exception = DidVerificationException("Did failed")
        val response = VerifiablePresentationExceptionHandler().handleDidVerificationException(exception)
        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `testHandleJwtInvalidFormatException should return 400 BAD_REQUEST`() {
        val exception = JwtInvalidFormatException("Invalid format")
        val response = VerifiablePresentationExceptionHandler().handleVerificationException(exception)
        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `testHandleInvalidTokenException should return 400 BAD_REQUEST`() {
        val exception = InvalidTokenException("Invalid token")
        val response = VerifiablePresentationExceptionHandler().handleRequestTokenVerificationException(exception)
        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

}