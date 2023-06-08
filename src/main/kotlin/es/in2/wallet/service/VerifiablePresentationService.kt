package es.in2.wallet.service

import java.util.UUID

interface VerifiablePresentationService {
    fun createVerifiablePresentation(verifiableCredentials: List<String>, format: String): String
    fun executeVP(vps: List<String>, siopAuthenticationRequest: String): String
}

