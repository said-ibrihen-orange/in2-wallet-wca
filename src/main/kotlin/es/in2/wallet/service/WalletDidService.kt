package es.in2.wallet.service

import es.in2.wallet.model.dto.DidRequestDTO

interface WalletDidService {
    fun createDid(didRequestDTO: DidRequestDTO): String
    fun generateDidKey(): String
    fun generateDidKeyWithKid(kid: String): String
    fun getDidsByUserId(): List<String>
    fun deleteDid(did: String)
}

