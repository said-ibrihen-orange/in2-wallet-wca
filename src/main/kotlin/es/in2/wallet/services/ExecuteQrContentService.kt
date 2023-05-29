package es.in2.wallet.services

import com.fasterxml.jackson.annotation.JsonProperty
import com.nimbusds.jose.JWSObject
import es.in2.wallet.JWT
import es.in2.wallet.OPEN_ID_PREFIX
import es.in2.wallet.exceptions.NoSuchQrContentException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import org.json.JSONObject

interface ExecuteQrContentService {
    fun executeQR(username:String,contentQR: String): Any
    fun getAuthenticationRequest(url: String): AuthRequestContent
    fun sendAuthenticationResponse( siopAuthenticationRequest: String, vp: String): String

    fun executeVP(username: String,vps: List<String>, siopAuthenticationRequest: String): String
}

@Service
class ExecuteQrContentImpl(
    private val requestTokenVerificationService: RequestTokenVerificationService,
    private val persistenceService: PersistenceService,
    private val credentialOfferService: CredentialOfferService,
    private val siopVerifiablePresentationService: SiopVerifiablePresentationService
) : ExecuteQrContentService {

    private val log: Logger = LogManager.getLogger(ExecuteQrContentImpl::class.java)
    enum class QRType {
        LOGIN_URL,
        AUTH_REQUEST,
        VC_URL,
        VC_CONTENT,
        UNKNOWN
    }
    private fun checkQRType(content: String): QRType {
        val loginUrlRegex = Regex("(https|http).*?(authentication-request|authentication-requests).*")
        val authRequestRegex = Regex("openid://.*")
        val vcUrlRegex = Regex("(https|http).*?(credential-offer|credential-offers).*")
        val vcContentRegex = Regex("ey.*")
        println("Matches loginUrlRegex: ${loginUrlRegex.matches(content)}")
        return when {
            loginUrlRegex.matches(content) -> QRType.LOGIN_URL
            authRequestRegex.matches(content) -> QRType.AUTH_REQUEST
            vcUrlRegex.matches(content) -> QRType.VC_URL
            vcContentRegex.matches(content) -> QRType.VC_CONTENT
            else -> QRType.UNKNOWN
        }
    }

    override fun executeQR(username: String,contentQR: String): Any {
        return when(checkQRType(contentQR)){
            QRType.LOGIN_URL -> executeLoginUrl(username,contentQR)
            QRType.AUTH_REQUEST -> executeSiopAuthRequest(username,contentQR)
            QRType.VC_URL -> executeCredentialOfferUri(username,contentQR)
            QRType.VC_CONTENT -> saveVcJwtInContextBroker(username,contentQR)
            QRType.UNKNOWN -> NoSuchQrContentException("Unknown QR Type")
        }
    }
    private fun executeLoginUrl(username: String,loginRequestUrl: String): ArrayList<String> {
        log.info("ExecuteContentImpl - executeLoginUrl() - contentQR: $loginRequestUrl")
        val siopAuthRequest = this.getAuthenticationRequest(loginRequestUrl).authRequest
        return this.executeSiopAuthRequest(username,siopAuthRequest)
    }

    /**
     * This method is used to execute the authentication request
     * @param contentQR
     * @return VC
     * Return a collections of VC that the scope this the sama type of the vc
     */
    private fun executeSiopAuthRequest(username: String,siopAuthenticationRequest: String): ArrayList<String> {
        log.info("ExecuteContentImpl - executeAuthRequest() - contentQR: $siopAuthenticationRequest")
        // Get the scope from the authRequest
        val scopeRegex = Regex("scope=\\[([^]]+)]")
        val scope = scopeRegex.find(siopAuthenticationRequest)
        val listCredentialType = scope!!.groupValues[1].split(",")
        val result = arrayListOf<String>()
        result.add(siopAuthenticationRequest)
        val vcs = persistenceService.getVCsByVCTypes(username, listCredentialType)
        for (vc in vcs){
            result.add(vc)
        }
        return result
    }

    private fun executeCredentialOfferUri(username: String,credentialOfferUri: String){
        log.info("ExecuteContentImpl - executeVCUrl() - contentQR: $credentialOfferUri")
        credentialOfferService.getCredentialOffer(credentialOfferUri, username)
    }
    private fun saveVcJwtInContextBroker(username: String,vcJWT: String){
        log.info("ExecuteContentImpl - executeVCContent() - contentQR: $vcJWT")
        persistenceService.saveVC(vcJWT, username)
    }

    override fun executeVP(username: String,vps: List<String>, siopAuthenticationRequest: String): String {
        log.info("building Verifiable Presentation")
        val verifiableCredentials = ArrayList<String>()
        for (vp in vps){
            val tmp = persistenceService.getVCByFormat(username,vp,"vc_jwt")
            val vc = JSONObject(tmp)
            val token = vc.getJSONObject("vc").getString("value")
            verifiableCredentials.add(token)
        }
        val vp = siopVerifiablePresentationService.createVerifiablePresentation(
            verifiableCredentials, JWT
        )
        log.info("executing the post Authentication Response ")
        // send the verifiable presentation to the dome backend
        return this.sendAuthenticationResponse(
            siopAuthenticationRequest, vp
        )
    }

    override fun getAuthenticationRequest(url: String): AuthRequestContent {
        log.info("ExecuteContentImpl - getAuthenticationRequest() - URL: $url")
        // Get RequestToken that contains the SIOP Authentication Request
        val requestToken = getSiopAuthenticationRequest(url)
        // validate the RequestToken
        requestTokenVerificationService.verifyRequestToken(requestToken)
        // extract siop_authentication_requests
        val jwsObject = JWSObject.parse(requestToken)
        return AuthRequestContent(
            jwsObject.payload.toJSONObject()["auth_request"].toString()
        )
    }

    private fun getSiopAuthenticationRequest(url: String): String {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() != 200) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST,"Request cannot be completed. HttpStatus response ${response.get().statusCode()}")
        }
        return response.get().body()
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
            throw ResponseStatusException(HttpStatus.MULTI_STATUS,"Request cannot be completed. HttpStatus response ${response.get().statusCode()}")
        }
        // access_token returned
        return response.get().body()
    }

}


class AuthRequestContent(
    @JsonProperty("auth_request") val authRequest: String
)
