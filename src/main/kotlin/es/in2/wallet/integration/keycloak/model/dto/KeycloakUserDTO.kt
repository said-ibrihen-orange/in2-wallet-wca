package es.in2.wallet.integration.keycloak.model.dto

data class KeycloakUserDTO (
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String
)