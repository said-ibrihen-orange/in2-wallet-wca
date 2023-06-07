package es.in2.wallet.service

import java.util.UUID

fun interface VerifiableCredentialService {

    fun getVerifiableCredential(userUUID: UUID, credentialOfferUri: String)

}