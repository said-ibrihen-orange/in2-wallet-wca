package es.in2.wallet.controller

import es.in2.wallet.exception.NoSuchQrContentException
import es.in2.wallet.exception.NoSuchVerifiableCredentialException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig

@SpringJUnitConfig
@SpringBootTest
@AutoConfigureMockMvc
class QrCodeProcessorExceptionHandlerTest {

    @Test
    fun testHandleNoSuchQrContentException() {
        val exception = NoSuchQrContentException("Unknown QR content")
        val response = QrCodeProcessorExceptionHandler().handleNoSuchQrContentException(exception)
        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun testHandleNoSuchVerifiableCredentialException() {
        val exception = NoSuchVerifiableCredentialException("There are no Verifiable Credential bound to the user")
        val response = QrCodeProcessorExceptionHandler().handleNoSuchVerifiableCredentialException(exception)
        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

}