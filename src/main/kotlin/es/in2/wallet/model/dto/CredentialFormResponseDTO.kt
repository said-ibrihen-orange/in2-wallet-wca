package es.in2.wallet.model.dto

data class CredentialFormResponseDTO(
        val proofTypeList: List<String>,
        val didList : List<DidResponseDTO>
)