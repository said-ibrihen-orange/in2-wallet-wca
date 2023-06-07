package es.in2.wallet.model.dto

import lombok.Getter

@Getter
data class BasicAuthCredentialsDTO(
    val username: String,
    val password: String
)