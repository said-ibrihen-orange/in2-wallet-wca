package es.in2.wallet.controllers

import es.in2.wallet.services.PersistenceService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/vc")
class VerifiableCredentialController(private val persistenceService: PersistenceService) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createVerifiableCredential(@RequestBody verifiableCredential: String) {
        persistenceService.saveVC(verifiableCredential, "1")
    }

    @GetMapping
    fun getVerifiableCredential(@RequestParam userid: String): String {
        return persistenceService.getVCs(userid)
    }

    @DeleteMapping
    fun deleteVerifiableCredential(@RequestParam userid: String, @RequestParam verifiableCredentialId: String) {

    }
}