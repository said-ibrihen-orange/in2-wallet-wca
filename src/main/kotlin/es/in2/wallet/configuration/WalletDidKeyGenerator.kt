package es.in2.wallet.configuration

class WalletDidKeyGenerator(private val didKey: String) {

    fun getDidKey(): String {
        return didKey
    }

}