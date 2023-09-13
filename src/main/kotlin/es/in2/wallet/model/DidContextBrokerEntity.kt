package es.in2.wallet.model

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

class DidContextBrokerEntity(
    @JsonProperty("id") val id: String, // We use the did directly on the id
    @JsonProperty("type") val type: String,
    @JsonProperty("userId") @JsonAlias("user_id") val userId: ContextBrokerAttribute, // We associate with userId for further queries
)