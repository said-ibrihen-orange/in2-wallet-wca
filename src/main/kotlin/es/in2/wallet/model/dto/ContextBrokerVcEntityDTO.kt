package es.in2.wallet.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

class ContextBrokerVcEntityDTO(
    @JsonProperty("id") val id: String,
    @JsonProperty("type") val type: String,
    @JsonProperty("user_ID") val userID: ContextBrokerAttributeDTO,
    @JsonProperty("vc") val vc: ContextBrokerAttributeDTO
)