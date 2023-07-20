package es.in2.wallet.controller

import es.in2.wallet.model.dto.DidRequestDTO
import es.in2.wallet.service.WalletDidService
import io.swagger.v3.oas.annotations.tags.Tag

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "DID Management", description = "Personal Data Space for DID Management API")
@RestController
@RequestMapping("/api/dids")
class DidManagementController(
    private val walletDidService: WalletDidService
) {

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    fun createDid(@RequestBody didRequestDTO: DidRequestDTO): String {
        walletDidService.createDid(didRequestDTO)
        return "DID created"
    }

}