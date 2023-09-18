package es.in2.wallet.integration.orionLD.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

class UserEntity(
    @JsonProperty("id") val id: String,  // User ID
    @JsonProperty("type") val type: String = "userEntity",
    @JsonProperty("userData") val userData: EntityAttribute<UserAttribute>,
    @JsonProperty("dids") val dids: EntityAttribute<List<DidAttribute>>, // Array attribute to store multiple DIDs
    @JsonProperty("issuers") val issuers: EntityAttribute<List<IssuerAttribute>>, // Array attribute to store multiple issuers
    @JsonProperty("vcs") val vcs: EntityAttribute<List<VCAttribute>>   // Array attribute to store multiple VCs
)
class EntityAttribute<T>(
    @JsonProperty("type") val type: String = "Property",
    @JsonProperty("value") val value: T
)
class UserAttribute(
    @JsonProperty("username") val username: String,
    @JsonProperty("email") val email: String
)
class DidAttribute(
    @JsonProperty("type") val type:String,
    @JsonProperty("value") val value: String
)
class IssuerAttribute(
    @JsonProperty("issuer") val issuer: String,
    @JsonProperty("data") val data: JsonNode
)
class VCAttribute(
    @JsonProperty("id") val id: String,
    @JsonProperty("type") val type: String,
    @JsonProperty("value") val value: Any
)