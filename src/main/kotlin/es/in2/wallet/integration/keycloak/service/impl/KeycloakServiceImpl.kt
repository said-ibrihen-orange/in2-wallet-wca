package es.in2.wallet.integration.keycloak.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import es.in2.wallet.integration.keycloak.service.KeycloakService
import es.in2.wallet.api.util.*
import es.in2.wallet.integration.keycloak.model.dto.KeycloakUserDTO
import jakarta.transaction.Transactional
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.json.JSONObject
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource.RealmsResource
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

    // TODO: For this to work we must configure KeyCloak by importing EAAProvider-realm.json
    //  This setup should be done automatically instead of manually.
    override fun getKeycloakToken(): String {
        val url = "${getKeycloakUrl()}/realms/$KEYCLOAK_REALM/protocol/openid-connect/token"
        val headers = listOf(CONTENT_TYPE to CONTENT_TYPE_URL_ENCODED_FORM)
        val formData = mapOf(
            "grant_type" to "password",
            "client_id" to KEYCLOAK_CLIENT_ID,
            "client_secret" to getKeycloakClientSecret(),
            "username" to KEYCLOAK_ADMIN_USERNAME,
            "password" to getKeycloakAdminPassword()
        )
        val body = ApplicationUtils.buildUrlEncodedFormDataRequestBody(formDataMap = formData)
        val response = ApplicationUtils.postRequest(url = url, headers = headers, body = body)
        log.info("Access token: $response")
        val jsonObject = ObjectMapper().readValue(response, Map::class.java) as Map<String, Any>
        return jsonObject["access_token"].toString()
    }

    @Transactional
    override fun createUserInKeycloak(token: String, userData: KeycloakUserDTO) {
        val keycloak = getKeycloakClient(token = token)

        val user = toUserRepresentation(userData = userData)

        val response = keycloak.realm(KEYCLOAK_REALM).users().create(user)
        keycloak.close()
        log.info("Response ${response.status}")
        val responseBody = response.readEntity(String::class.java)
        if (response.status < 200 || response.status > 299) {
            throw Exception("Response status ${response.status}, user not created because $responseBody")
        }
    }

    private fun toUserRepresentation(userData: KeycloakUserDTO): UserRepresentation {
        val user = UserRepresentation()

        user.username = userData.username
        user.firstName = userData.firstName
        user.lastName = userData.lastName
        user.email = userData.email

        return user
    }

    private fun toKeycloakUser(user: UserRepresentation): KeycloakUserDTO{
        return KeycloakUserDTO(
            username = user.username,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            id = user.id
        )
    }

    // The service account associated with the client needs to be allowed to view realm users otherwise returns 403 forbidden
    // https://stackoverflow.com/questions/66452108/keycloak-get-users-returns-403-forbidden
    override fun getKeycloakUsers(token: String): List<KeycloakUserDTO> {
        val keycloak = getKeycloakClient(token = token)
        val users: UsersResource? = keycloak.realm(KEYCLOAK_REALM).users()
        val result = mutableListOf<KeycloakUserDTO>()
        if (users != null){
            val userList = users.list()
            userList.forEach{user ->
                val userData = toKeycloakUser(user = user)
                log.info(userData)
                result.add(userData)
            }
            keycloak.close()
        }
        return result
    }

    override fun getKeycloakUser(token: String, username: String): KeycloakUserDTO? {
        val keycloak = getKeycloakClient(token=token)
        val users = keycloak.realm(KEYCLOAK_REALM).users().search(username)
        if (users != null && users.count() == 1) {
            val user = users[0]
            return toKeycloakUser(user=user)
        }
        return null
    }

    override fun getKeycloakUserById(token: String, id: String): KeycloakUserDTO? {
        val keycloak = getKeycloakClient(token = token)
        return toKeycloakUser(user = keycloak.realm(KEYCLOAK_REALM).users()[id].toRepresentation())
    }

    override fun deleteKeycloakUser(token: String, username: String) {
        val keycloak = getKeycloakClient(token = token)
        val usersResource: UsersResource? = keycloak.realm(KEYCLOAK_REALM).users()
        val users = usersResource?.search(username)
        if (users != null && users.count() == 1) {
            val user = users[0]
            val userResource: UserResource = usersResource[user.id]
            userResource.remove()
        } else {
            throw Exception("Couldn't delete user $username because it doesn't exist")
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

    fun getKeycloakAdminPassword(): String {
        return System.getenv("KC_ADMIN_PASSWORD")
    }
}