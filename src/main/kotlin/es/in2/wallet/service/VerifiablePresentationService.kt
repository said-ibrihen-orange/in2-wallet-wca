package es.in2.wallet.service

interface VerifiablePresentationService {
    fun createVerifiablePresentation(verifiableCredentials: List<String>, format: String): String
    fun executeVP(vcIdList: List<String>, siopAuthenticationRequest: String): String
}

