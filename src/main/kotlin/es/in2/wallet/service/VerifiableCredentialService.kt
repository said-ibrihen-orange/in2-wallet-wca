package es.in2.wallet.service

import es.in2.wallet.model.dto.CredentialRequestDTO

interface VerifiableCredentialService {
    fun getCredentialIssuerMetadata(credentialOfferUriExtended: String)
    fun getVerifiableCredential(credentialRequestDTO: CredentialRequestDTO)
}