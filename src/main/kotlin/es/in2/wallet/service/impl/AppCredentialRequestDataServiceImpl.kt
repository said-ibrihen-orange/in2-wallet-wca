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
        val userId = appUserService.getUserWithContextAuthentication().id
        val appCredentialRequestData = AppCredentialRequestData(
                issuerName = issuerName,
                userId = userId,
                issuerNonce = issuerNonce,
                issuerAccessToken = issuerAccessToken
        )
        appCredentialRequestDataRepository.save(appCredentialRequestData)
        log.debug("AppCredentialRequestData created.")

    }

    override fun getCredentialRequestDataByIssuerName(issuerName: String): Optional<AppCredentialRequestData> {
        val userId = appUserService.getUserWithContextAuthentication().id
        if (userId != null) {
            return appCredentialRequestDataRepository.findAppCredentialRequestDataByIssuerNameAndUserId(issuerName,userId)
        }
        else{throw CredentialRequestDataNotFoundException("The $issuerName was not found")}
    }


}