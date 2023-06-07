package es.in2.wallet.service.impl

import es.in2.wallet.exception.NoSuchQrContentException
import es.in2.wallet.model.QrType
import es.in2.wallet.service.PersonalDataSpaceService
import es.in2.wallet.service.SiopService
import es.in2.wallet.service.VerifiableCredentialService
import es.in2.wallet.service.QrCodeProcessorService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import java.util.*

@Service
class QrCodeProcessorServiceImpl(
    private val siopService: SiopService,
    private val verifiableCredentialService: VerifiableCredentialService,
    private val personalDataSpaceService: PersonalDataSpaceService,
) : QrCodeProcessorService {

    private val log: Logger = LogManager.getLogger(QrCodeProcessorServiceImpl::class.java)

    override fun processQrContent(userUUID: UUID, qrContent: String): Any {
        return when (identifyQrContentType(qrContent)) {
            QrType.SIOP_AUTH_REQUEST_URI -> siopService.getSiopAuthenticationRequest(userUUID, qrContent)
            QrType.SIOP_AUTH_REQUEST -> siopService.processSiopAuthenticationRequest(userUUID, qrContent)
            QrType.CREDENTIAL_OFFER_URI -> verifiableCredentialService.getVerifiableCredential(userUUID, qrContent)
            QrType.VC_JWT -> personalDataSpaceService.saveVC(userUUID, qrContent)
            QrType.UNKNOWN -> throw NoSuchQrContentException("The received QR content cannot be processed")
        }
    }

    private fun identifyQrContentType(content: String): QrType {
        val loginRequestUrlRegex = Regex("(https|http).*?(authentication-request|authentication-requests).*")
        val siopAuthenticationRequestRegex = Regex("openid://.*")
        val credentialOfferUriRegex = Regex("(https|http).*?(credential-offer|credential-offers).*")
        val verifiableCredentialInVcJwtFormatRegex = Regex("ey.*")
        return when {
            loginRequestUrlRegex.matches(content) -> QrType.SIOP_AUTH_REQUEST_URI
            siopAuthenticationRequestRegex.matches(content) -> QrType.SIOP_AUTH_REQUEST
            credentialOfferUriRegex.matches(content) -> QrType.CREDENTIAL_OFFER_URI
            verifiableCredentialInVcJwtFormatRegex.matches(content) -> QrType.VC_JWT
            else -> { QrType.UNKNOWN }
        }
    }

}