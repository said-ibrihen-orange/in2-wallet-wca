package es.in2.wallet.model

import com.fasterxml.jackson.annotation.JsonProperty

class NGSILDUserEntity(
        @JsonProperty("id") val id: String,  // User ID
        @JsonProperty("type") val type: String = "userEntity",
        @JsonProperty("userData") val userData: NGSILDAttribute<UserAttribute>,
        @JsonProperty("dids") val dids: NGSILDAttribute<List<DidAttribute>>, // Array attribute to store multiple DIDs
        @JsonProperty("vcs") val vcs: NGSILDAttribute<List<VCAttribute>>   // Array attribute to store multiple VCs
)
data class UserAttribute(
        @JsonProperty("username") val username: String,
        @JsonProperty("email") val email: String
)
class NGSILDAttribute<T>(
        @JsonProperty("type") val type: String = "Property",
        @JsonProperty("value") val value: T
)

class DidAttribute(
        @JsonProperty("type") val type:String,
        @JsonProperty("value") val value: String
)

class VCAttribute(
        @JsonProperty("id") val id: String,
        @JsonProperty("type") val type: String,
        @JsonProperty("value") val value: Any
)








