package es.in2.wallet.service

fun interface VerifiableCredentialService {
    fun getCredentialIssuerMetadata(credentialOfferUriExtended: String)
}