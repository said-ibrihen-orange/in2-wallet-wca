package es.in2.wallet.controllers

import es.in2.wallet.entities.AppUser
import es.in2.wallet.services.AppUserService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI

@RestController
@RequestMapping("/api/auth")
class AuthController(private val appUserService: AppUserService){
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerUser(@RequestBody appUser: AppUser): ResponseEntity<String> {

        appUserService.saveUser(appUser)
        val t = appUserService.getUserByUsername(appUser.username)
        val location: URI = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(t?.id)
            .toUri()
        println(location)
        val headers = HttpHeaders()
        headers.set("Location", location.toString())

        return ResponseEntity.created(location)
            .headers(headers)
            .body("User created with uuid: ${t?.id}")
    }
}