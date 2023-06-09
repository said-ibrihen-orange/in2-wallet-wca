package es.in2.wallet.services

import es.in2.wallet.exception.NoSuchQrContentException
import es.in2.wallet.service.PersonalDataSpaceService
import es.in2.wallet.service.QrCodeProcessorService
import es.in2.wallet.service.SiopService
import es.in2.wallet.service.VerifiableCredentialService
import es.in2.wallet.service.impl.QrCodeProcessorServiceImpl
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.EnabledIf
import java.util.*

@SpringBootTest
class QrCodeProcessorServiceImplTest {

    private val siopService: SiopService = mock(SiopService::class.java)
    private val verifiableCredentialService: VerifiableCredentialService = mock(VerifiableCredentialService::class.java)
    private val personalDataSpaceService: PersonalDataSpaceService = mock(PersonalDataSpaceService::class.java)
    private val qrCodeProcessorService: QrCodeProcessorService = QrCodeProcessorServiceImpl(
        siopService, verifiableCredentialService, personalDataSpaceService
    )

    @Test
    fun testProcessQrContentSiopAuthRequestUri() {
        // Mock behavior
        val qrContent = "https://example.com/authentication-requests/12345"
        `when`(siopService.getSiopAuthenticationRequest(qrContent)).thenReturn(mutableListOf("VerifiableId"))
        // Call the method
        val result = qrCodeProcessorService.processQrContent(qrContent)
        // Verify behavior and assertions
        verify(siopService).getSiopAuthenticationRequest(qrContent)
        Assertions.assertEquals(mutableListOf("VerifiableId"), result)
    }

    @Test
    fun testProcessQrContentSiopAuthRequest() {
        // Mock behavior
        val qrContent =
            "openid://?scope=VerifiableId&response_type=vp_token&response_mode=direct_post" +
                    "&client_id=did:elsi:packetdelivery" +
                    "&redirect_uri=https://www.packetdelivery.com/api/authentication_response&state=af0ifjsldkj" +
                    "&nonce=n-0S6_WzA2Mj"
        `when`(
            siopService.processSiopAuthenticationRequest(qrContent)
        ).thenReturn(mutableListOf("VerifiableId"))
        // Call the method
        val result = qrCodeProcessorService.processQrContent(qrContent)
        // Verify behavior and assertions
        verify(siopService).processSiopAuthenticationRequest(qrContent)
        Assertions.assertEquals(mutableListOf("VerifiableId"), result)
    }

    @Test
    fun testProcessQrContentCredentialOfferUri() {
        // Mock behavior
        val qrContent = "https://example.com/credential-offers/12345"
        // Call the method
        qrCodeProcessorService.processQrContent(qrContent)
        // Verify behavior and assertions
        verify(verifiableCredentialService).getVerifiableCredential(qrContent)
    }

    @Test
    @EnabledIf(expression = "#{environment['spring.profiles.active'] == 'local'}", loadContext = true)
    fun testProcessQrContentVcJwt() {
        // Mock behavior
        val qrContent =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9" +
                    "lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        // Verify behavior and assertions
        verify(personalDataSpaceService).saveVC(qrContent)
    }

    @Test
    fun testProcessQrContentUnknown() {
        // Call the method with unknown QR content
        val qrContent = "unknown-content"
        // Call the method
        val exception = assertThrows(NoSuchQrContentException::class.java) {
            qrCodeProcessorService.processQrContent(qrContent)
        }
        // Verify behavior and assertions
        Assertions.assertEquals("The received QR content cannot be processed", exception.message)
    }

}