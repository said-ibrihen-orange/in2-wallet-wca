package es.in2.wallet.controllers

import es.in2.wallet.services.AuthorizationService
import es.in2.wallet.services.CredentialOfferService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/credential-offers")
class CredentialOfferController(
    private val credentialOfferService: CredentialOfferService,
    private val authorizationService: AuthorizationService
) {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getCredentialOfferFromCredentialOfferUri(@RequestParam(name = "credential_offer_uri") credentialOfferUri: String,
                                                 @RequestParam(name = "credential_offer_uri") user: String): String {
        return credentialOfferService.getCredentialOffer(credentialOfferUri, user)
    }

    @Deprecated("Don't use")
    @GetMapping("/token")
    @ResponseStatus(HttpStatus.OK)
    fun getToken(@RequestParam(name = "grant_type") grantType: String,
                 @RequestParam(name = "pre-authorized_code") preAuthorizedCode: String,
                 @RequestParam(name = "user_pin") userPin: String) : String{
        return authorizationService.getToken(grantType, preAuthorizedCode, userPin)
    }

}