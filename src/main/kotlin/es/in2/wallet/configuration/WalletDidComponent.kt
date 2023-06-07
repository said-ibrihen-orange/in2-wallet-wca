//package es.in2.wallet.configuration
//
//import es.in2.wallet.util.WalletUtils
//import jakarta.annotation.PostConstruct
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.stereotype.Component
//
//@Component
//class WalletDidComponent {
//
//    @Value("\${walletIssuerDID}")
//    private lateinit var walletIssuerDID: String
//
//    @PostConstruct
//    fun init() {
//        val generatedValue = WalletUtils.walletIssuerDID
//    }
//
//}