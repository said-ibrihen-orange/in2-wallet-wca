package es.in2.wallet.service

interface WalletDidService {
    fun generateDidKey(): String
    fun generateDidKeyWithKid(kid: String): String
}

