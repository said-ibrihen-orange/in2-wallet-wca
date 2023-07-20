package es.in2.wallet.model.dto


data class DidRequestDTO(private val type: String, private val value: String?){

    fun getDid(): String? {
        return value
    }

    fun getType(): String {
        return type
    }
}