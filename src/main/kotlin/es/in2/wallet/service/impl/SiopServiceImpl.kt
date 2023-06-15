package es.in2.wallet.service.impl

import com.nimbusds.jose.JWSObject
import es.in2.wallet.model.dto.VcSelectorRequestDTO
import es.in2.wallet.model.dto.VcSelectorResponseDTO
import es.in2.wallet.service.PersonalDataSpaceService
import es.in2.wallet.service.SiopService
import es.in2.wallet.service.TokenVerificationService
import es.in2.wallet.util.ApplicationUtils
import es.in2.wallet.util.URL_ENCODED_FORM
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service

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
    override fun getSiopAuthenticationRequest(siopAuthenticationRequestUri: String): VcSelectorRequestDTO {
        log.info("SiopServiceImpl.getSiopAuthenticationRequest()")

        // get SIOP Authentication Request as JWT in JWS form by executing the received URI
        val jwtSiopAuthRequest = getSiopAuthenticationRequestInJwsFormat(siopAuthenticationRequestUri)

        // verify the received response
        tokenVerificationService.verifySiopAuthRequestAsJwsFormat(jwtSiopAuthRequest)

        // Extract "auth_request" of the JWT which contains the SIOP Authentication Request
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
    override fun processSiopAuthenticationRequest(siopAuthenticationRequest: String): VcSelectorRequestDTO {

        val parsedSiopAuthenticationRequest = ApplicationUtils.parseOpenIdConfig(siopAuthenticationRequest)

        // Extract the scope claim of the SIOP Authentication Request
        val scopeList = extractScopeClaimOfTheSiopAuthRequest(siopAuthenticationRequest)

        // Find if User has a Verifiable Credential that matches with all the scopes requested
        val selectableVcList = personalDataSpaceService.getSelectableVCsByVcTypeList(scopeList)

        // Populate the response to the Wallet Front-End adding the SIOP Authentication Request and a List of the
        // Verifiable Credential IDs that match with the requested scope
        return VcSelectorRequestDTO(
            redirectUri = parsedSiopAuthenticationRequest.redirectUri,
            state = parsedSiopAuthenticationRequest.state,
            selectableVcList = selectableVcList
        )
    }

    override fun sendAuthenticationResponse(vcSelectorResponseDTO: VcSelectorResponseDTO, vp: String): String {

        // Parse de String SIOP Authentication Response to a readable JSON Object
        val formData = "state=${vcSelectorResponseDTO.state}" +
                "&vp_token=$vp" +
                "&presentation_submission={\"definition_id\": \"CustomerPresentationDefinition\", " +
                "\"descriptor_map\": [{\"format\": \"vp_jwt\", \"id\": \"id_credential\", \"path\": \"\$\", " +
                "\"path_nested\": {\"format\": \"vc_jwt\", \"path\": \"\$.verifiableCredential[0]\"}}], " +
                "\"id\": \"CustomerPresentationSubmission\"}"
        log.info(vcSelectorResponseDTO.redirectUri)
        log.info(formData)

        val response = ApplicationUtils.postRequest(vcSelectorResponseDTO.redirectUri, formData, URL_ENCODED_FORM)
        log.info("response body = {}", response)
        // access_token returned
        return response
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