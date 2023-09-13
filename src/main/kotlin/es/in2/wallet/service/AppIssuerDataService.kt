package es.in2.wallet.service

import es.in2.wallet.model.AppIssuerData
import java.util.*

interface AppIssuerDataService {
    fun upsertIssuerData(issuerName: String, issuerMetadata: String)
    fun getIssuerDataByIssuerName(issuerName: String): Optional<AppIssuerData>
    fun getIssuers(): List<String>
    fun getMetadata(issuerName: String): String
}