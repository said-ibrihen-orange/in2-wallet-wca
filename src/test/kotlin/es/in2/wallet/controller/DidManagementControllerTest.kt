import es.in2.wallet.controller.DidManagementController
import es.in2.wallet.model.dto.DidRequestDTO
import es.in2.wallet.service.PersonalDataSpaceService
import es.in2.wallet.service.WalletDidService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@RunWith(MockitoJUnitRunner::class)
class DidManagementControllerTest {

    @Mock
    private lateinit var personalDataSpaceService: PersonalDataSpaceService

    @Mock
    private lateinit var walletDidService: WalletDidService

    @InjectMocks
    private lateinit var didManagementController: DidManagementController

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        mockMvc = MockMvcBuilders.standaloneSetup(didManagementController).build()
    }


    @Test
    fun `createDidKey should return 200 OK`() {
        val did = DidRequestDTO("key", null)
        Mockito.`when`(didManagementController.createDid(did)).thenReturn("DID created")

        val jsonRequestDTO =  """
            {
              "type": "key",
              "value": null
            }
        """.trimIndent()

        mockMvc.perform(MockMvcRequestBuilders.post("/api/dids").contentType(MediaType.APPLICATION_JSON).content(jsonRequestDTO))
            .andExpect(MockMvcResultMatchers.status().isCreated)

    }



    @Test
    fun `createDidElsi should return 200 OK`() {
        val did = DidRequestDTO("elsi", "did:elsi:56789")
        Mockito.`when`(didManagementController.createDid(did)).thenReturn("DID created")

        val jsonRequestDTO =  """
            {
              "type": "elsi",
              "value": "did:elsi:56789"
            }
        """.trimIndent()

        mockMvc.perform(MockMvcRequestBuilders.post("/api/dids").contentType(MediaType.APPLICATION_JSON).content(jsonRequestDTO))
            .andExpect(MockMvcResultMatchers.status().isCreated)


    }

    @Test
    fun `createDid should return 500 INTERNAL SERVER ERROR`() {
        val did = DidRequestDTO("key", "did:web:200")
        val errorMessage = "Value must be null for 'key' type DID"
        Mockito.`when`(didManagementController.createDid(did))

        val jsonRequestDTO =  """
            {
              "type": "key",
              "value": "did:web:200"
            }
        """.trimIndent()

        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/api/dids").contentType(MediaType.APPLICATION_JSON).content(jsonRequestDTO))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError)
                .andExpect(MockMvcResultMatchers.content().string(errorMessage))
        } catch (ex: Exception) {
            println("Error message: ${ex.message}")
            println("Root cause: ${ex.cause?.message}")
        }

    }

    @Test
    fun `createDidElsi should return 500 INTERNAL SERVER ERROR`() {
        val did = DidRequestDTO("elsi", "didweb:123456789abcdef")
        val errorMessage = "DID does not match the pattern"

        Mockito.`when`(didManagementController.createDid(did))

        val jsonRequestDTO =  """
            {
              "type": "elsi",
              "value": "didweb:123456789abcdef"
            }
        """.trimIndent()

        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/api/dids").contentType(MediaType.APPLICATION_JSON).content(jsonRequestDTO))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError)
                .andExpect(MockMvcResultMatchers.content().string(errorMessage))
        } catch (ex: Exception) {
            println("Error message: ${ex.message}")
            println("Root cause: ${ex.cause?.message}")
        }
    }
}
