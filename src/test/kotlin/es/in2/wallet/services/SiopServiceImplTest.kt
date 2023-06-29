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
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
class SiopServiceImplTest {

    private var tokenVerificationService: TokenVerificationService = Mockito.mock(TokenVerificationService::class.java)
    private var personalDataSpaceService: PersonalDataSpaceService = Mockito.mock(PersonalDataSpaceService::class.java)
    private var siopService: SiopService = Mockito.mock(SiopService::class.java)
    private val userUUID = UUID.randomUUID()
    private val siopAuthRequestUri = "https://portal.packetdelivery.com/authentication-requests?state=af0ifjsldkj"
    private val siopAuthRequest = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
            "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"


//    @Test
//    fun testGetSiopAuthenticationRequest() {
//        // Mock the behavior of getSiopAuthenticationRequestInJwsFormat
//        `when`(siopService.getSiopAuthenticationRequest(siopAuthRequestUri))
//            .thenReturn(mutableListOf("VerifiableId"))
//        // Mock the behavior of getAuthRequestClaim
//        val siopAuthenticationRequest = "siopAuthenticationRequest"
//        `when`(siopService.processSiopAuthenticationRequest(siopAuthRequestUri))
//            .thenReturn(mutableListOf("VerifiableId"))
//        // Mock the behavior of processSiopAuthenticationRequest
//        val result = mutableListOf("VerifiableId")
//        `when`(siopService.processSiopAuthenticationRequest(siopAuthenticationRequest))
//            .thenReturn(result)
//        // Call the getSiopAuthenticationRequest method
//        val response = siopService.getSiopAuthenticationRequest(siopAuthRequestUri)
//        // Verify the result
//        assertEquals(result, response)
//    }

}
