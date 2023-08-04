package es.in2.wallet.services

import es.in2.wallet.model.AppIssuerData
import es.in2.wallet.repository.AppIssuerDataRepository
import es.in2.wallet.service.impl.AppIssuerDataServiceImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.argThat
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
class AppIssuerDataServiceImplTest {

    @Mock
    private lateinit var appIssuerDataRepository: AppIssuerDataRepository

    private lateinit var appIssuerDataServiceImpl: AppIssuerDataServiceImpl

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        appIssuerDataServiceImpl = AppIssuerDataServiceImpl(appIssuerDataRepository)
    }

    @Test
    fun testSaveIssuerData() {
        // Prepare test data
        val issuerName = "issuer123"
        val issuerMetadata = """{"key": "value"}"""

        // Create an instance of the AppIssuerData entity to be saved
        val appIssuerDataToSave = AppIssuerData(
                issuerName = issuerName,
                issuerMetadata = issuerMetadata
        )

        // Set up mock behavior for the appIssuerDataRepository.findAppIssuerDataByIssuerName() method
        `when`(appIssuerDataRepository.findAppIssuerDataByIssuerName(issuerName)).thenReturn(Optional.empty())

        // Set up mock behavior for the appIssuerDataRepository.save() method
        `when`(appIssuerDataRepository.save(any(AppIssuerData::class.java))).thenReturn(appIssuerDataToSave)

        // Call the method to be tested
        appIssuerDataServiceImpl.saveIssuerData(issuerName, issuerMetadata)

        // Verify that the appIssuerDataRepository.save() method was called with the correct data
        verify(appIssuerDataRepository).save(argThat {
            it.issuerName == issuerName && it.issuerMetadata == issuerMetadata
        })
    }
    @Test
    fun testGetIssuerDataByIssuerName() {
        // Prepare test data
        val issuerName = "issuer123"
        val issuerMetadata = """{"key": "value"}"""

        // Set up mock behavior for the appIssuerDataRepository.findAppIssuerDataByIssuerName() method
        val appIssuerData = AppIssuerData(
                issuerName = issuerName,
                issuerMetadata = issuerMetadata
        )
        `when`(appIssuerDataRepository.findAppIssuerDataByIssuerName(issuerName)).thenReturn(Optional.of(appIssuerData))

        // Call the method to be tested
        val result = appIssuerDataServiceImpl.getIssuerDataByIssuerName(issuerName)

        // Verify the result
        assertTrue(result.isPresent)
        assertEquals(appIssuerData, result.get())

        // Verify that the appIssuerDataRepository.findAppIssuerDataByIssuerName() method was called with the correct issuerName
        verify(appIssuerDataRepository).findAppIssuerDataByIssuerName(issuerName)
    }
}