package es.in2.wallet.domain.dtos

import com.fasterxml.jackson.annotation.JsonProperty

class QrContentDto (
    @JsonProperty("qr_content") val content: String
)