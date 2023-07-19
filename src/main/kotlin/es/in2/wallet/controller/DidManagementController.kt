package es.in2.wallet.controller

import es.in2.wallet.service.WalletDidService
import io.swagger.v3.oas.annotations.tags.Tag

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "DID Management", description = "Personal Data Space for DID Management API")
@RestController
@RequestMapping("/api/did-management")
class DidManagementController(
    private val walletDidService: WalletDidService
) {

    @PostMapping("/dids/createkey")
    @ResponseStatus(HttpStatus.OK)
    fun createDidKey(): String {
        return walletDidService.createDidKey()
    }

    @PostMapping("/dids/createelsi/{elsi}")
    @ResponseStatus(HttpStatus.OK)
    fun createDidElsi(@PathVariable elsi: String): String {
         return walletDidService.createDidElsi(elsi)
    }

}