package es.in2.wallet.service.impl

import es.in2.wallet.service.KeycloakService
import jakarta.transaction.Transactional
import org.json.JSONObject
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

@Service
class KeycloakServiceImpl : KeycloakService {
    override fun test(): String {
        print("test");
        return "test";
    }

    override fun getKeycloakToken(): String {
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
    @Transactional
    override fun createUserInKeycloak(token: String, userData: Map<String, Any>) {
        val keycloak = KeycloakBuilder.builder()
                .serverUrl("http://localhost:8080")
                .realm("master")
                .authorization("Bearer $token")
                .build()

        val user = UserRepresentation()

        user.username = userData["username"].toString()
        user.firstName = userData["firstName"].toString()
        user.lastName = userData["lastName"].toString()
        user.email = userData["email"].toString()

        keycloak.realm("master").users().create(user)
    }
}