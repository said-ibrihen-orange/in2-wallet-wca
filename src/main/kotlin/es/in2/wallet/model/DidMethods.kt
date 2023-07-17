package es.in2.wallet.model

enum class DidMethods {
    DID_KEY {
        override fun toString(): String {
            return "DID_KEY"
        }
    },
    DID_EBSI {
        override fun toString(): String {
            return "DID_EBSI"
        }
    },
    DID_ELSI {
        override fun toString(): String {
            return "DID_ELSI"
        }
    }
}
