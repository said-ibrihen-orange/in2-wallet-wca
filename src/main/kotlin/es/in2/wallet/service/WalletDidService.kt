package es.in2.wallet.service

import es.in2.wallet.model.dto.DidRequestDTO
import es.in2.wallet.model.dto.DidResponseDTO

interface WalletDidService {
    fun createDid(didRequestDTO: DidRequestDTO): String
    fun generateDidKey(): String
    fun generateDidKeyWithKid(kid: String): String
    fun getDidsByUserId(): List<DidResponseDTO>
}

