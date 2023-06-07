package es.in2.wallet.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jwt.SignedJWT
import es.in2.wallet.service.PersonalDataSpaceService
import es.in2.wallet.exception.NoSuchVerifiableCredentialException
import org.json.JSONArray
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Service
class PersonalDataSpaceServiceImpl(
    @Value("\${fiware.url}") private var fiwareURL: String
) : PersonalDataSpaceService {

    override fun getVerifiableCredentialsByVcType(userUUID: UUID, vcTypeList: List<String>): List<String> {
        val result = arrayListOf<String>()
        vcTypeList.forEach {
            val vcTypeWithoutSpace = it.replace(" ".toRegex(), "")
            val tmpResult = arrayListOf<String>()
            val client = HttpClient.newBuilder().build()
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$fiwareURL/v2/entities?user_ID=$userUUID&q=vc.type:$vcTypeWithoutSpace"))
                .GET()
                .build()
            val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            if (response.get().statusCode() == 404) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found")
            }
            val vcArray = JSONArray(response.get().body())
            for (i in 0 until vcArray.length()) {
                val vc = vcArray.getJSONObject(i)
                val vcID = vc.getString("id")
                println("vcID: $vcID")
                tmpResult.add(vcID)
            }
            if (result.isEmpty()) {
                result.addAll(tmpResult)
            } else {
                result.retainAll(tmpResult.toSet())
            }
        }
        if (result.isEmpty()) {
            throw NoSuchVerifiableCredentialException("There is no Verifiable Credential stored in Context Broker")
        }
        return result
    }

    // To refactor

    /**
     * Save the Verifiable Credential in the User Personal Data Space
     */
    override fun saveVC(userUUID: UUID, vc: String): String {
        // Parse the VC to get the credential ID the json
        val parsedVerifiableCredential = SignedJWT.parse(vc)
        val payloadToJson = parsedVerifiableCredential.payload.toJSONObject()
        val verifiableCredential = payloadToJson["vc"]
        var credentialID: Any? = null
        if (verifiableCredential is MutableMap<*, *>)
            credentialID = verifiableCredential["id"]
        if (credentialID == null)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Verifiable Credential does not contain an id")
        // Savin the VC in the FIWARE Context Broker in format jwt-token
        // Create the entity for save jwt token
        val vcJWTData = HashMap<String, Any>()
        // The id of the entity is the credential ID
        vcJWTData["id"] = credentialID
        // The type of the entity is vc_jwt
        vcJWTData["type"] = "vc_jwt"
        // The user ID is the user ID
        val vcJWTDataUserID = HashMap<String, String>()
        vcJWTDataUserID["type"] = "String"
        vcJWTDataUserID["value"] = userUUID.toString()
        vcJWTData["user_ID"] = vcJWTDataUserID
        val vcJWTDataCredential = HashMap<String, String>()
        vcJWTDataCredential["type"] = "String"
        vcJWTDataCredential["value"] = vc
        vcJWTData["vc"] = vcJWTDataCredential
        saveVC(vcJWTData)
        // Saving the VC in the FIWARE Context Broker in format json
        val vcJSONData = HashMap<String, Any>()
        vcJSONData["id"] = credentialID
        vcJSONData["type"] = "vc_json"
        vcJSONData["user_ID"] = vcJWTDataUserID
        val vcJSONDataCredential = HashMap<String, Any>()
        vcJSONDataCredential["type"] = "JSON"
        vcJSONDataCredential["value"] = verifiableCredential as Any
        vcJSONData["vc"] = vcJSONDataCredential
        saveVC(vcJSONData)
        return credentialID.toString()
    }




    // TODO to refactor





    /**
     * Get the VC by format (vc_jwt or vc_json)
     */
    override fun getVCByFormat(userUUID: UUID, vcId: String, vcFormat: String): String {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$fiwareURL/v2/entities/$vcId?type=$vcFormat&user_ID=$userUUID"))
            .GET()
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() == 404) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found")
        }
        return response.get().body()
    }

    private fun saveVC(vcJWT: HashMap<String, Any>) {
        val objectMapper = ObjectMapper()
        val requestBody = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(vcJWT)
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$fiwareURL/v2/entities"))
            .headers("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() == 422) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Entity already exists")
        }
    }

    override fun getVCs(userUUID: UUID): String {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$fiwareURL/v2/entities?user_ID=$userUUID"))
            .GET()
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() == 404) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        }
        return response.get().body()
    }

    /**
     * Get the VCs by type (vc_jwt or vc_json)
     */
    override fun getVCsByFormat(userUUID: UUID, vcFormat: String): String {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$fiwareURL/v2/entities?type=$vcFormat&user_ID=$userUUID"))
            .GET()
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() == 404) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with type: $vcFormat not found")
        }
        return response.get().body()
    }

    override fun deleteVC(userUUID: UUID, vcId: String) {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$fiwareURL/v2/entities/$vcId?type=vc_jwt"))
            .DELETE()
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() == 404) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Credential $vcId with type jwt not found")
        }

        val request2 = HttpRequest.newBuilder()
            .uri(URI.create("$fiwareURL/v2/entities/$vcId?type=vc_json"))
            .DELETE()
            .build()
        val response2 = client.sendAsync(request2, HttpResponse.BodyHandlers.ofString())
        if (response2.get().statusCode() == 404) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Credential $vcId with type json not found")
        }
    }

    override fun getVCsByVCTypes(userUUID: UUID, vcTypeList: List<String>): ArrayList<String> {
        val result = arrayListOf<String>()
        for (vcType in vcTypeList) {
            val vcTypeWithoutSpace = vcType.replace(" ".toRegex(), "")
            val tmpResult = arrayListOf<String>()
            val client = HttpClient.newBuilder().build()
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$fiwareURL/v2/entities?user_ID=$userUUID&q=vc.type:$vcTypeWithoutSpace"))
                .GET()
                .build()
            val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            if (response.get().statusCode() == 404) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found")
            }
            val vcArray = JSONArray(response.get().body())
            for (i in 0 until vcArray.length()) {
                val vc = vcArray.getJSONObject(i)
                val vcID = vc.getString("id")
                println("vcID: $vcID")
                tmpResult.add(vcID)
            }

            if (result.isEmpty()) {
                result.addAll(tmpResult)
            } else {
                result.retainAll(tmpResult.toSet())
            }
        }

        if (result.isEmpty()) {
            throw NoSuchVerifiableCredentialException("There is no Verifiable Credential stored in Context Broker")
        }

        return result
    }

    /**
     * This method is used to initialize the url of the FIWARE Context Broker only for testing purposes
     * @param url the url of the FIWARE Context Broker
     */
    fun initUrl(url: String) {
        if (fiwareURL.isBlank()) { fiwareURL = url }
    }

}