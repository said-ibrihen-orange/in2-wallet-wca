package es.in2.wallet.service.impl

import es.in2.wallet.exception.FailedCommunicationException
import es.in2.wallet.util.ApplicationUtils
import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.keycloak.admin.client.KeycloakBuilder
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.boot.test.context.SpringBootTest
import java.net.HttpURLConnection
import org.mockito.kotlin.any

@SpringBootTest
class KeycloakServiceImplTest {
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

}