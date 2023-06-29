package es.in2.wallet.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class QrContentDTO (
    @JsonProperty("qr_content") val content: String
)