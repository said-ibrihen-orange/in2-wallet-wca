package es.in2.wallet.model

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.node.JsonNodeFactory

class VcContextBrokerEntity(
    @JsonProperty("id") val id: String,
    @JsonProperty("type") val type: String,
    @JsonProperty("userId") @JsonAlias("user_id") val userId: ContextBrokerAttribute,
    @JsonProperty("vcData") @JsonAlias("vc_data") val vcData: ContextBrokerAttribute,
)

class ContextBrokerAttribute(
    @JsonProperty("type") val type: String,
    @JsonProperty("value") val value: Any,
    @JsonProperty("metadata") val metadata: Any? = JsonNodeFactory.instance.objectNode(),
)


