package es.in2.wallet.service.impl

import es.in2.wallet.exception.InvalidDIDFormatException
import es.in2.wallet.model.DidMethods
import es.in2.wallet.model.dto.DidRequestDTO
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

    override fun createDid(didRequestDTO: DidRequestDTO): String {
        log.debug("DID Service - Create DID")
        val didType = didRequestDTO.type
        val didValue = didRequestDTO.value

        if (didType == "key" && didValue != null) {
            throw InvalidDIDFormatException("Value must be null for 'key' type DID.")
        }


        return when (didType) {
            "key" -> {
                createDidKey()
            }

            "elsi" -> {
                createDidElsi(didValue.toString())
            }

            else -> throw InvalidDIDFormatException("Invalid DID format")
        }
    }



    private fun createDidElsi(elsi: String): String {
        log.debug("DID Service - Create DID ELSI")
        personalDataSpaceService.saveDid(elsi, DidMethods.DID_ELSI)
        log.debug("DID ELSI = {}", elsi)
        return elsi
    }

    private fun createDidKey(): String {
        log.debug("DID Service - Create DID Key")
        val did = generateDidKey()
        log.debug("DID Key = {}", did)
        personalDataSpaceService.saveDid(did, DidMethods.DID_KEY)
        return did
    }

    override fun getDidsByUserId(): List<String> {
        return personalDataSpaceService.getDidsByUserId()
    }

    override fun deleteDid(did: String){
        if(!did.startsWith("did:key:") && !did.startsWith("did:elsi:")){
            throw InvalidDIDFormatException("Value DID has an invalid format.")
        }
        personalDataSpaceService.deleteSelectedDid(did)
        log.debug("Deleted $did")
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