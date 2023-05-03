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

    override fun verifyRequestToken(requestToken: String) {
        val signedJWTResponse = parseRequestTokenToSignedJwt(requestToken)
        val didDocument = checkIfDidIsInTheTrustedParticipantList(signedJWTResponse.payload)
        val ecPublicKey = generateEcPublicKeyFromDidDocument(didDocument)
        val verifier = verifySignedJwtWithPublicEcKey(ecPublicKey)
        checkJWSVerifierResponse(signedJWTResponse, verifier)
    }

    private fun parseRequestTokenToSignedJwt(requestToken: String): SignedJWT {
        try {
            return SignedJWT.parse(requestToken)
        } catch (e: ParseException) {
            throw RequestTokenException("The 'request_token' has an invalid format")
        }
    }

    private fun checkIfDidIsInTheTrustedParticipantList(payload: Payload): JsonNode {
        val issuerDID: String = payload.toJSONObject()[ISSUER_TOKEN_PROPERTY_NAME].toString()
        println(issuerDID)
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$UNIVERSAL_RESOLVER_URL/$issuerDID"))
            .headers("Content-Type", "application/x-www-form-urlencoded")
            .GET()
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() != 201 && response.get().statusCode() != 200) {
            throw Exception("Request cannot be completed. HttpStatus response ${response.get().statusCode()}")
        }
        val result = response.get().body()
        if (result.isNotBlank()) {
            return ObjectMapper().readTree(result)
        } else {
            throw DidVerificationException("The DID $issuerDID is not in the Trusted Participant List")
        }
    }

    private fun generateEcPublicKeyFromDidDocument(didDocument: JsonNode): ECPublicKey {
        val verificationMethod = didDocument["verificationMethod"]
        val verificationMethodIndex0 = verificationMethod[0]
        val publicKeyJwk = verificationMethodIndex0["publicKeyJwk"].toString()
        return ECKey.parse(publicKeyJwk).toECPublicKey()
    }

    private fun verifySignedJwtWithPublicEcKey(ecPublicKey: ECPublicKey): JWSVerifier {
        try {
            return ECDSAVerifier(ecPublicKey)
        } catch (e: JOSEException) {
            throw VerificationException("We have not been able to verify the 'request_token'")
        }
    }

    private fun checkJWSVerifierResponse(signedJWTResponse: SignedJWT, verifier: JWSVerifier) {
        if (!signedJWTResponse.verify(verifier)) {
            throw RequestTokenException("The 'request_token' is not valid")
        }
    }

}
