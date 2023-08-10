package es.in2.wallet.service

import es.in2.wallet.model.dto.CredentialFormResponseDTO

fun interface CredentialFormService {
    fun getCredentialForm(): CredentialFormResponseDTO
}