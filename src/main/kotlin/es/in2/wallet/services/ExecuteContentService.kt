package es.in2.wallet.services

import es.in2.wallet.WalletProperties
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

interface ExecuteContentService {
    fun getAuthenticationRequest(url: String): String

    fun sendAuthenticationResponse(state: String, vp: String): String
}

@Service
class ExecuteContentImpl(
    private val walletProperties: WalletProperties
) : ExecuteContentService {

    override fun getAuthenticationRequest(url: String): String {
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

    // TODO
    override fun sendAuthenticationResponse(state: String, vp: String): String {

        val formData = "state=$state" +
                "&vp_token=$vp" +
                "&presentation_submission=" +
                "%7B%22definition_id%22%3A%22" +
                "CustomerPresentationDefinition%22%2C%22" +
                "descriptor_map%22%3A%5B%7B%22" +
                "format%22%3A%22" +
                "jwt_vp%22%2C%22id%22%3A%22" +
                "id_credential%22%2C%22" +
                "path%22%3A%22%24%22%2C%22" +
                "path_nested%22%3A%7B%22" +
                "format%22%3A%22" +
                "jwt_vc%22%2C%22path%22%3A%22%24.verifiableCredential%5B0%5D%22%7D%7D%5D%2C%22id%22%3A%22" +
                "CustomerPresentationSubmission%22%7D&id_token=ADASD"
        val endpointUrl = "${walletProperties.domeBackendBaseURL}/relying-party/siop-sessions"

        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .headers("Content-Type", "application/x-www-form-urlencoded")
            .uri(URI.create(endpointUrl))
            .POST(HttpRequest.BodyPublishers.ofString(formData))
            .build()
        println("Form data: $formData")
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() != 200) {
            throw Exception("Request cannot be completed. HttpStatus response ${response.get().statusCode()}")
        }
        return response.get().body()
    }


}
