package es.in2.wallet.controllers

import es.in2.wallet.services.PersistenceService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/vc")
class VerifiableCredentialController(private val persistenceService: PersistenceService) {
    @PostMapping("/{userid}")
    @ResponseStatus(HttpStatus.CREATED)
    fun createVerifiableCredential(
        @RequestBody verifiableCredential: String,
        @PathVariable userid: String
    ){
        persistenceService.saveVC(verifiableCredential, userid)
    }

    @GetMapping("/{userid}")
    fun getVerifiableCredential(
        @PathVariable userid: String): String {
        return persistenceService.getVCs(userid)
    }

    @GetMapping("/{userid}/format/{format}")
    fun getVerifiableCredentialByFormat(
        @PathVariable format: String,
        @PathVariable userid: String
    ): String {
        return persistenceService.getVCsByFormat(userid, format)
    }

    @GetMapping("/{userid}/{verifiableCredentialId}/format/{format}")
    fun getVerifiableCredentialById(
        @PathVariable userid: String,
        @PathVariable verifiableCredentialId: String,
        @PathVariable format: String
    ): String {
        return persistenceService.getVCByFormat(userid,verifiableCredentialId,format )
    }

    @DeleteMapping("/{userid}/{verifiableCredentialId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteVerifiableCredential(
        @PathVariable userid: String,
        @PathVariable verifiableCredentialId: String
    ) {
        persistenceService.deleteVC(userid, verifiableCredentialId)
    }
}