package es.in2.wallet.util

object WalletUtils {
    var walletIssuerDID: String = ""
}

const val SERVICE_MATRIX = "service-matrix.properties"
const val SIOP_AUDIENCE = "https://self-issued.me/v2"
const val USER_ROLE = "USER"
const val JWT = "JWT"
const val OPEN_ID_PREFIX = "openid://"
const val ISSUER_TOKEN_PROPERTY_NAME = "iss"
const val UNIVERSAL_RESOLVER_URL = "https://dev.uniresolver.io/1.0/identifiers"
const val PRE_AUTH_CODE_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:pre-authorized_code"
const val CONTENT_TYPE = "Content-Type"
const val URL_ENCODED_FORM = "application/x-www-form-urlencoded"
const val HEADER_AUTHORIZATION = "Authorization"
const val BEARER_PREFIX = "Bearer "
const val ALL = "*"