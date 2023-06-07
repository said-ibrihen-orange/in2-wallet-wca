package es.in2.wallet.services

import es.in2.wallet.service.PersonalDataSpaceService
import es.in2.wallet.service.SiopService
import es.in2.wallet.service.TokenVerificationService
import es.in2.wallet.service.VerifiableCredentialService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.*

class SiopServiceImplTest {

    private var tokenVerificationService: TokenVerificationService = Mockito.mock(TokenVerificationService::class.java)
    private var personalDataSpaceService: PersonalDataSpaceService = Mockito.mock(PersonalDataSpaceService::class.java)
    private var siopService: SiopService = Mockito.mock(SiopService::class.java)
    private val userUUID = UUID.randomUUID()
    private val siopAuthRequestUri = "https://portal.packetdelivery.com/authentication-requests?state=af0ifjsldkj"
    private val siopAuthRequest = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
            "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

    @Test
    fun testGetSiopAuthenticationRequest_ValidUri() {
//        // Mock dependencies
//        `when`(personalDataSpaceService.getVerifiableCredentialsByVcType(userUUID, listOf("VerifiableId")))
//            .thenReturn(mutableListOf("vc1", "vc2"))
//        // Call the method
//        val result = siopService.getSiopAuthenticationRequest(userUUID, siopAuthRequestUri)
//        // Verify the calls and assertions
//        verify(tokenVerificationService).verifySiopAuthRequestAsJwsFormat(siopAuthRequest)
//        verify(personalDataSpaceService).getVerifiableCredentialsByVcType(userUUID, listOf("VerifiableId"))
//        assertEquals(mutableListOf(siopAuthRequest, "vc1", "vc2"), result)
    }

    @Test
    fun testGetSiopAuthenticationRequest_InvalidUri() {
//        // Call the method and assert the exception
//        assertThrows<Exception> {
//            siopService.getSiopAuthenticationRequest(userUUID, siopAuthRequestUri)
//        }
    }

    @Test
    fun testProcessSiopAuthenticationRequest() {
//        // Mock dependencies
//        `when`(personalDataSpaceService.getVerifiableCredentialsByVcType(userUUID, listOf("VerifiableId")))
//            .thenReturn(mutableListOf("vc1", "vc2"))
//        // Call the method
//        val result = siopService.processSiopAuthenticationRequest(userUUID, siopAuthRequest)
//        // Verify the calls and assertions
//        verify(personalDataSpaceService).getVerifiableCredentialsByVcType(userUUID, listOf("VerifiableId"))
//        assertEquals(mutableListOf(siopAuthRequest, "vc1", "vc2"), result)
    }

    @Test
    fun testSendAuthenticationResponse() {
//        // Mock dependencies
//        // Call the method
//        val response = siopService.sendAuthenticationResponse(siopAuthRequest, "vp")
//        // Verify the calls and assertions
//        // Add your assertions for the response as per your requirements
    }

}
