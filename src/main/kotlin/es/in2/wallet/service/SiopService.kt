package es.in2.wallet.service

import java.util.*

interface SiopService {
    fun getSiopAuthenticationRequest(siopAuthenticationRequestUri: String): MutableList<String>
    fun processSiopAuthenticationRequest(siopAuthenticationRequest: String): MutableList<String>
    fun sendAuthenticationResponse(siopAuthenticationRequest: String, vp: String): String
}