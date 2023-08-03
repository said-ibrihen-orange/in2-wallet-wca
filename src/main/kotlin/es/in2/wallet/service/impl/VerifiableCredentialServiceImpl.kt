package es.in2.wallet.service.impl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import es.in2.wallet.exception.FailedCommunicationException
import es.in2.wallet.service.PersonalDataSpaceService
import es.in2.wallet.service.VerifiableCredentialService
import es.in2.wallet.util.CONTENT_TYPE
import es.in2.wallet.util.HEADER_AUTHORIZATION
import es.in2.wallet.util.PRE_AUTH_CODE_GRANT_TYPE
import es.in2.wallet.util.URL_ENCODED_FORM
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service
class VerifiableCredentialServiceImpl(
    private val personalDataSpaceService: PersonalDataSpaceService
) : VerifiableCredentialService {

    private val log: Logger = LogManager.getLogger(VerifiableCredentialServiceImpl::class.java)

    override fun getVerifiableCredential(credentialOfferUri: String) {
        /*
            Example of Credential Offer URI for Pre-Authorized Code Flow using DOME standard:
            https://www.goodair.com/credential-offer?credential_offer_uri=https://www.goodair.com/credential-offer/5j349k3e3n23j
        */
        log.debug("getVerifiableCredential")
        val splitCredentialOfferUri = credentialOfferUri.split("=")
        val credentialOfferUriValue = splitCredentialOfferUri[1]
        log.debug("Parsed credential offer URI: {}", credentialOfferUriValue)
        // get credential_offer executing the credential_offer_uri
        val credentialOffer: JsonNode = ObjectMapper().readTree(getCredentialOffer(credentialOfferUriValue))
        log.debug("Credential offer: {}", credentialOffer)

        // generate dynamic URL to get the credential_issuer_metadata
        val credentialIssuerMetadataUri =
            credentialOffer["credential_issuer"].asText() + "/.well-known/openid-credential-issuer"
        val credentialIssuerMetadataObject =
            ObjectMapper().readTree(getCredentialIssuerMetadata(credentialIssuerMetadataUri))
        log.debug("Credential issuer metadata: {}", credentialIssuerMetadataObject)

        // request access_token using credential_offer and credential_issuer_metadata claims
        val tokenEndpoint = credentialIssuerMetadataObject["credential_token"].asText()
        val accessToken = getAccessToken(credentialOffer, tokenEndpoint)
        log.debug("Access token: $accessToken")

        // request credential using the access_token received
        val credentialType = credentialOffer["credentials"][0].asText()
        val credentialEndpoint = credentialIssuerMetadataObject["credential_endpoint"].asText() + credentialType
        val verifiableCredential = executePostRequestWithAccessToken(credentialEndpoint, mapOf(), accessToken)
        log.debug("Verifiable credential: {}", verifiableCredential)

        // stores the received credential in the user Personal Data Space
        personalDataSpaceService.saveVC(verifiableCredential)
    }

    private fun getCredentialOffer(credentialOfferUri: String): String {
        return executeGetRequest(credentialOfferUri)
    }

    private fun getCredentialIssuerMetadata(credentialIssuerMetadataUri: String): String {
        return executeGetRequest(credentialIssuerMetadataUri)
    }

    private fun getAccessToken(credentialOffer: JsonNode, tokenEndpoint: String): String {
        // prepare data to the POST request
        val preAuthorizedCodeObject = credentialOffer["grants"][PRE_AUTH_CODE_GRANT_TYPE]
        val preAuthorizedCode = preAuthorizedCodeObject["pre-authorized_code"].asText()
        val data = mapOf("grant_type" to PRE_AUTH_CODE_GRANT_TYPE, "pre-authorized_code" to preAuthorizedCode)
        // request POST
        val jsonBody = executePostRequest(tokenEndpoint, data)
        log.info("**** Endpoint response: $jsonBody")
        // read response
        val jsonObject = ObjectMapper().readTree(jsonBody)
        log.info("**** Endpoint response [Object]: $jsonBody")
        // return access_token as String
        return jsonObject["access_token"].asText()
    }

    private fun buildHttpClient(): HttpClient {
        return HttpClient.newBuilder().build()
    }

    private fun executeGetRequest(url: String): String {
        val client = buildHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .headers(CONTENT_TYPE, URL_ENCODED_FORM)
            .GET()
            .build()
        log.debug(request)
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() != 200) {
            throw FailedCommunicationException(
                "Request cannot be completed. HttpStatus response " +
                        "${response.get().statusCode()}"
            )
        }
        return response.get().body()
    }

    private fun executePostRequest(url: String, data: Map<String, String>): String {
        val client = buildHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .headers(CONTENT_TYPE, URL_ENCODED_FORM)
            .POST(formData(data))
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() != 200) {
            throw FailedCommunicationException(
                "Request cannot be completed. HttpStatus response ${
                    response.get().statusCode()
                }"
            )
        }
        return response.get().body()
    }

    private fun executePostRequestWithAccessToken(url: String, data: Map<String, String>, accessToken: String): String {
        val client = buildHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .headers(CONTENT_TYPE, URL_ENCODED_FORM, HEADER_AUTHORIZATION, "Bearer $accessToken")
            .POST(formData(data))
            .build()
        log.debug(request)
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() !in 200..299) {
            throw FailedCommunicationException(
                "Request cannot be completed. HttpStatus response ${
                    response.get().statusCode()
                }"
            )
        }
        return response.get().body()
    }

    private fun String.utf8(): String = URLEncoder.encode(this, "UTF-8")

    private fun formData(data: Map<String, String?>): HttpRequest.BodyPublisher? {
        val res = data.map { (k, v) -> "${(k.utf8())}=${v?.utf8()}" }
            .joinToString("&")
        return HttpRequest.BodyPublishers.ofString(res)
    }

}
