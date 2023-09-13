package es.in2.wallet.service

import com.nimbusds.jose.jwk.ECKey
import id.walt.crypto.KeyId

interface WalletKeyService {
    fun generateKey(): KeyId
    fun getECKeyFromKid(kid: String): ECKey
}
