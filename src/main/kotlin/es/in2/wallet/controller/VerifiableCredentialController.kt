package es.in2.wallet.controller

import es.in2.wallet.model.dto.CredentialRequestDTO
import es.in2.wallet.service.VerifiableCredentialService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/getVC")
class VerifiableCredentialController(
        private val verifiableCredentialService: VerifiableCredentialService
){
    @PostMapping
    fun getVC(@RequestBody credentialRequestDTO: CredentialRequestDTO): String{
        verifiableCredentialService.getVerifiableCredential(credentialRequestDTO)
        return "works"
    }
}