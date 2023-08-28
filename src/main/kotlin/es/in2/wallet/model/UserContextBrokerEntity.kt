package es.in2.wallet.model

import com.fasterxml.jackson.annotation.JsonProperty

class UserContextBrokerEntity(
    @JsonProperty("id") val id: String,  // User ID
    @JsonProperty("type") val type: String = "User",
    @JsonProperty("dids") val dids: NGSILDArrayAttribute, // Array attribute to store multiple DIDs
    @JsonProperty("vcs") val vcs: NGSILDArrayAttribute,   // Array attribute to store multiple VCs
    // Add other user attributes as needed
)

class NGSILDArrayAttribute(
    @JsonProperty("type") val type: String = "Property",
    @JsonProperty("value") val value: List<Any>,  // List to store multiple values
    // Other necessary attributes, such as observedAt, can be added here
)


