package es.in2.wallet.controller

import es.in2.wallet.service.WalletDidService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/did")
class DidController(
    private val didService: WalletDidService
) {

    @PostMapping
    fun createDid(): String {
        return didService.generateDidKey()
    }

}