package es.in2.wallet.waltid

import com.nimbusds.jose.jwk.ECKey
import id.walt.crypto.KeyId

interface CustomKeyService {
    fun generateKey(): KeyId
    fun getECKeyFromKid(kid: String): ECKey
}
