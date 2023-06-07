package es.in2.wallet.controller

import es.in2.wallet.model.AppUser
import es.in2.wallet.service.AppUserService
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

    private val uuid = UUID.randomUUID()

    private val appUser = AppUser(
        id = uuid,
        username = "jdoe",
        email = "jdoe@example.com",
        password = BCryptPasswordEncoder().encode("1234")
    )

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(AppUserControllerTest::class.java)
        mockMvc = MockMvcBuilders.standaloneSetup(appUserController).build()
    }

    @Test
    fun testRegisterUser() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"jdoe\",\"email\":\"jdoe@example.com\",\"password\":\"1234\"}")
        ).andExpect(status().isCreated)
    }

    @Test
    fun testGetUserByUUID() {
        given(appUserService.getUserById(uuid)).willReturn(Optional.of(appUser))
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/users/$uuid"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(uuid.toString()))
            .andExpect(jsonPath("$.username").value("jdoe"))
            .andExpect(jsonPath("$.email").value("jdoe@example.com"))
    }

}