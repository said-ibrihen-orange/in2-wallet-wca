package es.in2.wallet.controllers

import es.in2.wallet.JWT
import es.in2.wallet.domain.dtos.QrContentDto
import es.in2.wallet.domain.dtos.VpRequestDto
import es.in2.wallet.services.AuthRequestContent
import es.in2.wallet.services.ExecuteQrContentService
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
class ExecuteQrContentController(
    private val executeContentService: ExecuteQrContentService,
    private val siopVerifiablePresentationService: SiopVerifiablePresentationService
) {

    private val log: Logger = LogManager.getLogger(ExecuteQrContentController::class.java)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun executeQrContent(@RequestBody qrContentDto: QrContentDto,
                       @RequestHeader headers:HashMap<String,String>): Any {
        log.info("ExecuteContentController - executeContent() - QR Content: ${qrContentDto.content}")
        //Fixme capture the user from principal and pass it to the service
        val userUUID = "1"
        return executeContentService.executeQR(userUUID,qrContentDto.content)

    }



    @PostMapping("/get-siop-authentication-request")
    @ResponseStatus(HttpStatus.OK)
    fun executeURL(@RequestBody qrContentDto: QrContentDto): AuthRequestContent {
        return executeContentService.getAuthenticationRequest(qrContentDto.content)
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
    fun executeURLVP(@RequestBody vpRequestDto: VpRequestDto): String {
        // create a verifiable presentation
        log.info("building Verifiable Presentation")
        val vp = siopVerifiablePresentationService.createVerifiablePresentation(
            vpRequestDto.verifiableCredentials, JWT
        )
        log.info("executing the post Authentication Response ")
        // send the verifiable presentation to the dome backend
        return executeContentService.sendAuthenticationResponse(
            vpRequestDto.siopAuthenticationRequest, vp
        )
    }
}


