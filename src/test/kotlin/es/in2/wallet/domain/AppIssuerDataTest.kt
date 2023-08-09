package es.in2.wallet.domain

import es.in2.wallet.model.AppIssuerData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
class AppIssuerDataTest {

    @Test
    fun testAppIssuerDataProperties() {
        // Create test data
        val id: UUID = UUID.randomUUID()
        val issuerName = "issuer123"
        val issuerMetadata = """{"key": "value"}"""

        // Create an instance of the AppIssuerData entity
        val appIssuerData = AppIssuerData(
            id = id,
            name = issuerName,
            metadata = issuerMetadata
        )

        // Verify the properties
        Assertions.assertEquals(id, appIssuerData.id)
        Assertions.assertEquals(issuerName, appIssuerData.name)
        Assertions.assertEquals(issuerMetadata, appIssuerData.metadata)

    }
}