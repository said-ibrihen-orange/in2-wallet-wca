package es.in2.wallet.services

import es.in2.wallet.api.util.KEYCLOAK_ADMIN_USERNAME
import es.in2.wallet.integration.keycloak.model.dto.KeycloakUserDTO
import es.in2.wallet.integration.keycloak.service.impl.KeycloakServiceImpl
import okhttp3.internal.userAgent
import org.junit.jupiter.api.*
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.springframework.boot.test.context.SpringBootTest
import org.mockito.kotlin.doReturn
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("local") // To enable DEBUG logs in tests
class KeycloakServiceImplTest {
    @Spy
    private val keycloakService = KeycloakServiceImpl()

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
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
        Assertions.assertTrue(usernames.contains(KEYCLOAK_ADMIN_USERNAME))
    }

    @Test
    fun testGetUser(){
        val token = getToken()
        val user = keycloakService.getKeycloakUser(token = token, username = KEYCLOAK_ADMIN_USERNAME)
        Assertions.assertTrue(user.username == KEYCLOAK_ADMIN_USERNAME)
        val user1 = keycloakService.getKeycloakUserById(token = token, id = user.id!!)
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
        val exception = Assertions.assertThrows(Exception::class.java) {
            keycloakService.getKeycloakUser(token = token, username = userDTO.username)
        }

        val expectedErrorMessage = "User ${userDTO.username} not found"
        val actualMessage = exception.message
        Assertions.assertEquals(expectedErrorMessage, actualMessage)

    }

    @Test
    fun testCreateGetDeleteUserById(){
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
        keycloakService.deleteKeycloakUserById(token = token, id = user.id!!)
        val exception = Assertions.assertThrows(Exception::class.java) {
            keycloakService.getKeycloakUserById(token = token, id = user.id!!)
        }

        val expectedErrorMessage = "HTTP 404 Not Found"
        val actualMessage = exception.message
        Assertions.assertEquals(expectedErrorMessage, actualMessage)

    }

    @Test
    fun testUpdateUser() {
        val token = getToken()
        val userDTO = KeycloakUserDTO(
            username = "changeme",
            firstName = "changeme",
            lastName = "changeme",
            email = "changeme@changeme.test",
            id = null
        )
        keycloakService.createUserInKeycloak(token = token, userData = userDTO)
        val user = keycloakService.getKeycloakUser(token = token, username = userDTO.username)
        val newUserDTO = KeycloakUserDTO(
            username = "newname",
            firstName = "newFirstName",
            lastName = "newLastName",
            email = "new-email@test.test",
            id = null
        )
        keycloakService.updateUser(token = token, username = userDTO.username, userData = newUserDTO)
        val user1 = keycloakService.getKeycloakUserById(token = token, id = user.id!!)
        keycloakService.deleteKeycloakUser(token = token, username = userDTO.username)
        Assertions.assertEquals(newUserDTO.username, user1.username)
        Assertions.assertEquals(newUserDTO.firstName, user1.firstName)
        Assertions.assertEquals(newUserDTO.lastName, user1.lastName)
        Assertions.assertEquals(newUserDTO.email, user1.email)
    }
}