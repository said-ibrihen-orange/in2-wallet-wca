package es.in2.wallet.services

import es.in2.wallet.api.util.KEYCLOAK_ADMIN_USERNAME
import es.in2.wallet.integration.keycloak.model.dto.KeycloakUserDTO
import es.in2.wallet.integration.keycloak.service.impl.KeycloakServiceImpl
import org.junit.jupiter.api.*
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.springframework.boot.test.context.SpringBootTest
import org.mockito.kotlin.doReturn
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("local")
class KeycloakServiceImplTest {
    @Spy
    private val keycloakService = KeycloakServiceImpl()

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testTest() {
        val expected = "test"
        val result = keycloakService.test()
        Assertions.assertEquals(expected, result)
    }

    @Test
    fun testGetToken() {
        val token = getToken()
        Assertions.assertTrue(token.length > 512)
    }

    private fun getToken(): String {
        doReturn("http://localhost:8090").`when`(keycloakService).getKeycloakUrl() // partially mocking
        doReturn("vxuLBYhEJ0atp1AZPjwKh5hzaZMOqd5y").`when`(keycloakService).getKeycloakClientSecret() // partially mocking
        doReturn("password").`when`(keycloakService).getKeycloakAdminPassword() // partially mocking
        return keycloakService.getKeycloakToken()
    }

    @Test
    fun testCreateUser() {
        val userData = KeycloakUserDTO(
            username = "test",
            firstName = "test-firstname",
            lastName = "test-lastname",
            email = "test-email@test.test",
            id = null
        )
        val token = getToken()
        keycloakService.createUserInKeycloak(token = token, userData = userData)
        keycloakService.deleteKeycloakUser(token = token, username = userData.username)
    }

    @Test
    fun testGetUsers(){
        val token = getToken()
        val users = keycloakService.getKeycloakUsers(token = token)
        val usernames = mutableSetOf<String>()
        users.forEach{ user ->
            usernames.add(user.username)
        }
        usernames.contains(KEYCLOAK_ADMIN_USERNAME)
    }

    @Test
    fun testGetUser(){
        val token = getToken()
        val user = keycloakService.getKeycloakUser(token = token, username = KEYCLOAK_ADMIN_USERNAME)
        Assertions.assertTrue(user != null)
        Assertions.assertTrue(user?.username == KEYCLOAK_ADMIN_USERNAME)
        val user1 = user?.id?.let { keycloakService.getKeycloakUserById(token = token, id = it) }
        Assertions.assertTrue(user == user1)
    }
    @Test
    fun testCreateGetDeleteUser(){
        val token = getToken()
        val userDTO = KeycloakUserDTO(
            username = "deleteme",
            firstName = "deleteme",
            lastName = "deleteme",
            email = "delete-me@test.test",
            id = null,
        )
        keycloakService.createUserInKeycloak(token = token, userData = userDTO)
        val user = keycloakService.getKeycloakUser(token = token, username = userDTO.username)
        keycloakService.deleteKeycloakUser(token = token, username = userDTO.username)
        val user1 = keycloakService.getKeycloakUser(token = token, username = userDTO.username)
        Assertions.assertNull(user1)
    }



}