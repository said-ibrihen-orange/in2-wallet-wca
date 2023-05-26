package es.in2.wallet.controllers


import es.in2.wallet.services.CredentialOfferService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/credential-offers")
class CredentialOfferController(
    private val credentialOfferService: CredentialOfferService
) {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getCredentialOfferFromCredentialOfferUri(@RequestParam(name = "credential_offer_uri") credentialOfferUri: String,
                                                 @RequestParam(name = "credential_offer_uri") user: String) {
        credentialOfferService.getCredentialOffer(credentialOfferUri, user)
    }



}