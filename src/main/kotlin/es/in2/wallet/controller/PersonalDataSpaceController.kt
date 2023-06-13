package es.in2.wallet.controller

import es.in2.wallet.model.dto.contextBroker.VerifiableCredentialEntityContextBrokerDTO
import es.in2.wallet.service.PersonalDataSpaceService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.websocket.server.PathParam
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(name = "Personal Data Space", description = "Personal Data Space Management API")
@RestController
@RequestMapping("/api/personal-data-space")
class PersonalDataSpaceController(
    private val personalDataSpaceService: PersonalDataSpaceService
) {

    private val log: Logger = LogManager.getLogger(VerifiablePresentationController::class.java)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun saveVerifiableCredential(@RequestBody verifiableCredential: String) {
        log.debug("VerifiableCredentialController.createVerifiableCredential()")
        personalDataSpaceService.saveVC(verifiableCredential)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getVerifiableCredentialList(): MutableList<VerifiableCredentialEntityContextBrokerDTO> {
        log.debug("VerifiableCredentialController.getVerifiableCredential()")
        return personalDataSpaceService.getAllVerifiableCredentials()
    }

    @GetMapping("/vc/format")
    @ResponseStatus(HttpStatus.OK)
    fun getVerifiableCredentialByFormat(@PathParam("format") format: String): MutableList<VerifiableCredentialEntityContextBrokerDTO> {
        log.debug("VerifiableCredentialController.getVerifiableCredentialByFormat()")
        return personalDataSpaceService.getAllVerifiableCredentialsByFormat(format)
    }

    @GetMapping("/vc/id")
    @ResponseStatus(HttpStatus.OK)
    fun getVerifiableCredentialByIdAndFormat(@PathParam("id") id: String,
                                             @PathParam("format") format: String): VerifiableCredentialEntityContextBrokerDTO {
        log.debug("VerifiableCredentialController.getVerifiableCredentialById()")
        return personalDataSpaceService.getVerifiableCredentialByIdAndFormat(id, format)
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteVerifiableCredential(@PathParam("id") id: String) {
        log.debug("VerifiableCredentialController.deleteVerifiableCredential()")
        personalDataSpaceService.deleteVerifiableCredential(id)
    }

}
