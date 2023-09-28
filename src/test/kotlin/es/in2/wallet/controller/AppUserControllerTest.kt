package es.in2.wallet.controller

import es.in2.wallet.api.controller.AppUserController
import es.in2.wallet.api.model.entity.AppUser
import es.in2.wallet.api.service.AppUserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.*

@SpringJUnitConfig
@SpringBootTest
@AutoConfigureMockMvc
class AppUserControllerTest {

    @Mock
    private lateinit var appUserService: AppUserService

    @InjectMocks
    private lateinit var appUserController: AppUserController

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(AppUserControllerTest::class.java)
        mockMvc = MockMvcBuilders.standaloneSetup(appUserController).build()
    }

    // Data Test

    private val uuid = UUID.randomUUID()
    private val uuid2 = UUID.randomUUID()
    private val appUser = AppUser(
        id = uuid,
        username = "jdoe",
        email = "jdoe@example.com",
        password = BCryptPasswordEncoder().encode("1234")
    )
    private val appUser2 = AppUser(
        id = uuid2,
        username = "janeDoe",
        email = "janedoe@example.com",
        password = BCryptPasswordEncoder().encode("1234")
    )

    // @Post /api/users

    @Test
    fun testRegisterUser() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"jdoe\",\"email\":\"jdoe@example.com\",\"password\":\"1234\"}")
        ).andExpect(status().isCreated)
    }

    // @Get /api/users

    @Test
    fun `getAllUsers should return a list of AppUserResponseDTO`() {
        val users = listOf(appUser, appUser2)

        given(appUserService.getUsers()).willReturn(users)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].uuid").value(appUser.id.toString()))
            .andExpect(jsonPath("$[0].username").value(appUser.username))
            .andExpect(jsonPath("$[0].email").value(appUser.email))
            .andExpect(jsonPath("$[1].uuid").value(appUser2.id.toString()))
            .andExpect(jsonPath("$[1].username").value(appUser2.username))
            .andExpect(jsonPath("$[1].email").value(appUser2.email))
    }

    // @Get /api/users/{uuid}

    @Test
    fun testGetUserByUUID() {
        given(appUserService.getUserById(uuid)).willReturn(Optional.of(appUser))
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/uuid?uuid=$uuid"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.uuid").value(uuid.toString()))
            .andExpect(jsonPath("$.username").value("jdoe"))
            .andExpect(jsonPath("$.email").value("jdoe@example.com"))
    }

    // @Get /api/users/{uuid}

    @Test
    fun testGetUserByUsername() {
        given(appUserService.getUserByUsername(appUser.username)).willReturn(Optional.of(appUser))
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/username?username=${appUser.username}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.uuid").value(uuid.toString()))
            .andExpect(jsonPath("$.username").value("jdoe"))
            .andExpect(jsonPath("$.email").value("jdoe@example.com"))
    }

}