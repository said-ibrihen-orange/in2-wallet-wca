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
        val jsonRequestDTO = """
            {
              "type": "key",
              "value": null
            }
        """.trimIndent()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/dids").contentType(MediaType.APPLICATION_JSON).content(jsonRequestDTO)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)

    }


    @Test
    fun `createDidElsi should return 200 OK`() {
        val jsonRequestDTO = """
            {
              "type": "elsi",
              "value": "did:elsi:56789"
            }
        """.trimIndent()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/dids").contentType(MediaType.APPLICATION_JSON).content(jsonRequestDTO)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)


    }

    @Test
    fun `createDid should return 500 INTERNAL SERVER ERROR`() {
        val did = DidRequestDTO("key", "did:web:200")
        val errorMessage = "Value must be null for 'key' type DID"
        Mockito.`when`(didManagementController.createDid(did))

        val jsonRequestDTO = """
            {
              "type": "key",
              "value": "did:web:200"
            }
        """.trimIndent()

        try {
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/dids").contentType(MediaType.APPLICATION_JSON).content(jsonRequestDTO)
            )
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

        val jsonRequestDTO = """
            {
              "type": "elsi",
              "value": "didweb:123456789abcdef"
            }
        """.trimIndent()

        try {
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/dids").contentType(MediaType.APPLICATION_JSON).content(jsonRequestDTO)
            )
                .andExpect(MockMvcResultMatchers.status().isInternalServerError)
                .andExpect(MockMvcResultMatchers.content().string(errorMessage))
        } catch (ex: Exception) {
            println("Error message: ${ex.message}")
            println("Root cause: ${ex.cause?.message}")
        }
    }

    /*@Test
    fun `getDidList should return 200 OK`() {

        val userUUID = "fff36f29-2155-4647-aacf-e01e6f54cc91"

        val responseJsonArray = """
        [
            {
                "did": "did:key:z6MkvP5DbcyqCd8edocU8vU9yEpbnsSopnxCD7bybTPD95gZ"
            },
            {
                "did": "did:elsi:sasas"
            }
        ]
    """.trimIndent()

        val expectedDidResponseDTOs = mutableListOf(
            DidResponseDTO("did:key:z6MkvP5DbcyqCd8edocU8vU9yEpbnsSopnxCD7bybTPD95gZ"),
            DidResponseDTO("did:elsi:sasas")
        )

        Mockito.`when`(didManagementController.getDidList()).thenReturn(expectedDidResponseDTOs)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/dids")
                .param("userId.value", userUUID)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.content().json(responseJsonArray))
    }*/

    @Test
    fun `Delete Did should return 200 OK`() {
        val did = "did:key:zDnaeucFNSnCmRGj5VucjxJEJS6yhF9PtnfSjCyBMGza2Wt97"
        val userUUID = "fff36f29-2155-4647-aacf-e01e6f54cc91"

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/dids/{did}", did)
                .param("userId.value", userUUID)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `Delete Did should return 500 INTERNAL SERVER ERROR`() {
        val did = "did:key:zDnaeucFNSnCmRGj5VucjxJEJS6yhF9PtnfSjCyBMGza2Wt97"
        val errorMessage = "DID not found: did:key:sdsdsdsdsdsdsdsdsdsdsdsd"
        val userUUID = "fff36f29-2155-4647-aacf-e01e6f54cc91"
        Mockito.`when`(didManagementController.deleteDid(did))

        val jsonRequestDTO = """
            {
                "did": "did:key:sdsdsdsdsdsdsdsdsdsdsdsd"
            }
        """.trimIndent()

        try {
            mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/dids/{did}", did)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequestDTO)
                    .param("userId.value", userUUID)
            )
                .andExpect(MockMvcResultMatchers.status().isInternalServerError)
                .andExpect(MockMvcResultMatchers.content().string(errorMessage))
        } catch (ex: Exception) {
            println("Error message: ${ex.message}")
            println("Root cause: ${ex.cause?.message}")
        }


    }
}

