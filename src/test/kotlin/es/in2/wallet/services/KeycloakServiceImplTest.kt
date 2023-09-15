package es.in2.wallet.services

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
        return keycloakService.getKeycloakToken()
    }

    @Test
    fun testCreateUser() {
        val userData = KeycloakUserDTO(
            username = "test",
            firstName = "test-firstname",
            lastName = "test-lastname",
            email = "test-email@test.test"
        )
        val token = getToken()
        keycloakService.createUserInKeycloak(token = token, userData = userData)
    }

    @Test
    fun testGetUsers(){
        val token = getToken()
        keycloakService.getKeycloakUsers(token = token)
    }



}