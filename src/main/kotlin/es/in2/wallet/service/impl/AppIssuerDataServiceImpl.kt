package es.in2.wallet.service.impl

import es.in2.wallet.exception.IssuerDataNotFoundException
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

    /**
     * Inserts or updates Credential Issuer Metadata on an SQL table
     */
    override fun upsertIssuerData(issuerName: String, issuerMetadata: String) {
        val issuerData = getIssuerDataByIssuerName(issuerName)
        val appIssuerData: AppIssuerData = if (issuerData.isPresent) {
            AppIssuerData(
                id = issuerData.get().id,
                name = issuerName,
                metadata = issuerMetadata
            )
        } else {
            AppIssuerData(
                id = null,
                name = issuerName,
                metadata = issuerMetadata
            )
        }
        appIssuerDataRepository.save(appIssuerData)
    }

    override fun getIssuerDataByIssuerName(issuerName: String): Optional<AppIssuerData> {
        log.info("AppIssuerDataServiceImpl.getUserByUsername()")
        return appIssuerDataRepository.findAppIssuerDataByName(issuerName)
    }

    override fun getIssuers(): List<String> {
        log.info("AppIssuerServiceImpl.getIssuers()")
        val issuers = appIssuerDataRepository.findAll()
        val issuerResponseList = issuers.map { issuer ->
            issuer.name
        }
        if (issuerResponseList.isEmpty()) {
            throw IssuerDataNotFoundException("The Issuer List is empty.")
        }
        return issuerResponseList
    }

    override fun getMetadata(issuerName: String): String {
        val issuerData = getIssuerDataByIssuerName(issuerName)
        if (issuerData.isPresent){
            return issuerData.get().metadata
        } else {
            throw IssuerDataNotFoundException("Issuer metadata not found for $issuerName")
        }
    }
}