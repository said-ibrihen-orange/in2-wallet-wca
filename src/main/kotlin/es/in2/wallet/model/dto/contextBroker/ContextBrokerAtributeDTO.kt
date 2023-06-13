package es.in2.wallet.model.dto.contextBroker
import com.fasterxml.jackson.annotation.JsonProperty
class ContextBrokerAtributeDTO (
    @JsonProperty("type") val type: String?,
    @JsonProperty("value") val value: Any?,
    @JsonProperty("metadata") val metadata: Any?
)