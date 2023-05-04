package es.in2.wallet.services

import com.nimbusds.jose.JWSObject
import es.in2.wallet.OPEN_ID_PREFIX
import es.in2.wallet.domain.CustomSiopAuthenticationRequestParser
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

interface ExecuteContentService {
    fun getAuthenticationRequest(url: String): String
    fun sendAuthenticationResponse(siopAuthenticationRequest: String, vp: String): String
}

@Service
class ExecuteContentImpl(
    private val requestTokenVerificationService: RequestTokenVerificationService
) : ExecuteContentService {

    private val log: Logger = LogManager.getLogger(ExecuteContentImpl::class.java)

    override fun getAuthenticationRequest(url: String): String {
        // Get RequestToken that contains the SIOP Authentication Request
        val requestToken = getSiopAuthenticationRequest(url)
        // validate the RequestToken
        requestTokenVerificationService.verifyRequestToken(requestToken)
        // extract siop_authentication_requests
        val jwsObject = JWSObject.parse(requestToken)
        return jwsObject.payload.toJSONObject()["auth_request"].toString()
    }

    private fun getSiopAuthenticationRequest(url: String): String {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() != 200) {
            throw Exception("Request cannot be completed. HttpStatus response ${response.get().statusCode()}")
        }
        return response.get().body()
    }

    override fun sendAuthenticationResponse(siopAuthenticationRequest: String, vp: String): String {
        // TODO need to validate this business logic
        // Parse de String SIOP Authentication Response to a readable JSON Object
        val contentOfSiopAuthRequest = siopAuthenticationRequest.replace(OPEN_ID_PREFIX, "")

        val parsedSiopAuthenticationRequest = CustomSiopAuthenticationRequestParser().parse(contentOfSiopAuthRequest)

            //ObjectMapper().readValue(siopAuthenticationRequest, CustomSiopAuthenticationRequest::class.java)
        // Add the basic formData to execute de /api/verifier/siop-sessions
        // I am awareness that we need to implement a better function which adds the presentation submission.
        // Same case for 'scope' attribute


        val state = checkIfStateIsBlank(
            parsedSiopAuthenticationRequest.getState())
        val redirectUri = checkIfRedirectUriIsBlank(
            parsedSiopAuthenticationRequest.getRedirectUri())

        val formData = "state=$state}" +
                "&vp_token=$vp" +
                "&presentation_submission=null"
        // execute the Post request
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .headers("Content-Type", "application/x-www-form-urlencoded")
            .uri(URI.create(redirectUri))
            .POST(HttpRequest.BodyPublishers.ofString(formData))
            .build()

        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() != 200) {
            throw Exception("Request cannot be completed. HttpStatus response ${response.get().statusCode()}")
        }
        // access_token returned
        return response.get().body()
    }

    private fun checkIfStateIsBlank(state:String): String {
        if(state.isBlank()) {
            throw IllegalArgumentException("Missing 'state' parameter")
        } else {
            return state
        }
    }

    private fun checkIfRedirectUriIsBlank(redirectUri:String): String {
        if(redirectUri.isBlank()) {
            throw IllegalArgumentException("Missing 'redirect_uri' parameter")
        } else {
            return redirectUri
        }
    }

}
