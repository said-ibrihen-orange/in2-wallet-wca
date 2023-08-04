package es.in2.wallet.service.impl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import es.in2.wallet.exception.FailedCommunicationException
import es.in2.wallet.service.PersonalDataSpaceService
import es.in2.wallet.service.VerifiableCredentialService
import es.in2.wallet.util.*
import es.in2.wallet.util.ApplicationUtils.buildFormUrlEncodedRequestBody
import es.in2.wallet.util.ApplicationUtils.checkResponseStatus
import es.in2.wallet.util.ApplicationUtils.httpRequestBuilder
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springdoc.core.service.GenericResponseService
import org.springframework.stereotype.Service
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service
class VerifiableCredentialServiceImpl(
    private val personalDataSpaceService: PersonalDataSpaceService,
    private val response: GenericResponseService
) : VerifiableCredentialService {

    private val log: Logger = LogManager.getLogger(VerifiableCredentialServiceImpl::class.java)

    override fun getVerifiableCredential(credentialOfferUriExtended: String) {
        val credentialOfferUri = getCredentialOfferUri(credentialOfferUriExtended)
        val credentialOffer: JsonNode = ObjectMapper().readTree(getCredentialOffer(credentialOfferUri))
        val credentialIssuerMetadataUri = getCredentialIssuerMetadataUri(credentialOffer)
        val credentialIssuerMetadata = getCredentialIssuerMetadata(credentialIssuerMetadataUri)
        val accessToken = getAccessToken(credentialOffer, credentialIssuerMetadata)
        val verifiableCredential = getVerifiableCredential(accessToken, credentialOffer, credentialIssuerMetadata)
        personalDataSpaceService.saveVC(verifiableCredential)
    }

    /**
     * @param credentialOfferUriExtended:
     *  Example of Credential Offer URI for Pre-Authorized Code Flow using DOME standard:
     *  https://www.goodair.com/credential-offer?credential_offer_uri=
     *  https://www.goodair.com/credential-offer/5j349k3e3n23j
     */
    private fun getCredentialOfferUri(credentialOfferUriExtended: String): String {
        val splitCredentialOfferUri = credentialOfferUriExtended.split("=")
        val credentialOfferUriValue = splitCredentialOfferUri[1]
        log.debug("Credential offer URI: {}", credentialOfferUriValue)
        return credentialOfferUriValue
    }

    private fun getCredentialOffer(credentialOfferUri: String): String {
        val credentialOffer = executeGetRequest(credentialOfferUri)
        log.debug("Credential offer: $credentialOffer")
        return credentialOffer
    }

    /**
     * Generate dynamic URL to get the credential_issuer_metadata
     */
    private fun getCredentialIssuerMetadataUri(credentialOffer: JsonNode): String {
        return credentialOffer["credential_issuer"].asText() + "/.well-known/openid-credential-issuer"
    }

    private fun getCredentialIssuerMetadata(credentialIssuerMetadataUri: String): JsonNode {
        val credentialIssuerMetadata =
            ObjectMapper().readTree(executeGetRequest(credentialIssuerMetadataUri))
        log.debug("Credential Issuer Metadata: {}", credentialIssuerMetadata)
        return credentialIssuerMetadata
    }

    private fun getAccessToken(credentialOffer: JsonNode, credentialIssuerMetadata: JsonNode): String {
        val tokenEndpoint = credentialIssuerMetadata["credential_token"].asText()
        val preAuthorizedCodeObject = credentialOffer["grants"][PRE_AUTH_CODE_GRANT_TYPE]
        val preAuthorizedCode = preAuthorizedCodeObject["pre-authorized_code"].asText()
        val data = mapOf("grant_type" to PRE_AUTH_CODE_GRANT_TYPE, "pre-authorized_code" to preAuthorizedCode)
        val response = ObjectMapper().readTree(executePostRequest(tokenEndpoint, data))
        val accessToken = response["access_token"].asText()
        log.debug("Access token: $accessToken")
        return accessToken
    }

    private fun getVerifiableCredential(accessToken: String, credentialOffer: JsonNode,
                                        credentialIssuerMetadata: JsonNode): String {
        val credentialType = credentialOffer["credentials"][0].asText()
        val credentialEndpoint = credentialIssuerMetadata["credential_endpoint"].asText() + credentialType
        val verifiableCredential = executePostRequestWithAccessToken(credentialEndpoint, mapOf(), accessToken)
        log.debug("Verifiable credential: {}", verifiableCredential)
        return verifiableCredential
    }

    private fun buildHttpClient(): HttpClient {
        return HttpClient.newBuilder().build()
    }

    /**
     * @see es.in2.wallet.util.ApplicationUtils.getRequest
     * @deprecated We should use the utils classes
     */
    @Deprecated("We should use the utils classes")
    private fun executeGetRequest(url: String): String {
        val client = buildHttpClient()
        val headers = listOf(CONTENT_TYPE to CONTENT_TYPE_URL_ENCODED_FORM)
        val request = httpRequestBuilder(url=url, headers=headers).GET().build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get()
        checkResponseStatus(response=response, 200..200)
        return response.body()
    }

    /**
     * @see es.in2.wallet.util.ApplicationUtils.postRequest
     * @deprecated We should use the utils classes
     */
    @Deprecated("We should use the utils classes")
    private fun executePostRequest(url: String, formDataMap: Map<String, String>): String {
        val client = buildHttpClient()
        val headers = listOf(CONTENT_TYPE to CONTENT_TYPE_URL_ENCODED_FORM)
        val request = httpRequestBuilder(url=url, headers=headers)
            .POST(buildFormUrlEncodedRequestBody(formDataMap=formDataMap)).build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get()
        checkResponseStatus(response=response, statusCodeRange = 200..200)
        return response.body()
    }

    /**
     * @see es.in2.wallet.util.ApplicationUtils.postRequest
     * @deprecated We should use the utils classes
     */
    private fun executePostRequestWithAccessToken(url: String, formDataMap: Map<String, String>, accessToken: String): String {
        val client = buildHttpClient()
        val headers= listOf(
            CONTENT_TYPE to CONTENT_TYPE_URL_ENCODED_FORM,
            HEADER_AUTHORIZATION to "Bearer $accessToken")
        val request = httpRequestBuilder(url=url, headers=headers)
            .POST(buildFormUrlEncodedRequestBody(formDataMap=formDataMap)).build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get()
        checkResponseStatus(response=response, statusCodeRange = 200..299)
        return response.body()
    }
}
