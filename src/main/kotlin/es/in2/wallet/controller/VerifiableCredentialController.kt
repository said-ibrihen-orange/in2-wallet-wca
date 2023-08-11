package es.in2.wallet.controller

import es.in2.wallet.model.dto.CredentialRequestDTO
import es.in2.wallet.service.VerifiableCredentialService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/getVC")
class VerifiableCredentialController(
        private val verifiableCredentialService: VerifiableCredentialService
){
    @PostMapping
    @Operation(
            summary = "Get a verifiable credential",
            description = "Get a verifiable credential and save it in the personal data space."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Verifiable credential successfully saved."),
        ApiResponse(responseCode = "400", description = "Invalid request."),
        ApiResponse(responseCode = "403", description = "Access token has expired"),
        ApiResponse(responseCode = "500", description = "Internal server error.")
    ])
    fun getVC(@RequestBody credentialRequestDTO: CredentialRequestDTO): String{
        verifiableCredentialService.getVerifiableCredential(credentialRequestDTO)
        return "Verifiable Credential stored"
    }
}