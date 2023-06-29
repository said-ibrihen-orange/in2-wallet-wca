package es.in2.wallet.service

fun interface TokenVerificationService {
    fun verifySiopAuthRequestAsJwsFormat(requestToken: String)
}