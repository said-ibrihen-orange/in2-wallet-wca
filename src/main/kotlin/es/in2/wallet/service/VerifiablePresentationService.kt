package es.in2.wallet.service

import es.in2.wallet.model.dto.VcSelectorResponseDTO

fun interface VerifiablePresentationService {
    fun createVerifiablePresentation(vcSelectorResponseDTO: VcSelectorResponseDTO): String
}

