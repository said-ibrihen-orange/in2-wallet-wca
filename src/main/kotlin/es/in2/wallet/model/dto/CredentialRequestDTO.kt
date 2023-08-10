package es.in2.wallet.model.dto

data class CredentialRequestDTO (
        val issuerName: String,
        val proofType: String,
        val did: String
)