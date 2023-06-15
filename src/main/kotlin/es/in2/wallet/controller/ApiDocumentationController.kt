package es.in2.wallet.controller

import es.in2.wallet.model.TermsOfService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class ApiDocumentationController {

    @GetMapping("/terms-of-service")
    fun getTermsOfService(): TermsOfService {
        return TermsOfService()
    }

}