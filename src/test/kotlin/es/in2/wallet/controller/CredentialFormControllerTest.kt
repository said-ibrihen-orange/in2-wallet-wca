package es.in2.wallet.controller
import es.in2.wallet.model.dto.CredentialFormResponseDTO
import es.in2.wallet.model.dto.DidResponseDTO
import es.in2.wallet.service.CredentialFormService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@SpringBootTest
@AutoConfigureMockMvc
class CredentialFormControllerTest {

    @Mock
    private lateinit var credentialFormService: CredentialFormService

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockMvc = MockMvcBuilders.standaloneSetup(CredentialFormController(credentialFormService)).build()
    }

    @Test
    fun testGetCredentialForm() {
        // Arrange
        val expectedProofTypeList = listOf("jwt")
        val expectedDidList = listOf(
            DidResponseDTO("did:example1"),
            DidResponseDTO("did:example2")
        )

        val expectedResponse = CredentialFormResponseDTO(expectedProofTypeList, expectedDidList)

        `when`(credentialFormService.getCredentialForm()).thenReturn(expectedResponse)

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/form"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.content().json(
                """
                {
                    "proofTypeList": ["jwt"],
                    "didList": [
                        {"did": "did:example1"},
                        {"did": "did:example2"}
                    ]
                }
                """.trimIndent()
            ))
    }
}