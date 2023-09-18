package es.in2.wallet.api.config

import es.in2.wallet.wca.model.repository.CacheStore
import es.in2.wallet.wca.model.repository.UserIssuerKey
import es.in2.wallet.wca.model.repository.VCRequestData
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CacheConfig {

    // Define a Bean for the CacheStore with UserIssuerKey and VCRequestData types.
    // Adjust the expiryDuration and timeUnit as required.
    @Bean
    fun cacheStoreForVCRequestData(): CacheStore<UserIssuerKey, VCRequestData> {
        return CacheStore(expiryDuration = 5, timeUnit = TimeUnit.MINUTES)
    }
}