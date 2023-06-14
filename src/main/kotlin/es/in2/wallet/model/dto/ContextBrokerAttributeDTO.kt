package es.in2.wallet.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

class ContextBrokerAttributeDTO(
    @JsonProperty("type") val type: String,
    @JsonProperty("value") val value: Any,
    @JsonProperty("metadata") val metadata: Any?
)