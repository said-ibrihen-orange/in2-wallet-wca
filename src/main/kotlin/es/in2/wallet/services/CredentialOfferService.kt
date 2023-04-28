package es.in2.wallet.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun interface CredentialOfferService {
    fun getCredentialOffer(credentialOfferUri: String): String
}

@Service
class CredentialOfferServiceImpl() : CredentialOfferService {

    private val log: Logger = LogManager.getLogger(CredentialOfferServiceImpl::class.java)

    override fun getCredentialOffer(credentialOfferUri: String): String {
        val credentialOffer = executeGetRequest(credentialOfferUri)
        log.info("Credential Offer: $credentialOffer")
        val credentialIssuerMetadata = getCredentialIssuerMetadata(credentialOffer)
        log.info("Credential Issuer Metadata: $credentialIssuerMetadata")
        return credentialIssuerMetadata
    }

    private fun getCredentialIssuerMetadata(responseBody: String): String {
        val credentialOffer = ObjectMapper().readTree(responseBody)
        val credentialIssuerMetadata = credentialOffer["credentialIssuer"].asText() +
                    "/.well-known/openid-credential-issuer"
        return executeGetRequest(credentialIssuerMetadata)
    }

    private fun executeGetRequest(url: String): String {
        val client = HttpClient.newBuilder().build()
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

}