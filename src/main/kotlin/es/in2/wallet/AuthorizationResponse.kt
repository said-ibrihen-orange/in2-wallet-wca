package es.in2.wallet

import com.fasterxml.jackson.annotation.JsonProperty

data class AuthorizationResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("expires_in") val expiresIn: Int,
    @JsonProperty("refresh_expires_in") val refreshExpiresIn: Int,
    @JsonProperty("token_type") val tokenType : String,
    @JsonProperty("not_before_val_policy") val notBeforeValPolicy : Int,
    val scope : String
) {

    override fun toString(): String {
        return "Authorization Response [access_token: ${this.accessToken}, " +
                "expires_in: ${this.expiresIn}, " +
                "token_type: ${this.tokenType}, " +
                "not_before_val_policy: ${this.notBeforeValPolicy}, " +
                "scope: ${this.scope}, " +
                "refresh_expires_in: ${this.refreshExpiresIn}]"
    }

}