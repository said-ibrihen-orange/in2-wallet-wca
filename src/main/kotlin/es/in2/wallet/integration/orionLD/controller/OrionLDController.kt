package es.in2.wallet.integration.orionLD.controller

import es.in2.wallet.integration.orionLD.service.OrionLDService
import es.in2.wallet.wca.controller.VerifiablePresentationController
import es.in2.wallet.wca.model.dto.VcBasicDataDTO
import io.swagger.v3.oas.annotations.tags.Tag
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Personal Data Space", description = "Personal Data Space Management API")
@RestController
@RequestMapping("/api/personal-data-space")
class OrionLDController(
    private val orionLDService: OrionLDService
) {

    private val log: Logger = LogManager.getLogger(VerifiablePresentationController::class.java)

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getVerifiableCredentialList(): MutableList<VcBasicDataDTO> {
        log.debug("VerifiableCredentialController.getVerifiableCredential()")
        return orionLDService.getUserVCsInJson()
    }


    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    fun deleteVerifiableCredentials() {
        log.debug("deleteVerifiableCredentials()")
        orionLDService.deleteVCs()
    }

    @DeleteMapping("/{credentialId}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteVerifiableCredential(
        @PathVariable("credentialId") credentialId: String,
    ) {
        log.debug("deleteVerifiableCredential()")
        orionLDService.deleteVerifiableCredential(id=credentialId)
    }

}