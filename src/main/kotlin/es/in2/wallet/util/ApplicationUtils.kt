package es.in2.wallet.util

import es.in2.wallet.exception.FailedCommunicationException
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
object ApplicationUtils {

    fun getRequest(url: String): String {
        // Build Client
        val client = HttpClient.newBuilder().build()
        // Execute Request
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()
        // Get Response
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        // Verify Response HttpStatus
        checkResponseStatus(response.get().statusCode())
        // Return body as String
        return response.get().body()
    }

    private fun checkResponseStatus(statusCode: Int) {
        if (statusCode != 200) {
            throw FailedCommunicationException("Request cannot be completed. HttpStatus response $statusCode")
        }
    }




}