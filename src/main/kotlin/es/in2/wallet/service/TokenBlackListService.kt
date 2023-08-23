package es.in2.wallet.service

interface TokenBlackListService {
    fun addToBlacklist(token: String)
    fun isBlacklisted(token: String): Boolean
}