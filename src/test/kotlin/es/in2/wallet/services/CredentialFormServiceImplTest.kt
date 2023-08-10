package es.in2.wallet.services

import es.in2.wallet.model.dto.DidResponseDTO
import es.in2.wallet.service.WalletDidService
import es.in2.wallet.service.impl.CredentialFormServiceImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CredentialFormServiceImplTest {

    @Mock
    private lateinit var walletDidService: WalletDidService

    private lateinit var credentialFormService: CredentialFormServiceImpl

    @BeforeEach
    fun setUp() {
        credentialFormService = CredentialFormServiceImpl(walletDidService)

    }

    @Test
    fun testGetCredentialForm() {
        // Arrange
        val expectedProofTypeList = listOf("jwt")
        val expectedDidList = listOf(
            DidResponseDTO("did:example1"),
            DidResponseDTO("did:example2")
        )

        `when`(walletDidService.getDidsByUserId()).thenReturn(expectedDidList)

        // Act
        val result = credentialFormService.getCredentialForm()

        // Assert
        assertEquals(expectedProofTypeList, result.proofTypeList)
        assertEquals(expectedDidList, result.didList)
    }
}