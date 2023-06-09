package es.in2.wallet.controller

import com.google.common.io.Resources
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class ApiDocumentation {

    @GetMapping("/terms-of-service")
    fun getTermsOfService(): String {
        return Resources.getResource("terms-of-service.json").readText()
    }

}