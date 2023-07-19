package es.in2.wallet.service

interface WalletDidService {
    fun createDidKey(): String
    fun createDidElsi(elsi: String): String
    fun generateDidKey(): String
    fun generateDidKeyWithKid(kid: String): String
}

