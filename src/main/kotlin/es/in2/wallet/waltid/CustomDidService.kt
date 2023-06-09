package es.in2.wallet.waltid

interface CustomDidService {
    fun generateDidKey(): String
    fun generateDidKeyWithKid(kid: String): String
}

