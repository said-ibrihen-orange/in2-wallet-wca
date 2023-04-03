package es.in2.wallet.controllers

import es.in2.wallet.services.RequestTokenVerificationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/wallet/siop")
@Tag(name = "DOME User Wallet - Request Token Verification Service", description = "...")
class RequestTokenVerificationController(
    private val requestTokenVerificationService: RequestTokenVerificationService
) {

    @Operation(summary = "Check if the 'request_token' is valid.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "The 'request_token' is valid. Return true."),
            ApiResponse(responseCode = "404", description = "The DID is not found in the Trusted Participant List."),
            ApiResponse(responseCode = "400", description = "The 'request_token' is not valid or is malformed.")
        ]
    )
    @PostMapping(
        path = ["/request-token"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    fun verifyRequestToken(@RequestParam params: Map<String, String>) {
        val requestToken = params["request_token"]!!
        requestTokenVerificationService.verifyRequestToken(requestToken)
    }

}