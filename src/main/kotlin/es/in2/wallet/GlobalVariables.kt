package es.in2.wallet

import org.springframework.beans.factory.annotation.Value

class GlobalVariables()

const val JWT = "JWT"
const val SERVICE_MATRIX = "service-matrix.properties"
const val OPEN_ID_PREFIX = "openid://"
const val RESPONSE_MODE = "direct_post"
const val RESPONSE_TYPE = "vp_token"
// pass that attribute to the properties attribute
const val REDIRECT_URI = "https://domeapidev.in2.es/relying-party/siop-sessions" //"http://localhost:8080/siop-sessions"
const val SIOP_AUDIENCE = "https://self-issued.me/v2"
const val AUTH_REQUEST_CLAIM_NAME = "auth_request"
const val PEM_PUBLIC_KEY_PREFIX = "-----BEGIN PUBLIC KEY-----"
const val PEM_PUBLIC_KEY_SUFFIX = "-----END PUBLIC KEY-----"
const val CUSTOMER_CREDENTIAL_TYPE = "CustomerCredential"
const val PROVIDER_CREDENTIAL_TYPE = "ProviderCredential"
const val PROOF_TYPE_PROPERTY_NAME = "ProofType"
const val ISSUER_TOKEN_PROPERTY_NAME = "iss"
const val VERIFIABLE_CREDENTIAL_PROPERTY_NAME = "verifiableCredential"
const val UNIVERSAL_RESOLVER_URL = "https://dev.uniresolver.io/1.0/identifiers"

const val FIWARE_URL = "http://localhost:1026"




