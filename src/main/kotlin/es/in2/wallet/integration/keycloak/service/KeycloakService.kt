package es.in2.wallet.integration.keycloak.service

import es.in2.wallet.integration.keycloak.model.dto.KeycloakUserDTO

interface KeycloakService {
    fun test(): String
    fun getKeycloakToken(): String
    fun createUserInKeycloak(token: String, userData: KeycloakUserDTO)
}
