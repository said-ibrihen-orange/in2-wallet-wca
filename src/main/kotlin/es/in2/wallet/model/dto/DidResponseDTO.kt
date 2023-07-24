package es.in2.wallet.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class DidResponseDTO(
        @JsonProperty("id") val did: String
)