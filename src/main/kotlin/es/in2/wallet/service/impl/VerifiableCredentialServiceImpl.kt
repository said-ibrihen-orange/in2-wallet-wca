package es.in2.wallet.service.impl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import es.in2.wallet.service.PersonalDataSpaceService
import es.in2.wallet.service.VerifiableCredentialService
import es.in2.wallet.util.*
import es.in2.wallet.util.ApplicationUtils.buildUrlEncodedFormDataRequestBody
import es.in2.wallet.util.ApplicationUtils.getRequest
import es.in2.wallet.util.ApplicationUtils.postRequest
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springdoc.core.service.GenericResponseService
import org.springframework.stereotype.Service

@Service
class VerifiableCredentialServiceImpl(
    private val personalDataSpaceService: PersonalDataSpaceService
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
        val headers = listOf(CONTENT_TYPE to CONTENT_TYPE_URL_ENCODED_FORM)
        val credentialOffer = getRequest(url=credentialOfferUri, headers=headers)
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
        val headers = listOf(CONTENT_TYPE to CONTENT_TYPE_URL_ENCODED_FORM)
        val credentialIssuerMetadata =
            ObjectMapper().readTree(getRequest(url=credentialIssuerMetadataUri, headers=headers))
        log.debug("Credential Issuer Metadata: {}", credentialIssuerMetadata)
        return credentialIssuerMetadata
    }

    private fun getAccessToken(credentialOffer: JsonNode, credentialIssuerMetadata: JsonNode): String {
        val tokenEndpoint = credentialIssuerMetadata["credential_token"].asText()
        val preAuthorizedCodeObject = credentialOffer["grants"][PRE_AUTH_CODE_GRANT_TYPE]
        val preAuthorizedCode = preAuthorizedCodeObject["pre-authorized_code"].asText()
        val headers = listOf(CONTENT_TYPE to CONTENT_TYPE_URL_ENCODED_FORM)
        val formData = mapOf("grant_type" to PRE_AUTH_CODE_GRANT_TYPE, "pre-authorized_code" to preAuthorizedCode)
        val body = buildUrlEncodedFormDataRequestBody(formDataMap=formData)
        val response = postRequest(url=tokenEndpoint, headers=headers, body=body)
        val accessTokenJson: JsonNode = ObjectMapper().readTree(response)
        val accessToken = accessTokenJson["access_token"].asText()
        log.debug("Access token: $accessToken")
        return accessToken
    }

    private fun getVerifiableCredential(accessToken: String, credentialOffer: JsonNode,
                                        credentialIssuerMetadata: JsonNode): String {
        val credentialType = credentialOffer["credentials"][0].asText()
        val credentialEndpoint = credentialIssuerMetadata["credential_endpoint"].asText() + credentialType
        val headers = listOf(
            CONTENT_TYPE to CONTENT_TYPE_URL_ENCODED_FORM,
            HEADER_AUTHORIZATION to "Bearer $accessToken")
        val verifiableCredential = postRequest(url=credentialEndpoint, headers=headers, body="")
        log.debug("Verifiable credential: {}", verifiableCredential)
        return verifiableCredential
    }
}
