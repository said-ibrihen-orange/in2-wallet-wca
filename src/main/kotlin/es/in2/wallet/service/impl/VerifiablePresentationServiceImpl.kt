package es.in2.wallet.service.impl

import com.nimbusds.jwt.SignedJWT
import es.in2.wallet.model.dto.VcSelectorResponseDTO
import es.in2.wallet.service.PersonalDataSpaceService
import es.in2.wallet.service.SiopService
import es.in2.wallet.service.VerifiablePresentationService
import es.in2.wallet.util.VC_JWT
import es.in2.wallet.service.WalletDidService
import id.walt.custodian.Custodian
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class VerifiablePresentationServiceImpl(
    private val walletDidService: WalletDidService,
    private val personalDataSpaceService: PersonalDataSpaceService,
    private val siopService: SiopService
) : VerifiablePresentationService {

    private val log: Logger = LogManager.getLogger(VerifiablePresentationServiceImpl::class.java)

    override fun createVerifiablePresentation(vcSelectorResponseDTO: VcSelectorResponseDTO): String {

        // Get vc_jwt list from the selected list of VCs received
        val verifiableCredentialsList = mutableListOf<String>()
        vcSelectorResponseDTO.selectedVcList.forEach {
            verifiableCredentialsList.add(personalDataSpaceService.getVerifiableCredentialByIdAndFormat(it.id, VC_JWT))
        }

        // Get Subject DID
        val subjectDid = getSubjectDidFromTheFirstVcOfTheList(verifiableCredentialsList)
        log.info("subject DID = {}", subjectDid)

        /*
            The holder DID MUST be received by the Wallet implementation, and it MUST match with the
            subject_id of, at least, one of the VCs attached.
            That VP MUST be signed using the PrivateKey related with the holderDID.
         */
        val holderDid = walletDidService.generateDidKey()

        /*
            The holder SHOULD be able to modify the attribute 'expiration_date' by any of its
            Verifiable Presentation.
        */
        val secondsToAdd: Long = 60000

        return Custodian.getService().createPresentation(
            vcs = verifiableCredentialsList,
            holderDid = holderDid,
            expirationDate = Instant.now().plusSeconds(secondsToAdd)
        )
    }

    private fun getSubjectDidFromTheFirstVcOfTheList(verifiableCredentialsList: MutableList<String>): String {
        val verifiableCredential = verifiableCredentialsList[0]
        val parsedVerifiableCredential = SignedJWT.parse(verifiableCredential)
        val payloadToJson = parsedVerifiableCredential.payload.toJSONObject()
        return payloadToJson["sub"].toString()
    }

}
