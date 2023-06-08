package es.in2.wallet.controller

import es.in2.wallet.model.AppUser
import es.in2.wallet.model.dto.VpRequestDTO
import es.in2.wallet.service.SiopService
import es.in2.wallet.service.VerifiablePresentationService
import es.in2.wallet.util.JWT
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(name = "Verifiable Presentations", description = "Verifiable Presentation management API")
@RestController
@RequestMapping("/api/vp")
class VerifiablePresentationController(
    private val verifiablePresentationService: VerifiablePresentationService,
    private val siopService: SiopService
) {

    private val log: Logger = LogManager.getLogger(VerifiablePresentationController::class.java)

    @Operation(summary = "Create a Verifiable Presentation with the Verifiable Credentials attached.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "The Verifiable Credential was created and send successfully."
            )
        ]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    fun executeURLVP(@RequestBody vpRequestDto: VpRequestDTO): String {
        // create a verifiable presentation
        val vp = verifiablePresentationService.createVerifiablePresentation(vpRequestDto.verifiableCredentials, JWT)
        // send the verifiable presentation to the dome backend
        return siopService.sendAuthenticationResponse(vpRequestDto.siopAuthenticationRequest, vp)
    }


    @PostMapping("/vp/v2")
    @ResponseStatus(HttpStatus.OK)
    fun executeVPv2(@RequestBody vpRequestDto: VpRequestDTO): String {

        return verifiablePresentationService.executeVP(vpRequestDto.verifiableCredentials,
            vpRequestDto.siopAuthenticationRequest)
    }

}