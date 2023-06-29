package es.in2.wallet.waltid.impl

import es.in2.wallet.util.SERVICE_MATRIX
import es.in2.wallet.waltid.CustomDidService
import es.in2.wallet.waltid.CustomKeyService
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.did.DidService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service

@Service
class CustomDidServiceImpl(
    private val customKeyService: CustomKeyService
) : CustomDidService {

    private val log: Logger = LogManager.getLogger(CustomDidService::class.java)

    override fun generateDidKey(): String {
        log.info("DID Service - Generate DID Key")
        ServiceMatrix(filePath = SERVICE_MATRIX)
        val keyId = customKeyService.generateKey().id
        val did = DidService.create(DidMethod.key, keyId)
        log.info("DID Key = {}", did)
        return did
    }

    override fun generateDidKeyWithKid(kid: String): String {
        log.info("DID Service - Generate DID Key by KID")
        ServiceMatrix(filePath = SERVICE_MATRIX)
        return DidService.create(DidMethod.key, kid)
    }

}