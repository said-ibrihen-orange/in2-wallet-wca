package es.in2.wallet.service

import java.util.*

interface SiopService {
    fun getSiopAuthenticationRequest(userUUID: UUID, siopAuthenticationRequestUri: String): MutableList<String>
    fun processSiopAuthenticationRequest(userUUID: UUID, siopAuthenticationRequest: String): MutableList<String>
    fun sendAuthenticationResponse(siopAuthenticationRequest: String, vp: String): String
}