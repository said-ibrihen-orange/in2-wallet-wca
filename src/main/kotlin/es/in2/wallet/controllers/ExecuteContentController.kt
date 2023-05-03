package es.in2.wallet.controllers

import es.in2.wallet.JWT
import es.in2.wallet.services.ExecuteContentService
import es.in2.wallet.services.RequestTokenVerificationService
import es.in2.wallet.services.SiopVerifiablePresentationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.websocket.server.PathParam
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/execute-content")
class ExecuteContentController(
    private val executeContentService: ExecuteContentService,
    private val requestTokenVerificationService: RequestTokenVerificationService,
    private val siopVerifiablePresentationService: SiopVerifiablePresentationService
) {

    private val log: Logger = LogManager.getLogger(ExecuteContentController::class.java)

    @PostMapping(path = ["/get-siop-authentication-request"])
    //@CrossOrigin(origins = ["http://localhost:8100","http://localhost:8000","https://domewalletdev.in2.es"])
    @ResponseStatus(HttpStatus.OK)
    fun executeURL(@RequestBody url: String): HashMap<String, String> {
        log.info("Getting url: $url")
        val requestToken = executeContentService.getAuthenticationRequest(url)
        requestTokenVerificationService.verifyRequestToken(requestToken)
        val map = HashMap<String, String>()
        map["requestToken"] = requestToken
        return map
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
    @PostMapping(
        path = ["/vp"]
    )
    //consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    @CrossOrigin(origins = ["http://localhost:8100", "http://localhost:8000", "https://domewalletdev.in2.es"])
    @ResponseStatus(HttpStatus.OK)
    fun executeURLVP(
        @PathParam("state") state: String,
        @RequestBody verifiableCredentials: List<String>
    ): String {
        log.info("Getting vp: $verifiableCredentials ")
        log.info("State $state")
        // create a verifiable presentation
        val vp = siopVerifiablePresentationService.createVerifiablePresentation(verifiableCredentials, JWT)
        log.info("VP created : $vp")
        // send the verifiable presentation to the dome backend
        return executeContentService.sendAuthenticationResponse(state, vp)
    }


}