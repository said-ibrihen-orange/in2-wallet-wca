package es.in2.wallet.controllers
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import org.json.JSONObject
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

@RestController
@RequestMapping("/api")
class KeycloakToken {


    fun getKeycloakToken(): String {
        val url = URL("http://localhost:8080/realms/master/protocol/openid-connect/token")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

        val postData = "grant_type=${URLEncoder.encode("password", "UTF-8")}" +
                "&client_id=${URLEncoder.encode("admin-rest-client", "UTF-8")}" +
                "&username=${URLEncoder.encode("admin", "UTF-8")}" +
                "&password=${URLEncoder.encode("password", "UTF-8")}"

        connection.doOutput = true
        connection.outputStream.write(postData.toByteArray())

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val inputStreamReader = InputStreamReader(connection.inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)

            val response = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                response.append(line)
            }

            bufferedReader.close()

            val jsonResponse = JSONObject(response.toString())

            return jsonResponse.getString("access_token")
        } else {
            throw Exception("Failed to obtain Keycloak token. Response Code: $responseCode")
        }
    }

    fun createUserInKeycloak(token: String, userData: Map<String, Any>) {
        val keycloak = KeycloakBuilder.builder()
                .serverUrl("http://localhost:8080")
                .realm("master")
                .authorization("Bearer $token")
                .build()



        val user = UserRepresentation()
        user.username = "testuser23"
        user.firstName = "Test23"
        user.lastName = "User23"
        user.email = "aaa@bbb3.com"

        keycloak.realm("master").users().create(user)
    }
    @PostMapping("/createuser")
    suspend fun main() {
        val token = getKeycloakToken()
        val userData = mapOf(
                "enabled" to "true",
                "username" to "4",
                "email" to "4@gmail.com",
                "firstName" to "test1",
                "lastName" to "test1"

        )
        createUserInKeycloak(token, userData)
    }


}