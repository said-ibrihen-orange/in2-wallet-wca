package es.in2.wallet.controllers

import es.in2.wallet.services.SiopVerifiablePresentationService
import es.in2.wallet.JWT
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/siop")
@Tag(name = "DOME User Wallet - Verifiable Presentation Creation Service", description = "...")
class SiopVerifiablePresentationController(
    private val siopVerifiablePresentationService: SiopVerifiablePresentationService
) {

    @Operation(summary = "Create a Verifiable Presentation with the Verifiable Credentials attached.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "The Verifiable Credential was created successfully.")
        ]
    )
    @PostMapping("/vp")
    @ResponseStatus(HttpStatus.CREATED)
    fun createVerifiablePresentation(
        httpServletRequest: HttpServletRequest,
        @RequestBody verifiableCredentials: List<String>
    ): String {
        /*
            In next versions we need to handle with different Verifiable Presentation formats.
            That is, JSON+JWT and CBOR+MSO.

            We MUST consider how format is passed between the Wallet implementation and the
            Server Backend.
         */
        return siopVerifiablePresentationService.createVerifiablePresentation(verifiableCredentials, JWT)
    }

}