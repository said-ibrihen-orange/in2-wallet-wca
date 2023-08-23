package es.in2.wallet.controller

import es.in2.wallet.service.TokenBlackListService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController {

    @Autowired
    private lateinit var tokenBlacklistService: TokenBlackListService

    @PostMapping("/logout")
    fun logout(@RequestHeader("Authorization") token: String): ResponseEntity<Unit> {
        tokenBlacklistService.addToBlacklist(token)
        return ResponseEntity.ok().build()
    }

}