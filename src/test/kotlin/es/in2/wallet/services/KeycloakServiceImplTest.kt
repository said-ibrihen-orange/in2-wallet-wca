//package es.in2.wallet.service.impl
//
//import junit.framework.TestCase.assertEquals
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.keycloak.admin.client.KeycloakBuilder
//import org.mockito.InjectMocks
//import org.mockito.Mock
//import org.mockito.Mockito.`when`
//import org.mockito.MockitoAnnotations
//import java.net.HttpURLConnection
//import java.net.URL
//
//internal class KeycloakServiceImplTest {
//    @Mock
//    private val keycloakBuilderMock: KeycloakBuilder? = null
//
//    @InjectMocks
//    private val keycloakService: KeycloakServiceImpl? = null
//    @BeforeEach
//    fun setUp() {
//        MockitoAnnotations.openMocks(this)
//    }
//    @Mock
//    private lateinit var urlMock: URL
//
//    @Mock
//    private lateinit var connectionMock: HttpURLConnection
//
//    @Test
//    fun testTest() {
//        // Arrange
//        val expected = "test"
//
//        // Act
//        val result = keycloakService!!.test()
//
//        // Assert
//        assertEquals(expected, result)
//    }
//
//
//    @Test
//    fun testGetKeycloakToken_NullToken() {
//
//        val responseCode = HttpURLConnection.HTTP_UNAUTHORIZED
//        val expectedErrorMessage = "Failed to obtain Keycloak token. Response Code: $responseCode"
//
//
//        `when`(urlMock.openConnection()).thenReturn(connectionMock)
//        `when`(connectionMock.responseCode).thenReturn(responseCode)
//        `when`(connectionMock.inputStream).thenReturn(null)
//
//
//
//            keycloakService.getKeycloakToken()
//
//        assertEquals(expectedErrorMessage, exception.message)
//    }
//
//
//
//}
//
//}