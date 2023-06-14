package es.in2.wallet.controller

import es.in2.wallet.model.dto.VcBasicDataDTO
import es.in2.wallet.service.PersonalDataSpaceService
import io.swagger.v3.oas.annotations.tags.Tag
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

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getVerifiableCredentialList(): MutableList<VcBasicDataDTO> {
        log.debug("VerifiableCredentialController.getVerifiableCredential()")
        return personalDataSpaceService.getUserVCsInJson()
    }

}
