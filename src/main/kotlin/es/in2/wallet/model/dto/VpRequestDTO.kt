package es.in2.wallet.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

class VpRequestDTO (
    @JsonProperty("siop_authentication_request") val siopAuthenticationRequest: String,
    @JsonProperty("vc_list") val verifiableCredentials: List<String>
)