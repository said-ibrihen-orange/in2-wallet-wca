package es.in2.wallet.configuration

import es.in2.wallet.util.SERVICE_MATRIX
import es.in2.wallet.waltid.CustomDidService
import id.walt.crypto.KeyAlgorithm
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.did.DidService
import id.walt.services.key.KeyService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Configuration class for Waltid that provides bean definitions and initialization logic.
 */
@Configuration
class WalletDidConfig {

    private val log: Logger = LogManager.getLogger(CustomDidService::class.java)

    /**
     * Defines a bean for DidKeyGenerator.
     *
     * @return The initialized DidKeyGenerator bean.
     */
    @Bean
    fun didKeyGenerator(): WalletDidKeyGenerator {
        val keyId: String = generateKey()
        val didKey = generateDidKey(keyId)
        return WalletDidKeyGenerator(didKey)
    }

    /**
     * Generates a key using KeyService and logs the key ID.
     *
     * @return The generated key ID.
     */
    fun generateKey(): String {
        log.info("Generating key...")
        ServiceMatrix(filePath = SERVICE_MATRIX)
        val keyId = KeyService.getService().generate(KeyAlgorithm.ECDSA_Secp256r1)
        log.info("Generated key. KeyId = {}", keyId)
        return keyId.id
    }

    /**
     * Generates a DID key using DidService and logs the DID.
     *
     * @param keyId The ID of the key used for DID generation.
     * @return The generated DID key.
     */
    fun generateDidKey(keyId: String): String {
        log.info("Generating DID key...")
        ServiceMatrix(filePath = SERVICE_MATRIX)
        val didKey = DidService.create(DidMethod.key, keyId)
        log.info("Generated DID key. DID = {}", didKey)
        return didKey
    }


}
