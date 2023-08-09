package es.in2.wallet.service.impl

import es.in2.wallet.exception.CredentialRequestDataNotFoundException
import es.in2.wallet.model.AppCredentialRequestData
import es.in2.wallet.repository.AppCredentialRequestDataRepository
import es.in2.wallet.service.AppCredentialRequestDataService
import es.in2.wallet.service.AppUserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class AppCredentialRequestDataServiceImpl(
        private val appCredentialRequestDataRepository: AppCredentialRequestDataRepository,
        private val appUserService: AppUserService
) : AppCredentialRequestDataService {

    private val log: Logger = LoggerFactory.getLogger(AppCredentialRequestDataServiceImpl::class.java)
    override fun saveCredentialRequestData(issuerName: String, issuerNonce: String, issuerAccessToken: String) {
        try {
            val requestData = getCredentialRequestDataByIssuerName(issuerName)
            // Update the existent data
            val appCredentialRequestDataUpdated = requestData.get().copy(
                issuerNonce = issuerNonce,
                issuerAccessToken = issuerAccessToken
            )
            appCredentialRequestDataRepository.save(appCredentialRequestDataUpdated)
            log.debug("Updated the nonce value and the access token value.")

        }catch (e: CredentialRequestDataNotFoundException){
            val userId = appUserService.getUserWithContextAuthentication().id.toString()
            // Create new data
            val appCredentialRequestData = AppCredentialRequestData(
                id = UUID.randomUUID(),
                issuerName = issuerName,
                userId = userId,
                issuerNonce = issuerNonce,
                issuerAccessToken = issuerAccessToken
            )
            appCredentialRequestDataRepository.save(appCredentialRequestData)
            log.debug("AppCredentialRequestData created.")
        }
    }

    override fun getCredentialRequestDataByIssuerName(issuerName: String):  Optional<AppCredentialRequestData>{
        val userId = appUserService.getUserWithContextAuthentication().id.toString()
        val requestData = appCredentialRequestDataRepository.findAppCredentialRequestDataByIssuerNameAndUserId(issuerName, userId)
        if (requestData.isPresent) {
            return requestData
        }

        else{throw CredentialRequestDataNotFoundException("The $issuerName was not found")}
    }


}