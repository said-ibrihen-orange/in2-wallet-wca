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
    fun testCreateUser() {
        //`when`(keycloakService.getKeycloakUrl()).doReturn("http://localhost:8080") //This fails because when calling getKeycloakUrl it raises error...
        doReturn("http://localhost:8080").`when`(keycloakService).getKeycloakUrl() // partially mocking
        val token = keycloakService.getKeycloakToken()
        Assertions.assertTrue(false)
    }

    @Test
    fun testOldCreateUser() {
        keycloakService.getKeycloakToken1()
        Assertions.assertTrue(false)
    }

}