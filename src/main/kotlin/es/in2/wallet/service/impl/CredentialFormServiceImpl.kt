package es.in2.wallet.service.impl

import es.in2.wallet.exception.DIDNotFoundException
import es.in2.wallet.model.dto.CredentialFormResponseDTO
import es.in2.wallet.service.CredentialFormService
import es.in2.wallet.service.WalletDidService
import org.springframework.stereotype.Service
import org.slf4j.Logger
import org.slf4j.LoggerFactory
@Service
class CredentialFormServiceImpl(
        private val walletDidService: WalletDidService,
) : CredentialFormService {

    private val log: Logger = LoggerFactory.getLogger(AppCredentialRequestDataServiceImpl::class.java)

    private val proofTypeList = listOf(
            "jwt"
    )
    override fun getCredentialForm(): CredentialFormResponseDTO {
        val didList = walletDidService.getDidsByUserId()
        if (didList.isEmpty()){
            throw DIDNotFoundException("No DIDs available.")
        }
        log.debug("List of Did = {}", didList)
        return CredentialFormResponseDTO(proofTypeList,didList)
    }

}