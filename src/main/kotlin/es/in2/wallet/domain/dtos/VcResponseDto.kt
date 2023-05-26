package es.in2.wallet.domain.dtos

import com.fasterxml.jackson.annotation.JsonProperty

class VCResponseDto (@JsonProperty("id") val id: String,
                     @JsonProperty("type") val type: String,
                     @JsonProperty("user_ID") val userID: String,
                     @JsonProperty("vc") val vc: Any
)