package es.in2.wallet.service.impl

import es.in2.wallet.model.DidMethods
import es.in2.wallet.service.PersonalDataSpaceService
import es.in2.wallet.util.SERVICE_MATRIX
import es.in2.wallet.service.WalletDidService
import es.in2.wallet.service.WalletKeyService
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.did.DidService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service

@Service
class WalletDidServiceImpl(
    private val walletKeyService: WalletKeyService,
    private val personalDataSpaceService: PersonalDataSpaceService
) : WalletDidService {

    private val log: Logger = LogManager.getLogger(WalletDidService::class.java)

    override fun createDidKey(): String {
        log.info("DID Service - Create DID Key")
        val did = generateDidKey()
        log.info("DID Key = {}", did)
        personalDataSpaceService.saveDid(did, DidMethods.DID_KEY)
        return did
    }

    override fun createDidElsi(elsi: String): String {
        log.info("DID Service - Create DID ELSI")
        log.info("DID ELSI = {}", elsi)
        personalDataSpaceService.saveDid(elsi, DidMethods.DID_ELSI)
        return elsi
    }

    override fun generateDidKey(): String {
        log.info("DID Service - Generate DID Key")
        ServiceMatrix(filePath = SERVICE_MATRIX)
        val keyId = walletKeyService.generateKey().id
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