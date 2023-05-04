package es.in2.wallet.controllers

import com.fasterxml.jackson.annotation.JsonProperty
import es.in2.wallet.JWT
import es.in2.wallet.services.ExecuteContentService
import es.in2.wallet.services.SiopVerifiablePresentationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/execute-content")
class ExecuteContentController(
    private val executeContentService: ExecuteContentService,
    private val siopVerifiablePresentationService: SiopVerifiablePresentationService
) {

    private val log: Logger = LogManager.getLogger(ExecuteContentController::class.java)

    @PostMapping("/get-siop-authentication-request")
    @ResponseStatus(HttpStatus.OK)
    fun executeURL(@RequestBody qrContent: QrContent): String {
        log.info("execute QR content - content ${qrContent.content}")
        return executeContentService.getAuthenticationRequest(qrContent.content)
    }

    @Operation(summary = "Create a Verifiable Presentation with the Verifiable Credentials attached.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "The Verifiable Credential was created and send successfully."
            )
        ]
    )
    @PostMapping("/vp")
    @ResponseStatus(HttpStatus.OK)
    fun executeURLVP(@RequestBody vpRequest: VpRequest): String {
        // create a verifiable presentation
        val vp = siopVerifiablePresentationService.createVerifiablePresentation(
            vpRequest.verifiableCredentials, JWT
        )
        // send the verifiable presentation to the dome backend
        return executeContentService.sendAuthenticationResponse(
            vpRequest.siopAuthenticationRequest, vp
        )
    }
}

class VpRequest(
    @JsonProperty("siop_authentication_request") val siopAuthenticationRequest: String,
    @JsonProperty("vc_list") val verifiableCredentials: List<String>
)

class QrContent(
    @JsonProperty("qr_content") val content: String
)

