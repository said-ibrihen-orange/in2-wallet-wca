package es.in2.wallet.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.InetSocketAddress
import java.net.ProxySelector

fun interface CredentialOfferService {
    fun getCredentialOffer(credentialOfferUri: String, user: String): String
}

@Service
class CredentialOfferServiceImpl(
    private val persistenceService: PersistenceService
) : CredentialOfferService {

    private val log: Logger = LogManager.getLogger(CredentialOfferServiceImpl::class.java)

    override fun getCredentialOffer(credentialOfferUri: String, user: String): String {
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
        //TODO return base64 Certificate
        return credential
    }

    private fun getCredentialIssuerMetadata(credentialIssuerMetadataUri: String): String {
        //val credentialOffer = ObjectMapper().readTree(responseBody)
        //val credentialIssuerMetadata = credentialOffer["credentialIssuer"].asText() +
        //            "/.well-known/openid-credential-issuer"
        return executeGetRequest(credentialIssuerMetadataUri)
    }

    private fun getCredentialToken(credentialOffer: JsonNode, tokenEndpoint: String): String {
        //val credentialOfferObject = ObjectMapper().readTree(credentialOfferBody)
        val preAuthorizedCodeObject = credentialOffer["grants"]["urn:ietf:params:oauth:grant-type:pre-authorized_code"]
        val authToken = getToken(tokenEndpoint,"urn:ietf:params:oauth:grant-type:pre-authorized_code",
            preAuthorizedCodeObject["preAuthorizedCode"].asText(), "")
        return authToken
    }

    private fun getToken(tokenEndpoint: String, grantType: String, preAuthorizedCode : String, userPin : String): String {

        //Get new Token
        //TODO send code
        val data = mapOf(
            "grant_type" to grantType,
            "pre-authorized_code" to preAuthorizedCode
        )
        var jsonBody = executePostRequest(tokenEndpoint, data);
        log.info("**** Endpoint response: " +  jsonBody);

        val jsonObject = ObjectMapper().readTree(jsonBody)
        //: AuthorizationResponse = gson.fromJson(jsonBody, AuthorizationResponse::class.java)
        log.info("**** Endpoint response [Object]: " +  jsonBody);

//        //TODO move to next endpoint
//        var token = jsonObject["access_token"].asText()
//        //Validate preauthCode
//        var response = executeGetRequest("http://localhost:8081/api/credential-offers/$preAuthorizedCode", token)
//
//        log.info("*** Credential Offers response: " +  response);
//        var responseObject = ObjectMapper().readTree(response)
//        if(preAuthorizedCode != responseObject["grants"]["urn:ietf:params:oauth:grant-type:pre-authorized_code"]["preAuthorizedCode"].asText()){
//            throw Exception("PreAuthorizedCode is not valid")
//        }

//
//        //TODO validate userPin
//
//        //Create Response

        return jsonObject["access_token"].asText()
    }

    private fun executeGetRequest(url: String): String {
        val client = HttpClient
            .newBuilder()
            //.proxy(ProxySelector.of(InetSocketAddress("localhost", 8085)))
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .headers("Content-Type", "application/x-www-form-urlencoded")
            .GET()
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() != 200) {
            throw Exception("Request cannot be completed. HttpStatus response ${response.get().statusCode()}")
        }
        return response.get().body()
    }

    private fun executePostRequest(url: String?, data: Map<String, String?>): String {

        val client = HttpClient
            .newBuilder()
            //.proxy(ProxySelector.of(InetSocketAddress("localhost", 8085)))
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .headers("Content-Type", "application/x-www-form-urlencoded")
            .POST(formData(data))
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() != 200) {
            throw Exception("Request cannot be completed. HttpStatus response ${response.get().statusCode()}")
        }
        return response.get().body()
    }

    private fun executePostRequestWithAuth(url: String?, data: Map<String, String?>, authToken: String): String {

        val client = HttpClient
            .newBuilder()
            //.proxy(ProxySelector.of(InetSocketAddress("localhost", 8085)))
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .headers("Content-Type", "application/x-www-form-urlencoded", "Authorization", "Bearer $authToken")
            .POST(formData(data))
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() !in 200..299) {
            throw Exception("Request cannot be completed. HttpStatus response ${response.get().statusCode()}")
        }
        return response.get().body()
    }

    fun String.utf8(): String = URLEncoder.encode(this, "UTF-8")

    fun formData(data: Map<String, String?>): HttpRequest.BodyPublisher? {

        val res = data.map { (k, v) -> "${(k.utf8())}=${v?.utf8()}" }
            .joinToString("&")

        return HttpRequest.BodyPublishers.ofString(res)
    }

}