package es.in2.wallet.domain.dtos

import com.fasterxml.jackson.annotation.JsonProperty

class VpRequestDto (
    @JsonProperty("siop_authentication_request") val siopAuthenticationRequest: String,
    @JsonProperty("vc_list") val verifiableCredentials: List<String>
)