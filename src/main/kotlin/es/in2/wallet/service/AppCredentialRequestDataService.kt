package es.in2.wallet.service

import es.in2.wallet.model.AppCredentialRequestData
import java.util.*

interface AppCredentialRequestDataService {
    fun saveCredentialRequestData(issuerName: String, issuerNonce: String, issuerAccessToken: String)
    fun getCredentialRequestDataByIssuerName(issuerName: String) : Optional<AppCredentialRequestData>

    fun clearIssuerNonceByIssuerName(issuerName: String)
}