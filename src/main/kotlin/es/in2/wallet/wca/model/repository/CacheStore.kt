package es.in2.wallet.wca.model.repository

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.util.concurrent.TimeUnit

// Represents a composite key combining both userId and issuerId.
// This allows us to uniquely identify each issuer's data for a specific user.
data class UserIssuerKey(val userId: String, val issuer: String)

// Represents the data that will be stored in the cache for each issuer of a user.
// Contains nonce and token details.
data class VCRequestData(val nonce: String, val token: String)

class CacheStore<K, V>(
    private val expiryDuration: Long,
    timeUnit: TimeUnit
) {
    private val cache: Cache<Any, Any> = CacheBuilder.newBuilder()
        .expireAfterWrite(expiryDuration, timeUnit)
        .concurrencyLevel(Runtime.getRuntime().availableProcessors())
        .build()

    operator fun get(key: K): V? {
        return cache.getIfPresent(key as Any) as V?
    }

    fun delete(key: K) {
        cache.invalidate(key as Any)
    }

    fun add(key: K, value: V) {
        cache.put(key as Any, value as Any)
    }
}