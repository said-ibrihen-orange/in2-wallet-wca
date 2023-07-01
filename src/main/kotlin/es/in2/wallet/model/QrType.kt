package es.in2.wallet.model

enum class QrType {
    SIOP_AUTH_REQUEST_URI,
    SIOP_AUTH_REQUEST,
    CREDENTIAL_OFFER_URI,
    FIWARE_CREDENTIAL_OFFER_URI,
    VC_JWT,
    FIWARE_VC,
    UNKNOWN
}