package es.in2.wallet.services

import es.in2.wallet.model.AppUser
import es.in2.wallet.repository.AppCredentialRequestDataRepository
import es.in2.wallet.service.AppUserService
import es.in2.wallet.service.impl.AppCredentialRequestDataServiceImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.argThat
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
class AppCredentialRequestDataServiceImplTest {

    @Mock
    private lateinit var appCredentialRequestDataRepository: AppCredentialRequestDataRepository

    @Mock
    private lateinit var appUserService: AppUserService

    private lateinit var appCredentialRequestDataServiceImpl: AppCredentialRequestDataServiceImpl

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        appCredentialRequestDataServiceImpl = AppCredentialRequestDataServiceImpl(
                appCredentialRequestDataRepository,
                appUserService
        )
    }

    @Test
    fun testSaveCredentialRequestData() {
        // Prepare test data
        val issuerName = "issuer123"
        val issuerNonce = "nonce123"
        val issuerAccessToken = "accessToken123"

        // Mock the behavior of the appUserService.getUserWithContextAuthentication() method to return a user ID
        val userId = UUID.randomUUID()
        `when`(appUserService.getUserWithContextAuthentication()).thenReturn(AppUser(id = userId, username = "user123", email = "user@example.com", password = "password123"))

        // Call the method to be tested
        appCredentialRequestDataServiceImpl.saveCredentialRequestData(issuerName, issuerNonce, issuerAccessToken)

        // Verify that the appUserService.getUserWithContextAuthentication() method was called
        verify(appUserService).getUserWithContextAuthentication()

        // Verify that the appCredentialRequestDataRepository.save() method was called with the correct data
        verify(appCredentialRequestDataRepository).save(argThat {
            it.issuerName == issuerName &&
                    it.userId == userId &&
                    it.issuerNonce == issuerNonce &&
                    it.issuerAccessToken == issuerAccessToken
        })
    }
    @Test
    fun testGetCredentialRequestDataByIssuerName() {
        // Prepare test data
        val issuerName = "issuer123"

        // Mock the behavior of the appUserService.getUserWithContextAuthentication() method to return a user ID
        val userId = UUID.randomUUID()
        `when`(appUserService.getUserWithContextAuthentication()).thenReturn(AppUser(id = userId, username = "user123", email = "user@example.com", password = "password123"))

        // Call the method to be tested
        appCredentialRequestDataServiceImpl.getCredentialRequestDataByIssuerName(issuerName)

        // Verify that the appUserService.getUserWithContextAuthentication() method was called
        verify(appUserService).getUserWithContextAuthentication()

        // Verify that the appCredentialRequestDataRepository.findAppCredentialRequestDataByIssuerNameAndUserId() method was called with the correct data
        verify(appCredentialRequestDataRepository).findAppCredentialRequestDataByIssuerNameAndUserId(issuerName, userId)
    }
}