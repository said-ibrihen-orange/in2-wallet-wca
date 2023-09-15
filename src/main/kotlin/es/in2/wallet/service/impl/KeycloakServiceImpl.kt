package es.in2.wallet.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import es.in2.wallet.service.KeycloakService
import es.in2.wallet.util.*
import id.walt.common.prettyPrint
import jakarta.transaction.Transactional
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.json.JSONObject
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource.UserResource
import org.keycloak.admin.client.resource.UsersResource
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

@Service
class KeycloakServiceImpl : KeycloakService {
    private val log: Logger = LogManager.getLogger(KeycloakServiceImpl::class.java)

    override fun test(): String {
        return "test";
    }

    // TODO: This was the original function, should be removed.
    fun getKeycloakToken1(): String {
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

    // TODO: For this to work we must configure KeyCloak by importing realm-import.json
    //  https://github.com/keycloak/keycloak-quickstarts/blob/latest/spring/rest-authz-resource-server/src/test/resources/realm-import.json
    //  Another alternative is to import EAAProvider-realm.json
    //  This setup should be done automatically instead of manually and it should be tailored to our specific needs.
    override fun getKeycloakToken(): String {
        val url = "${getKeycloakUrl()}/realms/$KEYCLOAK_REALM/protocol/openid-connect/token"
        val headers = listOf(CONTENT_TYPE to CONTENT_TYPE_URL_ENCODED_FORM)
        val formData = mapOf(
            "grant_type" to "password",
            "client_id" to "oidc4vci-client",
            "client_secret" to getKeycloakClientSecret(),
            "username" to "in2admin1",
            "password" to "password"
        )
        val body = ApplicationUtils.buildUrlEncodedFormDataRequestBody(formDataMap = formData)
        val response = ApplicationUtils.postRequest(url = url, headers = headers, body = body)
        log.info("Access token: $response")
        val jsonObject = ObjectMapper().readValue(response, Map::class.java) as Map<String, Any>
        return jsonObject["access_token"].toString()
    }
    @Transactional
    override fun createUserInKeycloak(token: String, userData: Map<String, Any>) {
        val keycloak = getKeycloakClient(token = token)

        val user = UserRepresentation()

        user.username = userData["username"].toString()
        user.firstName = userData["firstName"].toString()
        user.lastName = userData["lastName"].toString()
        user.email = userData["email"].toString()

        val response = keycloak.realm(KEYCLOAK_REALM).users().create(user)
        log.info("Response ${response.status}")
        val responseBody = response.readEntity(String::class.java)
        if (response.status < 200 || response.status > 299) {
            throw Exception("Response status ${response.status}, user not created because $responseBody")
        }
    }

    // The service account associated with the client needs to be allowed to view realm users otherwise returns 403 forbidden
    // https://stackoverflow.com/questions/66452108/keycloak-get-users-returns-403-forbidden
    fun getKeycloakUsers(token: String) {
        val keycloak = getKeycloakClient(token = token)
        val users: UsersResource? = keycloak.realm(KEYCLOAK_REALM).users()
        if (users != null){
            val userList = users.list()
            userList.forEach{user ->
                val userPrint = listOf(
                    user.firstName,
                    user.lastName,
                    user.username,
                    user.email
                )
                log.info(userPrint)
            }
        }
    }

    fun getKeycloakClient(token: String): Keycloak{
        return KeycloakBuilder.builder()
            .serverUrl(getKeycloakUrl())
            .realm(KEYCLOAK_REALM)
            .authorization("Bearer $token")
            .build()
    }

    fun getKeycloakUrl(): String{
        return System.getenv("KC_HOSTNAME_URL")
    }

    fun getKeycloakClientSecret(): String {
        return System.getenv("KC_CLIENT_SECRET")
    }
}