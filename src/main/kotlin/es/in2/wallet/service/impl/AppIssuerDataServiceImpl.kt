package es.in2.wallet.service.impl

import es.in2.wallet.model.AppIssuerData
import es.in2.wallet.repository.AppIssuerDataRepository
import es.in2.wallet.service.AppIssuerDataService
import org.springframework.stereotype.Service
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

@Service
class AppIssuerDataServiceImpl(
        private val appIssuerDataRepository: AppIssuerDataRepository,
) : AppIssuerDataService {

    private val log: Logger = LoggerFactory.getLogger(AppUserServiceImpl::class.java)
    override fun saveIssuerData(issuerName: String, issuerMetadata: String) {
        val appIssuerData = AppIssuerData(
                issuerName = issuerName,
                issuerMetadata = issuerMetadata
        )
        log.debug("AppUserServiceImpl.saveUser()")
        appIssuerDataRepository.save(appIssuerData)
    }

    override fun getIssuerDataByIssuerName(issuerName: String): Optional<AppIssuerData> {
        log.info("AppUserServiceImpl.getUserByUsername()")
        return appIssuerDataRepository.findAppIssuerDataByIssuerName(issuerName)
    }
}