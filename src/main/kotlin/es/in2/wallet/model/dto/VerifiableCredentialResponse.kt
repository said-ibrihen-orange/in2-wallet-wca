package es.in2.wallet.model.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = """
    Implements the credential response according to 
    https://github.com/hesusruiz/EUDIMVP/blob/main/issuance.md#credential-response
    """)
data class VerifiableCredentialResponse(
    @Schema(
        required = true,
        example = "jwt_vc_json",
        description = "Format of the issued Credential.")
    @JsonProperty("format") val format: String,
    @Schema(
        required = true,
        example = "LUpixVCWJk0eOt4CXQe1NXK....WZwmhmn9OQp6YxX0a2L",
        description = "Contains issued Credential")
    @JsonProperty("credential") val credential: String,
    @Schema(
        required = false,
        example = "fGFF7UkhLA",
        description = """
            Nonce to be used to create a proof of possession of key material when requesting a Credential. 
            When received, the Wallet MUST use this nonce value for its subsequent credential requests until the 
            Credential Issuer provides a fresh nonce.
        """)
    @JsonProperty("cnonce") val cNonce: String,
    @Schema(
        required = false,
        example = "86400",
        description = "Lifetime in seconds of the c_nonce")
    @JsonProperty("cnonceExpiresIn") val cNonceExpiresIn: Int
)
