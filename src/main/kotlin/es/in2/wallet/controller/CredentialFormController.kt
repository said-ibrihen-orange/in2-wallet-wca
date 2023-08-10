package es.in2.wallet.controller

import es.in2.wallet.model.dto.CredentialFormResponseDTO
import es.in2.wallet.service.CredentialFormService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/form")
class CredentialFormController (
        private val credentialFormService: CredentialFormService,
){
    @GetMapping
    @Operation(
            summary = "Get credential form",
            description = "Retrieve a list of proof type and did list."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Get credential form, retrieved successfully."),
        ApiResponse(responseCode = "500", description = "Internal server error.")
    ])
    fun getCredentialForm() : CredentialFormResponseDTO {
        return credentialFormService.getCredentialForm()
    }
}