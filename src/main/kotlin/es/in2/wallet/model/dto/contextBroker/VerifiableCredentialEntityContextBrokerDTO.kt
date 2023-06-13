package es.in2.wallet.model.dto.contextBroker
import com.fasterxml.jackson.annotation.JsonProperty
class VerifiableCredentialEntityContextBrokerDTO (
    @JsonProperty("id") val id:String,
    @JsonProperty("type") val type:String,
    @JsonProperty("userID") val userID:ContextBrokerAttributeDTO,
    @JsonProperty("vc") val vc:ContextBrokerAttributeDTO
 )