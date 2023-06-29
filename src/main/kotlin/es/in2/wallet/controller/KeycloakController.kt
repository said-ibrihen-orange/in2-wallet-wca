package es.in2.wallet.controllers
import es.in2.wallet.service.KeycloakService
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import jakarta.annotation.security.PermitAll
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class KeycloakToken(private val keycloakService: KeycloakService) {


    data class UserData(
            val enabled: Boolean,
            val username: String,
            val email: String,
            val firstName: String,
            val lastName: String
    )
    @PermitAll
    @GetMapping("/test")
    fun test(): String {
        println("test")
        return "test"
    }

    @PermitAll
    @GetMapping("/token")
    fun getKeycloakToken(): String {
        return keycloakService.getKeycloakToken()
    }
    @PermitAll
    @PostMapping("/create")
    suspend fun createUserInKeycloak(
            @RequestBody userData: UserData
    ) {
        val token = keycloakService.getKeycloakToken()
        val userDataMap = mapOf(
                "enabled" to userData.enabled,
                "username" to userData.username,
                "email" to userData.email,
                "firstName" to userData.firstName,
                "lastName" to userData.lastName
        )
        keycloakService.createUserInKeycloak(token, userDataMap)
    }
}