package es.in2.wallet.wca.service

import es.in2.wallet.wca.model.dto.DidRequestDTO
import es.in2.wallet.wca.model.dto.DidResponseDTO

interface WalletDidService {
    fun createDid(didRequestDTO: DidRequestDTO): String
    fun generateDidKey(): String
    fun generateDidKeyWithKid(kid: String): String
    fun getDidsByUserId(): List<DidResponseDTO>
    fun deleteDid(didRequestDTO: DidRequestDTO): String
}

