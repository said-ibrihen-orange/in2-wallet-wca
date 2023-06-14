package es.in2.wallet.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

class VcBasicDataDTO(
    @JsonProperty("id") val id: String,
    @JsonProperty("vc_type") val vcType: MutableList<String>,
    @JsonProperty("credential_subject") val credentialSubject: Any
)