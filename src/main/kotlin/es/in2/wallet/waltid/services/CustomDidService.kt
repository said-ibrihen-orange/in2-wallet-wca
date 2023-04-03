package es.in2.wallet.waltid.services

import es.in2.wallet.SERVICE_MATRIX
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.did.DidService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service

interface CustomDidService {
    fun generateDidKey(): String
    fun generateDidKeyWithKid(kid: String): String
}

@Service
class CustomDidServiceImpl(
    private val customKeyService: CustomKeyService
) : CustomDidService {

    private val log: Logger = LogManager.getLogger(CustomDidService::class.java)

    override fun generateDidKey(): String {
        log.info("DID Service - Generate DID Key")
        ServiceMatrix(filePath = SERVICE_MATRIX)
        return DidService.create(DidMethod.key, customKeyService.generateKey().id)
    }

    override fun generateDidKeyWithKid(kid: String): String {
        log.info("DID Service - Generate DID Key by KID")
        ServiceMatrix(filePath = SERVICE_MATRIX)
        return DidService.create(DidMethod.key, kid)
    }

}