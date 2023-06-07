package es.in2.wallet.configuration

import es.in2.wallet.util.WalletUtils
import es.in2.wallet.waltid.impl.CustomDidServiceImpl
import es.in2.wallet.waltid.impl.CustomKeyServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WalletDidConfig {

    @Bean
    fun walletIssuerDID(): String {
        val customDidService = CustomDidServiceImpl(CustomKeyServiceImpl())
        WalletUtils.walletIssuerDID = customDidService.generateDidKey()
        return WalletUtils.walletIssuerDID
    }

}