package es.in2.wallet.service.impl

import es.in2.wallet.service.TokenBlackListService
import org.springframework.stereotype.Service
import java.util.*

@Service
class TokenBlackListServiceImpl: TokenBlackListService {
    private val blacklist = Collections.synchronizedSet(mutableSetOf<String>())
    override fun addToBlacklist(token: String) {
            blacklist.add(token)
    }

    override fun isBlacklisted(token: String): Boolean {
        return blacklist.contains(token)
    }
}