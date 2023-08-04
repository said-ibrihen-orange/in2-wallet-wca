package es.in2.wallet.domain

import es.in2.wallet.model.AppIssuerData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class AppIssuerDataTest {

    @Test
    fun testAppIssuerDataProperties() {
        // Create test data
        val issuerName = "issuer123"
        val issuerMetadata = """{"key": "value"}"""

        // Create an instance of the AppIssuerData entity
        val appIssuerData = AppIssuerData(
                issuerName = issuerName,
                issuerMetadata = issuerMetadata
        )

        // Verify the properties
        Assertions.assertEquals(issuerName, appIssuerData.issuerName)
        Assertions.assertEquals(issuerMetadata, appIssuerData.issuerMetadata)

    }
}