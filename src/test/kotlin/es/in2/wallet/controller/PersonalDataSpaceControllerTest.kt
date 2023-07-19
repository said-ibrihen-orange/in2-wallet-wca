package es.in2.wallet.controller

import es.in2.wallet.exception.InvalidDIDFormatException
import es.in2.wallet.service.PersonalDataSpaceService
import es.in2.wallet.service.WalletDidService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@SpringJUnitConfig
@SpringBootTest
@AutoConfigureMockMvc
class PersonalDataSpaceControllerTest {

    @Mock
    private lateinit var personalDataSpaceService: PersonalDataSpaceService

    @Mock
    private lateinit var walletDidService: WalletDidService

    @InjectMocks
    private lateinit var personalDataSpaceController: PersonalDataSpaceController

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        mockMvc = MockMvcBuilders.standaloneSetup(personalDataSpaceController).build()
    }

    @Test
    fun `createDidKey should return 200 OK`() {
        val didKey = "did:key:123456789abcdef"
        `when`(personalDataSpaceController.createDidKey()).thenReturn(didKey)

        mockMvc.perform(MockMvcRequestBuilders.post("/api/personal-data-space/dids/createkey"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(didKey))

    }
    @Test
    fun `createDidElsi should return 500 INTERNAL SERVER ERROR`() {
        val didElsi = "didweb:123456789abcdef"
        val errorMessage = "Invalid DID format"

        `when`(personalDataSpaceController.createDidElsi(didElsi)).thenThrow(InvalidDIDFormatException(errorMessage))

        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/api/personal-data-space/dids/createelsi/$didElsi"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError)
                .andExpect(MockMvcResultMatchers.content().string(errorMessage))
        } catch (ex: Exception) {
            println("Error message: ${ex.message}")
            println("Root cause: ${ex.cause?.message}")
        }
    }



    @Test
    fun `createDidElsi should return 200 OK`() {
        val didElsi = "did:elsi:123456789abcdef"


        mockMvc.perform(MockMvcRequestBuilders.post("/api/personal-data-space/dids/createelsi/$didElsi"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(didElsi))

    }
/*
    @Test
    fun `getVerifiableCredentialList should return 200 OK`() {
        // Create a list of VcBasicDataDTO objects for the expected response
        val vcBasicDataDTO = VcBasicDataDTO(
            id = "urn:uuid:59dabec0-a6f1-4455-8f3f-c13955f27bba",
            vcType = mutableListOf("VerifiableCredential", "VerifiableAttestation", "VerifiableId"),
            credentialSubject = "subject data"
        )

        // Add objects to the list as needed
        val expectedList = mutableListOf(vcBasicDataDTO)

        // Mock the personalDataSpaceService to return the expected list
        `when`(personalDataSpaceService.getUserVCsInJson()).thenReturn(expectedList)

        // Perform the GET request and assert the response
        mockMvc.perform(MockMvcRequestBuilders.get("/api/personal-data-space"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].vcType").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].vcType", Matchers.hasSize<String>(3)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].vcType[0]").value("VerifiableCredential"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].vcType[1]").value("VerifiableAttestation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].vcType[2]").value("VerifiableId"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("urn:uuid:59dabec0-a6f1-4455-8f3f-c13955f27bba"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].credentialSubject").value("subject data"))

        // Add additional assertions as needed
    }*/

}
