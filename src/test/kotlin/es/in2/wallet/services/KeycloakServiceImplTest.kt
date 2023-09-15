package es.in2.wallet.service.impl

import es.in2.wallet.exception.FailedCommunicationException
import es.in2.wallet.util.ApplicationUtils
import io.ktor.util.reflect.*
import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.*
import org.keycloak.admin.client.KeycloakBuilder
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.springframework.boot.test.context.SpringBootTest
import java.net.HttpURLConnection
import org.mockito.kotlin.any
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
        return keycloakService.getKeycloakToken()
    }

    @Test
    fun testCreateUser() {
        val userData = mapOf(
            "username" to "test",
            "firstName" to "test-firstname",
            "lastName" to "test-lastname",
            "email" to "test-email@test.test"
        )
        val token = getToken()
        keycloakService.createUserInKeycloak(token=token, userData=userData)
    }

    @Test
    fun testGetUsers(){
        val token = getToken()
        keycloakService.getKeycloakUsers(token = token)
    }

}