package es.in2.wallet.service.impl

import com.nimbusds.jwt.SignedJWT
import es.in2.wallet.service.PersonalDataSpaceService
import es.in2.wallet.service.SiopService
import es.in2.wallet.service.VerifiablePresentationService
import es.in2.wallet.util.JWT
import es.in2.wallet.waltid.CustomDidService
import id.walt.custodian.Custodian
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.json.JSONObject
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class VerifiablePresentationServiceImpl(
    private val customDidService: CustomDidService,
    private val personalDataSpaceService: PersonalDataSpaceService,
    private val siopService: SiopService
) : VerifiablePresentationService {

    private val log: Logger = LogManager.getLogger(VerifiablePresentationServiceImpl::class.java)

    override fun createVerifiablePresentation(verifiableCredentials: List<String>, format: String): String {
        val verifiableCredential = verifiableCredentials[0]
        log.info("VerifiableCredential: $verifiableCredential")
        val parsedVerifiableCredential = SignedJWT.parse(verifiableCredential)
        val payloadToJson = parsedVerifiableCredential.payload.toJSONObject()
        val subjectDid = payloadToJson["sub"]
        log.info("Subject Did: $subjectDid")
        /*
            The holder DID MUST be received by the Wallet implementation, and it MUST match with the
            subject_id of, at least, one of the VCs attached.
            That VP MUST be signed using the PrivateKey related with the holderDID.
         */
        val holderDid = customDidService.generateDidKey()
        /*
            The holder SHOULD be able to modify the attribute 'expiration_date' by any of its
            Verifiable Presentation.
        */
        val secondsToAdd: Long = 600
        return Custodian.getService().createPresentation(
            vcs = verifiableCredentials,
            holderDid = holderDid,
            expirationDate = Instant.now().plusSeconds(secondsToAdd)
        )
    }

    override fun executeVP(vps: List<String>, siopAuthenticationRequest: String): String {
        log.info("building Verifiable Presentation")
        val verifiableCredentials = ArrayList<String>()
        for (vp in vps) {
            val tmp = personalDataSpaceService.getVcByFormat(vp, "vc_jwt")
            val vc = JSONObject(tmp)
            val token = vc.getJSONObject("vc").getString("value")
            verifiableCredentials.add(token)
        }
        val vp = createVerifiablePresentation(verifiableCredentials, JWT)
        log.info("executing the post Authentication Response ")
        // send the verifiable presentation to the dome backend
        return siopService.sendAuthenticationResponse(siopAuthenticationRequest, vp)
    }

}
