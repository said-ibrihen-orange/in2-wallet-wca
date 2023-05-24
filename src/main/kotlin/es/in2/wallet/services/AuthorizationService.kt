package es.in2.wallet.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.InetSocketAddress
import java.net.ProxySelector
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

interface AuthorizationService {
    fun getToken(grantType: String, preAuthorizedCode : String, userPin : String): String
}

@Service
class AuthorizationServiceImpl() : AuthorizationService {

    private val log: Logger = LogManager.getLogger(AuthorizationServiceImpl::class.java)

    @Value("\${spring.security.oauth2.resourceserver.jwt.token-set-uri}")
    val issuerURI: String? = null

    @Value("\${spring.security.oauth2.resourceserver.jwt.did}")
    val did: String? = null


    override fun getToken(grantType: String, preAuthorizedCode : String, userPin : String): String {

        //Get new Token
        val data = mapOf(
            "grant_type" to grantType,
            "pre-authorized_code" to preAuthorizedCode
        )
        var jsonBody = executePostRequest(this.issuerURI, data);
        log.info("**** Endpoint response: " +  jsonBody);

        val jsonObject = ObjectMapper().readTree(jsonBody)
        //: AuthorizationResponse = gson.fromJson(jsonBody, AuthorizationResponse::class.java)
        log.info("**** Endpoint response [Object]: " +  jsonBody);

        //TODO move to next endpoint
        var token = jsonObject["access_token"].asText()
        //Validate preauthCode
        var response = executeGetRequest("http://localhost:8081/api/credential-offers/$preAuthorizedCode", token)

        log.info("*** Credential Offers response: " +  response);
       var responseObject = ObjectMapper().readTree(response)
        if(preAuthorizedCode != responseObject["grants"]["urn:ietf:params:oauth:grant-type:pre-authorized_code"]["preAuthorizedCode"].asText()){
            throw Exception("PreAuthorizedCode is not valid")
        }
//
//        //TODO validate userPin
//
//        //Create Response

        return response
            //jsonObject.asText()
    }

    fun String.utf8(): String = URLEncoder.encode(this, "UTF-8")

    fun formData(data: Map<String, String?>): HttpRequest.BodyPublisher? {

        val res = data.map { (k, v) -> "${(k.utf8())}=${v?.utf8()}" }
            .joinToString("&")

        return HttpRequest.BodyPublishers.ofString(res)
    }

    private fun executeGetRequest(url: String, token: String): String {
        val client = HttpClient
            .newBuilder()
            //.proxy(ProxySelector.of(InetSocketAddress("localhost", 8085)))
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .headers("Content-Type", "application/x-www-form-urlencoded")
            .headers("Authorization", "Bearer $token")
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

}