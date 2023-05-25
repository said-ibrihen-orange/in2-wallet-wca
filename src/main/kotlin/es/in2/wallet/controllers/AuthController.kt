package es.in2.wallet.controllers

import es.in2.wallet.entities.AppUser
import es.in2.wallet.services.AppUserService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val appUserService: AppUserService){
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerUser(@RequestBody appUser: AppUser): ResponseEntity<String> {
        // Get User UUID
        var uuid: UUID
        try {
            uuid = appUserService.registerUser(appUser.username)
        } catch (e: Exception) {
            return if (e.message == "User already exists") {
                ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists")
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating user")
            }
        }

        val location: URI = URI.create("$uuid")

        val headers = HttpHeaders()
        headers["Location"] = location.toString()
        return ResponseEntity.created(location)
            .headers(headers)
            .build()
    }

    @GetMapping("/users/{uuid}")
    fun getUserId(@PathVariable uuid: String): Optional<AppUser> {
        return appUserService.getUserById(UUID.fromString(uuid))
    }
}