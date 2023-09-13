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
        val id = UUID.randomUUID()
        val issuerName = "issuer123"
        val userId = UUID.randomUUID().toString()
        val issuerNonce = "nonce123"
        val issuerAccessToken = "accessToken123"

        // Create an instance of the AppIssuerData entity
        val appCredentialRequestData = AppCredentialRequestData(
                id = id,
                issuerName = issuerName,
                userId = userId,
                issuerNonce = issuerNonce,
                issuerAccessToken = issuerAccessToken
        )

        // Verify the properties
        Assertions.assertEquals(id, appCredentialRequestData.id)
        Assertions.assertEquals(issuerName, appCredentialRequestData.issuerName)
        Assertions.assertEquals(userId, appCredentialRequestData.userId)
        Assertions.assertEquals(issuerNonce, appCredentialRequestData.issuerNonce)
        Assertions.assertEquals(issuerAccessToken, appCredentialRequestData.issuerAccessToken)
    }
}