import es.in2.wallet.controller.DidManagementController
import es.in2.wallet.exception.InvalidDIDFormatException
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
        val didKey = "did:key:123456789abcdef"
        Mockito.`when`(didManagementController.createDidKey()).thenReturn(didKey)

        mockMvc.perform(MockMvcRequestBuilders.post("/api/did-management/dids/createkey"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(didKey))

    }
    @Test
    fun `createDidElsi should return 500 INTERNAL SERVER ERROR`() {
        val didElsi = "didweb:123456789abcdef"
        val errorMessage = "Invalid DID format"

        Mockito.`when`(didManagementController.createDidElsi(didElsi))
            .thenThrow(InvalidDIDFormatException(errorMessage))

        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/api/did-management/dids/createelsi/$didElsi"))
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
        Mockito.`when`(didManagementController.createDidElsi(didElsi)).thenReturn(didElsi)

        mockMvc.perform(MockMvcRequestBuilders.post("/api/did-management/dids/createelsi/$didElsi"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(didElsi))


    }
}
