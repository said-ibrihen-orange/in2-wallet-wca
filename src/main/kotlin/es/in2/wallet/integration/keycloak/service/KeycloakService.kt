package es.in2.wallet.integration.keycloak.service

interface KeycloakService {
    fun test(): String
    fun getKeycloakToken(): String
    fun createUserInKeycloak(token: String, userData: Map<String, Any>)
}
