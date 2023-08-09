package es.in2.wallet.service

import es.in2.wallet.model.AppIssuerData
import es.in2.wallet.model.dto.AppIssuerDataResponseDTO
import java.util.*

interface AppIssuerDataService {
    fun saveIssuerData(issuerName: String, issuerMetadata: String)
    fun getIssuerDataByIssuerName(issuerName: String): Optional<AppIssuerData>
    fun getIssuers(): List<AppIssuerDataResponseDTO>
}