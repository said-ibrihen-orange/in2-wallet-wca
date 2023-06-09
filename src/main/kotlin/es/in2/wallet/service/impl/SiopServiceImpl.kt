package es.in2.wallet.service.impl

import com.nimbusds.jose.JWSObject
import es.in2.wallet.service.PersonalDataSpaceService
import es.in2.wallet.service.SiopService
import es.in2.wallet.service.TokenVerificationService
import es.in2.wallet.util.ApplicationUtils
import es.in2.wallet.util.OPEN_ID_PREFIX
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service
class SiopServiceImpl(
    private val tokenVerificationService: TokenVerificationService,
    private val personalDataSpaceService: PersonalDataSpaceService,
    private val applicationUtils: ApplicationUtils
) : SiopService {

    private val log: Logger = LogManager.getLogger(SiopServiceImpl::class.java)

    /**
     * This method executes the siop_authentication_request_uri to get a SIOP Authentication Request
     * as a JWT in JWS format.
     */
    override fun getSiopAuthenticationRequest(siopAuthenticationRequestUri: String): MutableList<String> {
        log.info("SiopServiceImpl.getSiopAuthenticationRequest()")

        // get SIOP Authentication Request as JWT in JWS form by executing the received URI
        val jwtSiopAuthRequest = getSiopAuthenticationRequestInJwsFormat(siopAuthenticationRequestUri)

        // verify the received response
        tokenVerificationService.verifySiopAuthRequestAsJwsFormat(jwtSiopAuthRequest)

        // extract "auth_request" of the JWT SIOP Authentication Request
        val siopAuthenticationRequest = getAuthRequestClaim(jwtSiopAuthRequest)

        return processSiopAuthenticationRequest(siopAuthenticationRequest)
    }

    private fun getSiopAuthenticationRequestInJwsFormat(siopAuthenticationRequestUri: String): String {
        return applicationUtils.getRequest(siopAuthenticationRequestUri)
    }

    /**
     * This method processes the received SIOP Authentication Request. This SIOP Authentication Request
     * can be received as a result of a previous method - getSiopAuthenticationRequestInJwsFormat() - or
     * as a result of the identification of QR content.
     */
    override fun processSiopAuthenticationRequest(siopAuthenticationRequest: String): MutableList<String> {

        // set up function result
        val result: MutableList<String> = mutableListOf()

        // Extract the scope claim of the SIOP Authentication Request
        val scopeList = extractScopeClaimOfTheSiopAuthRequest(siopAuthenticationRequest)

        // Find if User has a Verifiable Credential that matches with all the scopes requested
        val verifiableCredentialList = personalDataSpaceService.getVcIdListByVcTypeList(scopeList)

        // Populate the response to the Wallet Front-End adding the SIOP Authentication Request and a List of the
        // Verifiable Credential IDs that match with the requested scope
        result.add(siopAuthenticationRequest)
        verifiableCredentialList.forEach { result.add(it) }

        return result
    }

    override fun sendAuthenticationResponse(siopAuthenticationRequest: String, vp: String): String {

        // Parse de String SIOP Authentication Response to a readable JSON Object
        val contentOfSiopAuthRequest = siopAuthenticationRequest.replace(OPEN_ID_PREFIX, "")
        val parameter = contentOfSiopAuthRequest.replace("?", "").split("&")
        val state = parameter[4].replace("state=", "")
        val redirectUri = parameter[6].replace("redirect_uri=", "")
        val formData = "state=$state" +
                "&vp_token=$vp" +
                "&presentation_submission={\"definition_id\": \"CustomerPresentationDefinition\", \"descriptor_map\": [{\"format\": \"ldp_vp\", \"id\": \"id_credential\", \"path\": \"\$\", \"path_nested\": {\"format\": \"ldp_vc\", \"path\": \"\$.verifiableCredential[0]\"}}], \"id\": \"CustomerPresentationSubmission\"}"

        log.info(formData)

        // execute the Post request
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(redirectUri))
            .headers("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formData))
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())

        log.info("response = ${response.get().statusCode()}")

        if (response.get().statusCode() != 200) {
            throw ResponseStatusException(
                HttpStatus.MULTI_STATUS,
                "Request cannot be completed. HttpStatus response ${response.get().statusCode()}"
            )
        }

        // access_token returned
        return response.get().body()
    }



    private fun getAuthRequestClaim(jwtSiopAuthRequest: String): String {
        val jwsObject = JWSObject.parse(jwtSiopAuthRequest)
        return jwsObject.payload.toJSONObject()["auth_request"].toString()
    }

    private fun extractScopeClaimOfTheSiopAuthRequest(siopAuthenticationRequest: String): List<String> {
        val scopeRegex = Regex("scope=\\[([^]]+)]")
        val scope = scopeRegex.find(siopAuthenticationRequest)
        // Check if scope is null, if it is null use the default, if not, use the scope list attached
        return if (scope != null) {
            scope.groupValues[1].split(",")
        } else {
            listOf("VerifiableId")
        }
    }

}