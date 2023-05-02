package es.in2.wallet

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class WalletProperties(
    @Value("\${dome.backend.router.base}") val domeBackendBaseURL: String
)