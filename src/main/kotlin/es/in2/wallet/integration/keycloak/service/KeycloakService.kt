package es.in2.wallet.integration.keycloak.service

import es.in2.wallet.integration.keycloak.model.dto.KeycloakUserDTO

interface KeycloakService {
    fun getKeycloakToken(): String
    fun createUserInKeycloak(token: String, userData: KeycloakUserDTO)

    fun getKeycloakUsers(token: String): List<KeycloakUserDTO>

    fun getKeycloakUser(token: String, username: String): KeycloakUserDTO?

    fun getKeycloakUserById(token: String, id: String): KeycloakUserDTO?

    fun deleteKeycloakUser(token: String, username: String)

}
