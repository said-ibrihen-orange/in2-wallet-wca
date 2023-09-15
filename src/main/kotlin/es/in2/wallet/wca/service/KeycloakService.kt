package es.in2.wallet.service

interface KeycloakService {
    fun test(): String
    fun getKeycloakToken(): String
    fun createUserInKeycloak(token: String, userData: Map<String, Any>)
}
