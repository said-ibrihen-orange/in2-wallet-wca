package es.in2.wallet.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

class VcSelectorRequestDTO(
    @JsonProperty("redirectUri") val redirectUri: String,
    @JsonProperty("state") val state: String,
    @JsonProperty("selectableVcList") val selectableVcList: List<VcBasicDataDTO>,
)