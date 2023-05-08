package es.in2.wallet.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jwt.SignedJWT
import es.in2.wallet.ISSUER_TOKEN_PROPERTY_NAME
import es.in2.wallet.UNIVERSAL_RESOLVER_URL
import es.in2.wallet.exceptions.DidVerificationException
import es.in2.wallet.exceptions.RequestTokenException
import es.in2.wallet.exceptions.VerificationException
import id.walt.services.did.DidService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.interfaces.ECPublicKey
import java.text.ParseException

fun interface RequestTokenVerificationService {
    fun verifyRequestToken(requestToken: String)
}

@Service
class RequestTokenVerificationServiceImpl : RequestTokenVerificationService {

    private val log: Logger = LogManager.getLogger(RequestTokenVerificationServiceImpl::class.java)

    override fun verifyRequestToken(requestToken: String) {
        log.info("RequestTokenVerificationServiceImpl - verifyRequestToken()")
        val signedJWTResponse = parseRequestTokenToSignedJwt(requestToken)
        // resolveDID() implements a new business logic that, if Universal Resolver is down,
        // we resolve the DID value using the SSI Kit. It only works with did:key methods
        val didDocument = resolveDID(signedJWTResponse.payload)
        val ecPublicKey = generateEcPublicKeyFromDidDocument(didDocument)
        val verifier = verifySignedJwtWithPublicEcKey(ecPublicKey)
        checkJWSVerifierResponse(signedJWTResponse, verifier)
    }

    private fun parseRequestTokenToSignedJwt(requestToken: String): SignedJWT {
        log.info("RequestTokenVerificationServiceImpl - parseRequestTokenToSignedJwt()")
        try {
            return SignedJWT.parse(requestToken)
        } catch (e: ParseException) {
            throw RequestTokenException("The 'request_token' has an invalid format")
        }
    }

    private fun resolveDID(payload: Payload): JsonNode {
        log.info("RequestTokenVerificationServiceImpl - resolveDID()")

        val issuerDID: String = payload.toJSONObject()[ISSUER_TOKEN_PROPERTY_NAME].toString()
        log.info("issuer_did = $issuerDID")

        try {
            log.info("Resolving DID using Universal Resolver")
            val url = "$UNIVERSAL_RESOLVER_URL/$issuerDID"
            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers("Content-Type", "application/json")
                .GET()
                .build()
            val response = HttpClient.newBuilder()
                .build()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            if (response.get().statusCode() != 200) {
                log.error("Request cannot be completed. HttpStatus response ${response.get().statusCode()}")
                throw Exception("Request cannot be completed. HttpStatus response ${response.get().statusCode()}")
            }
            val result = response.get().body()
            if (result.isNotBlank()) {
                log.debug("didDocument = $result")
                return ObjectMapper().readTree(result)["didDocument"]
            } else {
                log.error("The DID $issuerDID is not in the Trusted Participant List")
                throw DidVerificationException("The DID $issuerDID is not in the Trusted Participant List")
            }
        } catch(e: Exception) {
            log.info("Resolving DID using SSI Kit")
            val didDocument = DidService.resolve(issuerDID)
            log.debug("didDocument = ${didDocument.encodePretty()}")
            return ObjectMapper().readTree(didDocument.encodePretty())
        }

    }

    private fun generateEcPublicKeyFromDidDocument(didDocument: JsonNode): ECPublicKey {
        log.info("RequestTokenVerificationServiceImpl - generateEcPublicKeyFromDidDocument()")
        val verificationMethod = didDocument["verificationMethod"]
        val verificationMethodIndex0 = verificationMethod[0]
        val publicKeyJwk = verificationMethodIndex0["publicKeyJwk"].toString()
        return ECKey.parse(publicKeyJwk).toECPublicKey()
    }

    private fun verifySignedJwtWithPublicEcKey(ecPublicKey: ECPublicKey): JWSVerifier {
        log.info("RequestTokenVerificationServiceImpl - verifySignedJwtWithPublicEcKey()")
        try {
            return ECDSAVerifier(ecPublicKey)
        } catch (e: JOSEException) {
            throw VerificationException("We have not been able to verify the 'request_token'")
        }
    }

    private fun checkJWSVerifierResponse(signedJWTResponse: SignedJWT, verifier: JWSVerifier) {
        log.info("RequestTokenVerificationServiceImpl - checkJWSVerifierResponse()")
        if (!signedJWTResponse.verify(verifier)) {
            throw RequestTokenException("The 'request_token' is not valid")
        }
    }

}
