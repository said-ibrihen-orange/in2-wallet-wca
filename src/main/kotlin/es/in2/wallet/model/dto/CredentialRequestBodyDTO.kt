package es.in2.wallet.model.dto


data class CredentialRequestBodyDTO(
        val format: String,
        val proof: ProofDTO
)