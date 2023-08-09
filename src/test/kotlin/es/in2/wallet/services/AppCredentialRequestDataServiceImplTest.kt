package es.in2.wallet.services

import es.in2.wallet.model.AppCredentialRequestData
import es.in2.wallet.exception.CredentialRequestDataNotFoundException
import es.in2.wallet.model.AppUser
import es.in2.wallet.repository.AppCredentialRequestDataRepository
import es.in2.wallet.service.AppUserService
import es.in2.wallet.service.impl.AppCredentialRequestDataServiceImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito.*
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
    fun testSaveCredentialRequestData_CredentialRequestDataNotFoundException() {
        val issuerName = "issuer"
        val issuerNonce = "nonce"
        val issuerAccessToken = "token"
        val user = AppUser(UUID.randomUUID(), "username", "email@example.com", "password")

        `when`(appUserService.getUserWithContextAuthentication()).thenReturn(user)
        `when`(appCredentialRequestDataRepository.findAppCredentialRequestDataByIssuerNameAndUserId(issuerName, user.id.toString()))
            .thenReturn(Optional.empty())

        appCredentialRequestDataServiceImpl.saveCredentialRequestData(issuerName, issuerNonce, issuerAccessToken)

        verify(appCredentialRequestDataRepository, times(1)).save(any(AppCredentialRequestData::class.java))
    }

    @Test
    fun testSaveCredentialRequestData_ExistingData() {
        val issuerName = "issuer"
        val issuerNonce = "nonce"
        val issuerAccessToken = "token"
        val user = AppUser(UUID.randomUUID(), "username", "email@example.com", "password")
        val requestData = AppCredentialRequestData(UUID.randomUUID(), issuerName, user.id.toString(), issuerNonce, issuerAccessToken)

        `when`(appUserService.getUserWithContextAuthentication()).thenReturn(user)
        `when`(appCredentialRequestDataRepository.findAppCredentialRequestDataByIssuerNameAndUserId(issuerName, user.id.toString()))
            .thenReturn(Optional.of(requestData))

        appCredentialRequestDataServiceImpl.saveCredentialRequestData(issuerName, issuerNonce, issuerAccessToken)

        verify(appCredentialRequestDataRepository, times(1)).save(any(AppCredentialRequestData::class.java))
    }

    @Test
    fun testGetCredentialRequestDataByIssuerName_Found() {
        // Prepare test data
        val issuerName = "issuer123"
        val userId = UUID.randomUUID()
        val expectedCredentialRequestData = AppCredentialRequestData(
            id = UUID.randomUUID(),
            issuerName = issuerName,
            userId = userId.toString(),
            issuerNonce = "nonce123",
            issuerAccessToken = "token123"
        )

        // Mock behavior of appUserService.getUserWithContextAuthentication() to return a user ID
        `when`(appUserService.getUserWithContextAuthentication()).thenReturn(AppUser(id = userId, username = "user123", email = "user@example.com", password = "password123"))

        // Mock behavior of appCredentialRequestDataRepository.findAppCredentialRequestDataByIssuerNameAndUserId() to return the expected data
        `when`(appCredentialRequestDataRepository.findAppCredentialRequestDataByIssuerNameAndUserId(issuerName, userId.toString())).thenReturn(Optional.of(expectedCredentialRequestData))

        // Call the method to be tested
        val result = appCredentialRequestDataServiceImpl.getCredentialRequestDataByIssuerName(issuerName)

        // Verify that the result is the expected data
        assertEquals(expectedCredentialRequestData, result.get())
    }

    @Test
    fun testGetCredentialRequestDataByIssuerName_NotFound() {
        // Prepare test data
        val issuerName = "nonExistentIssuer"

        // Mock behavior of appUserService.getUserWithContextAuthentication() to return a user ID
        val userId = UUID.randomUUID()
        `when`(appUserService.getUserWithContextAuthentication()).thenReturn(AppUser(id = userId, username = "user123", email = "user@example.com", password = "password123"))

        // Mock behavior of appCredentialRequestDataRepository.findAppCredentialRequestDataByIssuerNameAndUserId() to return an empty Optional
        `when`(appCredentialRequestDataRepository.findAppCredentialRequestDataByIssuerNameAndUserId(issuerName, userId.toString())).thenReturn(Optional.empty())

        // Call the method to be tested and expect the exception
        assertThrows<CredentialRequestDataNotFoundException> {
            appCredentialRequestDataServiceImpl.getCredentialRequestDataByIssuerName(issuerName)
        }
    }

}