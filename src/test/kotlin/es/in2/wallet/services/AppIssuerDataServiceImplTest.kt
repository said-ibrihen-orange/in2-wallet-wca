package es.in2.wallet.services

import es.in2.wallet.model.AppIssuerData
import es.in2.wallet.repository.AppIssuerDataRepository
import es.in2.wallet.service.impl.AppIssuerDataServiceImpl
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.*
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
    fun testSaveIssuerData_Success() {
        // Prepare test data
        val issuerName = "issuer123"
        val issuerMetadata = """{"key": "value"}"""

        // Mock behavior of the repository method
        `when`(appIssuerDataRepository.findAppIssuerDataByName(issuerName)).thenReturn(Optional.empty())

        // Call the method to be tested
        appIssuerDataServiceImpl.upsertIssuerData(issuerName, issuerMetadata)

        // Verify that the repository method was called
        verify(appIssuerDataRepository).findAppIssuerDataByName(issuerName)
        verify(appIssuerDataRepository).save(any(AppIssuerData::class.java))
    }

    @Test
    fun testSaveIssuerData_IssuerNameAlreadyExists() {
        // Prepare test data
        val issuerName = "issuer123"
        val issuerMetadata = """{"key": "value"}"""

        val appIssuerData = AppIssuerData(id = UUID.randomUUID(), name = issuerName, metadata = issuerMetadata)
        // Mock behavior of the repository method
        `when`(appIssuerDataRepository.findAppIssuerDataByName(issuerName))
            .thenReturn(Optional.of(appIssuerData))
        `when`(appIssuerDataRepository.save(appIssuerData)).thenReturn(null)

        // Call the method to be tested
        appIssuerDataServiceImpl.upsertIssuerData(issuerName, issuerMetadata)

        // Verify that the repository method was called
        verify(appIssuerDataRepository).findAppIssuerDataByName(issuerName)
        verify(appIssuerDataRepository).save(appIssuerData)
        verifyNoMoreInteractions(appIssuerDataRepository)
    }

    @Test
    fun testGetIssuerDataByIssuerName_Found() {
        // Prepare test data
        val issuerName = "issuer123"
        val issuerMetadata = """{"key": "value"}"""
        val appIssuerData = AppIssuerData(id = UUID.randomUUID(), name = issuerName, metadata = issuerMetadata)

        // Mock behavior of the repository method
        `when`(appIssuerDataRepository.findAppIssuerDataByName(issuerName)).thenReturn(Optional.of(appIssuerData))

        // Call the method to be tested
        val result = appIssuerDataServiceImpl.getIssuerDataByIssuerName(issuerName)

        // Verify the result
        assertTrue(result.isPresent)
        assertEquals(appIssuerData, result.get())
    }

    @Test
    fun testGetIssuerDataByIssuerName_NotFound() {
        // Prepare test data
        val issuerName = "issuer123"

        // Mock behavior of the repository method
        `when`(appIssuerDataRepository.findAppIssuerDataByName(issuerName)).thenReturn(Optional.empty())

        // Call the method to be tested
        val result = appIssuerDataServiceImpl.getIssuerDataByIssuerName(issuerName)

        // Verify the result
        assertFalse(result.isPresent)
    }
    @Test
    fun testGetIssuers() {
        val issuer1 = AppIssuerData(id = UUID.randomUUID(), name = "Issuer1", metadata = "Metadata1")
        val issuer2 = AppIssuerData(id = UUID.randomUUID(), name = "Issuer2", metadata = "Metadata2")
        val issuers = listOf(issuer1, issuer2)

        `when`(appIssuerDataRepository.findAll()).thenReturn(issuers)

        val result = appIssuerDataServiceImpl.getIssuers()

        assertEquals(2, result.size)
        assertEquals("Issuer1", result[0])
        assertEquals("Issuer2", result[1])
    }
}