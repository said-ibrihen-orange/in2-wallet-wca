package es.in2.wallet.model.dto


import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *  This data class is used to represent the Credential Offer by Reference using credential_offer_uri parameter for a
 *  Pre-Authorized Code Flow.
 *
 *  url: https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-sending-credential-offer-by-
 *
 *  example:
 *      {
 *          "credential_issuer": "https://credential-issuer.example.com",
 *          "credentials": [
 *          "UniversityDegree"
 *          ],
 *          "grants": {
 *              "urn:ietf:params:oauth:grant-type:pre-authorized_code": {
 *                  "pre-authorized_code": "1234",
 *                  "user_pin_required": true
 *               }
 *           }
 *      }
 */
data class CredentialOfferForPreAuthorizedCodeFlow(
    @Schema(
        required = true,
        example = "https://credential-issuer.example.com")
    @JsonProperty("credential_issuer")
    val credentialIssuer: String,

    @Schema(
        required = true,
        example = "[\"UniversityDegree\"]")
    @JsonProperty("credentials")
    val credentials: MutableList<String>,

    @Schema(implementation = Grant::class)
    @JsonProperty("grants")
    val grants: MutableMap<String, Grant>
)

data class Grant(
    @Schema(
        required = true,
        example = "1234")
    @JsonProperty("pre-authorized_code")
    val preAuthorizedCode: String,

    @Schema(
        required = true,
        example = "true")
    @JsonProperty("user_pin_required") val userPinRequired: Boolean,
)
