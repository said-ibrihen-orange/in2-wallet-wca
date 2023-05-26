package es.in2.wallet.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import es.in2.wallet.CONTENT_TYPE
import es.in2.wallet.HEADER_AUTHORIZATION
import es.in2.wallet.PRE_AUTH_CODE_GRANT_TYPE
import es.in2.wallet.URL_ENCODED_FORM
import es.in2.wallet.exceptions.FailedCommunicationException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun interface CredentialOfferService {
    fun getCredentialOffer(credentialOfferUri: String, user: String)
}

@Service
class CredentialOfferServiceImpl(
    private val persistenceService: PersistenceService
) : CredentialOfferService {

    private val log: Logger = LogManager.getLogger(CredentialOfferServiceImpl::class.java)

    override fun getCredentialOffer(credentialOfferUri: String, user: String) {
        //1. Call credentialOfferUri, save credentialIssuer and preauth code
        val credentialOffer = ObjectMapper().readTree(executeGetRequest(credentialOfferUri))
        log.info("Credential Offer: $credentialOffer")
        val credentialIssuerMetadataUri = credentialOffer["credentialIssuer"].asText() +
                "/.well-known/openid-credential-issuer"
        val credentialIssuerMetadataObject = ObjectMapper().readTree(getCredentialIssuerMetadata(credentialIssuerMetadataUri))
        //2. Call credentialIssuerMetadata from previous response credentialsIssuer field
        log.info("Credential Issuer Metadata: $credentialIssuerMetadataObject")
        val tokenEndpoint = credentialIssuerMetadataObject["credentialToken"].asText()
        //3. Call credentialToken from previous response credentialIssuer.grants.preAuthorizedCode
        val authToken = getCredentialToken(credentialOffer, tokenEndpoint)
        log.info("Credential Token: $authToken")
        //4. Call Credential Endpoint with authToken
        //  4.1. Get credentialType asked for, we get the first one TODO iterate in case multiple credentials are asked
        val credentialType = credentialOffer["credentials"][0].asText()
        val credentialEndpoint = credentialIssuerMetadataObject["credentialEndpoint"].asText() + credentialType
        val credential = executePostRequestWithAuth(credentialEndpoint, mapOf() ,authToken)
        //5. Save credential into wallet DB memory table
        persistenceService.saveVC(credential, user)
    }

    private fun getCredentialIssuerMetadata(credentialIssuerMetadataUri: String): String {
        return executeGetRequest(credentialIssuerMetadataUri)
    }

    private fun getCredentialToken(credentialOffer: JsonNode, tokenEndpoint: String): String {
        val preAuthorizedCodeObject = credentialOffer["grants"][PRE_AUTH_CODE_GRANT_TYPE]
        return getToken(tokenEndpoint, preAuthorizedCodeObject["preAuthorizedCode"].asText(), "")
    }

    private fun getToken(tokenEndpoint: String, preAuthorizedCode : String,
                         userPin : String): String {

        //Get new Token
        //TODO send code
        val data = mapOf(
            "grant_type" to PRE_AUTH_CODE_GRANT_TYPE,
            "pre-authorized_code" to preAuthorizedCode
        )
        val jsonBody = executePostRequest(tokenEndpoint, data);
        log.info("**** Endpoint response: $jsonBody");

        val jsonObject = ObjectMapper().readTree(jsonBody)
        //: AuthorizationResponse = gson.fromJson(jsonBody, AuthorizationResponse::class.java)
        log.info("**** Endpoint response [Object]: $jsonBody");

        // TODO validate userPin
        log.info(userPin)

        return jsonObject["access_token"].asText()
    }

    private fun executeGetRequest(url: String): String {
        val client = HttpClient
            .newBuilder()
            //.proxy(ProxySelector.of(InetSocketAddress("localhost", 8085)))
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .headers(CONTENT_TYPE, URL_ENCODED_FORM)
            .GET()
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() != 200) {
            throw FailedCommunicationException("Request cannot be completed. HttpStatus response ${response.get().statusCode()}")
        }
        return response.get().body()
    }

    private fun executePostRequest(url: String, data: Map<String, String>): String {
        val client = HttpClient
            .newBuilder()
            //.proxy(ProxySelector.of(InetSocketAddress("localhost", 8085)))
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .headers(CONTENT_TYPE, URL_ENCODED_FORM)
            .POST(formData(data))
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() != 200) {
            throw FailedCommunicationException("Request cannot be completed. HttpStatus response ${response.get().statusCode()}")
        }
        return response.get().body()
    }

    private fun executePostRequestWithAuth(url: String, data: Map<String, String>, authToken: String): String {

        val client = HttpClient
            .newBuilder()
            //.proxy(ProxySelector.of(InetSocketAddress("localhost", 8085)))
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .headers(CONTENT_TYPE, URL_ENCODED_FORM,  HEADER_AUTHORIZATION, "Bearer $authToken")
            .POST(formData(data))
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() !in 200..299) {
            throw FailedCommunicationException("Request cannot be completed. HttpStatus response ${response.get().statusCode()}")
        }
        return response.get().body()
    }

    fun String.utf8(): String = URLEncoder.encode(this,"UTF-8")

    fun formData(data: Map<String, String?>): HttpRequest.BodyPublisher? {
        val res = data.map { (k, v) -> "${(k.utf8())}=${v?.utf8()}" }
            .joinToString("&")
        return HttpRequest.BodyPublishers.ofString(res)
    }

}