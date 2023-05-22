package es.in2.wallet

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.Serializable

@Serializable
data class AuthorizationResponse(
    val access_token: String,
    val expires_in: Int,
    val refresh_expires_in: Int,
    val token_type : String,
    val not_before_val_policy : Int,
    val scope : String
) {

    override fun toString(): String {
        return "Authorization Response [access_token: ${this.access_token}, " +
                "expires_in: ${this.expires_in}, " +
                "token_type: ${this.token_type}, " +
                "not_before_val_policy: ${this.not_before_val_policy}, " +
                "scope: ${this.scope}, " +
                "refresh_expires_in: ${this.refresh_expires_in}]"
    }
}