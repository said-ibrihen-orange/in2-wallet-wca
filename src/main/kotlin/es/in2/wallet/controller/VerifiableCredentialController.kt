package es.in2.wallet.controller

import es.in2.wallet.service.PersonalDataSpaceService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(name = "Verifiable Credentials", description = "Verifiable Credential management API")
@RestController
@RequestMapping("/api/vc")
class VerifiableCredentialController(
    private val personalDataSpaceService: PersonalDataSpaceService
) {
    @PostMapping("/{userid}")
    @ResponseStatus(HttpStatus.CREATED)
    fun createVerifiableCredential(@RequestBody verifiableCredential: String, @PathVariable userid: String) {
        personalDataSpaceService.saveVC(UUID.fromString(userid), verifiableCredential)
    }

    @GetMapping("/{userid}")
    fun getVerifiableCredential(@PathVariable userid: String): String {
        return personalDataSpaceService.getVCs(UUID.fromString(userid))
    }

    @GetMapping("/{userid}/format/{format}")
    fun getVerifiableCredentialByFormat(@PathVariable format: String, @PathVariable userid: String): String {
        return personalDataSpaceService.getVCsByFormat(UUID.fromString(userid), format)
    }

    @GetMapping("/{userid}/{verifiableCredentialId}/format/{format}")
    fun getVerifiableCredentialById(
        @PathVariable userid: String, @PathVariable verifiableCredentialId: String,
        @PathVariable format: String
    ): String {
        return personalDataSpaceService.getVCByFormat(UUID.fromString(userid), verifiableCredentialId, format)
    }

    @DeleteMapping("/{userid}/{verifiableCredentialId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteVerifiableCredential(@PathVariable userid: String, @PathVariable verifiableCredentialId: String) {
        personalDataSpaceService.deleteVC(UUID.fromString(userid), verifiableCredentialId)
    }

}
