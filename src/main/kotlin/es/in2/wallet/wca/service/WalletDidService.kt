package es.in2.wallet.wca.service

import es.in2.wallet.wca.model.dto.DidRequestDTO


interface WalletDidService {
    fun createDid(didRequestDTO: DidRequestDTO): String
    fun generateDidKey(): String
    fun generateDidKeyWithKid(kid: String): String
    fun getDidsByUserId(): List<String>
    // fixme: deleteDid() should use a DidRequestDTO not a DidResponseDTO
    fun deleteDid(did: String): String
}

