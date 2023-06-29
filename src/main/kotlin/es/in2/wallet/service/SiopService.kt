package es.in2.wallet.service

import es.in2.wallet.model.dto.VcSelectorRequestDTO
import es.in2.wallet.model.dto.VcSelectorResponseDTO
import java.util.*

interface SiopService {
    fun getSiopAuthenticationRequest(siopAuthenticationRequestUri: String): VcSelectorRequestDTO
    fun processSiopAuthenticationRequest(siopAuthenticationRequest: String): VcSelectorRequestDTO
    fun sendAuthenticationResponse(vcSelectorResponseDTO: VcSelectorResponseDTO, vp: String): String
}