package es.in2.wallet.util

import es.in2.wallet.configuration.UserAdminConfig
import es.in2.wallet.exception.FailedCommunicationException
import es.in2.wallet.model.OpenIdConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
object ApplicationUtils {

    private val log: Logger = LoggerFactory.getLogger(UserAdminConfig::class.java)

    fun getRequest(url: String): String {
        log.info("ApplicationUtils.getRequest()")
        // Set the HttpClient
        val client = HttpClient.newBuilder().build()
        // Execute Request
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()
        // Get Response
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        // Verify Response HttpStatus
        checkGetResponseStatus(response.get().statusCode())
        // Return body as String
        return response.get().body()
    }

    fun postRequest(url: String, requestBody: String, contentType: String): String {
        log.info("ApplicationUtils.postRequest()")
        // Set the HttpClient
        val client = HttpClient.newBuilder().build()
        // Execute Request
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .headers(CONTENT_TYPE, contentType)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()
        // Get Response
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if(response.get().body().isNotBlank()) {
            log.info("post response = {}", response.get().body())
        } else {
            log.info("post response = {}", response.get().statusCode())
        }
        // Verify Response HttpStatus
        checkPostResponseStatus(response.get().statusCode())
        return response.get().body()
    }

    fun deleteRequest(url: String) {
        log.info("ApplicationUtils.deleteRequest()")
        // Set the HttpClient
        val client = HttpClient.newBuilder().build()
        // Execute Request
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .DELETE()
            .build()
        // Get Response
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        // Verify Response HttpStatus
        checkDeleteResponseStatus(response.get().statusCode())
    }

    private fun checkGetResponseStatus(statusCode: Int) {
        when (statusCode) {
            200 -> {
                log.info("Get request done successfully")
            }
            404 -> {
                throw NoSuchElementException("Element not found: $statusCode")
            }
            else -> {
                throw FailedCommunicationException("Request cannot be completed: $statusCode")
            }
        }
    }

    private fun checkPostResponseStatus(statusCode: Int) {
        when (statusCode) {
            201 -> {
                log.info("Post request done successfully")
            }
            200 -> {
                log.info("Post request done successfully")
            }
            422 -> {
                throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Entity already exists")
            }
        }
    }

    private fun checkDeleteResponseStatus(statusCode: Int) {
        when (statusCode) {
            204 -> {
                log.info("Delete request done successfully")
            }
            404 -> {
                throw NoSuchElementException("Element not found: $statusCode")
            }
        }
    }

    fun parseOpenIdConfig(url: String): OpenIdConfig {
        val builder = UriComponentsBuilder.fromUriString(url)
        val queryParams = builder.build().queryParams

        val scope = queryParams.getFirst("scope")!!
        val responseType = queryParams.getFirst("response_type")!!
        val responseMode = queryParams.getFirst("response_mode")!!
        val clientId = queryParams.getFirst("client_id")!!
        val redirectUri = queryParams.getFirst("redirect_uri")!!
        val state = queryParams.getFirst("state")!!
        val nonce = queryParams.getFirst("nonce")!!

        return OpenIdConfig(
            scope = scope,
            responseType = responseType,
            responseMode = responseMode,
            clientId = clientId,
            redirectUri = redirectUri,
            state = state,
            nonce = nonce
        )
    }

}