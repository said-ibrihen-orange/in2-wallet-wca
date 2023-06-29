package es.in2.wallet.services

import es.in2.wallet.controller.QrCodeProcessorController
import es.in2.wallet.exception.NoSuchQrContentException
import es.in2.wallet.service.impl.PersonalDataSpaceServiceImpl
import es.in2.wallet.service.impl.QrCodeProcessorServiceImpl
import es.in2.wallet.service.impl.SiopServiceImpl
import es.in2.wallet.service.impl.VerifiableCredentialServiceImpl
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.EnabledIf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@SpringBootTest
@AutoConfigureMockMvc
class QrCodeProcessorServiceImplTest {

    @MockBean
    private lateinit var siopService: SiopServiceImpl

    @MockBean
    private lateinit var verifiableCredentialService: VerifiableCredentialServiceImpl

    @MockBean
    private lateinit var personalDataSpaceService: PersonalDataSpaceServiceImpl

    @MockBean
    private lateinit var qrCodeProcessorService: QrCodeProcessorServiceImpl

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(QrCodeProcessorController(qrCodeProcessorService)).build()
        qrCodeProcessorService = QrCodeProcessorServiceImpl(
            siopService,
            verifiableCredentialService,
            personalDataSpaceService
        )
//        clearMocks(siopService, verifiableCredentialService, personalDataSpaceService)
    }

//    @Test
//    @EnabledIf(expression = "#{environment['spring.profiles.active'] == 'local'}", loadContext = true)
//    fun `processQrContent should return SiopAuthenticationRequest when QR content is SIOP_AUTH_REQUEST_URI`() {
//        val qrContent = "https://example.com/authentication-request"
//
//        every { siopService.getSiopAuthenticationRequest(qrContent) } returns VcSelectorRequestDTO(
//            redirectUri = "https://example.com/siop-sessions",
//            state = "1234",
//            selectableVcList = listOf(VcBasicDataDTO(
//                id = "1",
//                vcType = mutableListOf("VerifiableId"),
//                credentialSubject = "custom data"
//            ))
//        )
//
//        mockMvc.perform(get("/process-qr-content?qrContent=$qrContent"))
//            .andExpect(status().isOk)
//    }
//
//    @Test
//    fun `processQrContent should return VerifiableCredential when QR content is CREDENTIAL_OFFER_URI`() {
//        val qrContent = "https://example.com/credential-offer"
//
//        every { verifiableCredentialService.getVerifiableCredential(qrContent) } returns Unit
//
//        mockMvc.perform(get("/process-qr-content?qrContent=$qrContent"))
//            .andExpect(status().isOk)
//    }
//
//    @Test
//    fun `processQrContent should save VerifiableCredential when QR content is VC_JWT`() {
//        val qrContent = "eyJ..."
//
//        every { personalDataSpaceService.saveVC(qrContent) } just Runs
//
//        mockMvc.perform(get("/process-qr-content?qrContent=$qrContent"))
//            .andExpect(status().isOk)
//    }

//    private val siopService: SiopService = mock(SiopService::class.java)
//    private val verifiableCredentialService: VerifiableCredentialService = mock(VerifiableCredentialService::class.java)
//    private val personalDataSpaceService: PersonalDataSpaceService = mock(PersonalDataSpaceService::class.java)
//    private val qrCodeProcessorService: QrCodeProcessorService = QrCodeProcessorServiceImpl(
//        siopService, verifiableCredentialService, personalDataSpaceService
//    )
//
//    @Test
//    fun testProcessQrContentSiopAuthRequestUri() {
//        VcSelectorRequestDTO()
//        // Mock behavior
//        val qrContent = "https://example.com/authentication-requests/12345"
//        `when`(siopService.getSiopAuthenticationRequest(qrContent)).thenReturn(mutableListOf("VerifiableId"))
//        // Call the method
//        val result = qrCodeProcessorService.processQrContent(qrContent)
//        // Verify behavior and assertions
//        verify(siopService).getSiopAuthenticationRequest(qrContent)
//        Assertions.assertEquals(mutableListOf("VerifiableId"), result)
//    }
//
//    @Test
//    fun testProcessQrContentSiopAuthRequest() {
//        // Mock behavior
//        val qrContent =
//            "openid://?scope=VerifiableId&response_type=vp_token&response_mode=direct_post" +
//                    "&client_id=did:elsi:packetdelivery" +
//                    "&redirect_uri=https://www.packetdelivery.com/api/authentication_response&state=af0ifjsldkj" +
//                    "&nonce=n-0S6_WzA2Mj"
//        `when`(
//            siopService.processSiopAuthenticationRequest(qrContent)
//        ).thenReturn(mutableListOf("VerifiableId"))
//        // Call the method
//        val result = qrCodeProcessorService.processQrContent(qrContent)
//        // Verify behavior and assertions
//        verify(siopService).processSiopAuthenticationRequest(qrContent)
//        Assertions.assertEquals(mutableListOf("VerifiableId"), result)
//    }
//
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

    @Test
    fun `processQrContent should throw NoSuchQrContentException when QR content is unknown`() {
        val qrContent = "unknown-content"
        mockMvc.perform(get("/process-qr-content?qrContent=$qrContent"))
            .andExpect(status().isNotFound)
    }

}