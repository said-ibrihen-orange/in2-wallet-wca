package es.in2.wallet.domain

import es.in2.wallet.model.AppCredentialRequestData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
class AppCredentialRequestDataTest {

    @Test
    fun testAppCredentialRequestDataProperties() {
        // Create test data
        val issuerName = "issuer123"
        val userId: UUID = UUID.randomUUID()
        val issuerNonce = "nonce123"
        val issuerAccessToken = "accessToken123"

        // Create an instance of the AppIssuerData entity
        val appCredentialRequestData = AppCredentialRequestData(
                issuerName = issuerName,
                userId = userId,
                issuerNonce = issuerNonce,
                issuerAccessToken = issuerAccessToken
        )

        // Verify the properties
        Assertions.assertEquals(issuerName, appCredentialRequestData.issuerName)
        Assertions.assertEquals(userId, appCredentialRequestData.userId)
        Assertions.assertEquals(issuerNonce, appCredentialRequestData.issuerNonce)
        Assertions.assertEquals(issuerAccessToken, appCredentialRequestData.issuerAccessToken)
    }
}