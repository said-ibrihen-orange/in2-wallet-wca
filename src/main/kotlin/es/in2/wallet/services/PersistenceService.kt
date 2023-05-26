package es.in2.wallet.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jwt.SignedJWT
import org.json.JSONArray
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

interface PersistenceService {
    fun saveVC(vc: String, userid: String): String
    fun getVCByType(userid:String,vcId:String,vcType: String): String
    fun getVCs(userid: String): String
    fun getVCsByFormat(userid: String, vcFormat: String): String
    fun deleteVC(userid: String, vcId: String)
    fun getVCsByVCType(userid: String, vcTypeList: List<String>): ArrayList<String>
}

@Service
class PersistenceServiceImpl: PersistenceService {

    @Value("\${fiware.url}")
    private var fiwareURL: String? = null

    /**
     * Save the VC in the FIWARE Context Broker
     * @param vc the VC data
     * @return the id of the entity
     */
    override fun saveVC(vc: String, userid: String): String {

        // Parse the VC to get the credential ID the json
        val parsedVerifiableCredential = SignedJWT.parse(vc)
        val payloadToJson = parsedVerifiableCredential.payload.toJSONObject()
        val verifiableCredential = payloadToJson["vc"]
        var credentialID: Any? = null
        if (verifiableCredential is MutableMap<*, *>)
            credentialID = verifiableCredential["id"]

        if (credentialID == null)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Verifiable Credential does not contain an id")

        /*
        * Attributos de la entidad:
            credential_ID (el de la credencial, no el del sujeto)
            user_ID
            credential_type: (vc_jwt / vc_json)
            vc (formato JWT-JWS o los datos de la credential en formato JSON)*/

        // Savin the VC in the FIWARE Context Broker in format jwt-token

        // Create the entity for save jwt token
        val vcJWTData = HashMap<String,Any>()
        // The id of the entity is the credential ID
        vcJWTData["id"] = credentialID
        // The type of the entity is vc_jwt
        vcJWTData["type"] = "vc_jwt"
        // The user ID is the user ID
        val vcJWTDataUserID = HashMap<String,String>()
            vcJWTDataUserID["type"] = "String"
            vcJWTDataUserID["value"] = userid
        vcJWTData["user_ID"] = vcJWTDataUserID

        val vcJWTDataCredential = HashMap<String,String>()
            vcJWTDataCredential["type"] = "String"
            vcJWTDataCredential["value"] = vc

        vcJWTData["vc"] = vcJWTDataCredential

        saveVC(vcJWTData)

        // Saving the VC in the FIWARE Context Broker in format json

        val vcJSONData = HashMap<String,Any>()
        vcJSONData["id"] = credentialID
        vcJSONData["type"] = "vc_json"
        vcJSONData["user_ID"] = vcJWTDataUserID

        val vcJSONDataCredential = HashMap<String,Any>()
            vcJSONDataCredential["type"] = "JSON"
            vcJSONDataCredential["value"] = payloadToJson
        vcJSONData["vc"] = vcJSONDataCredential

        saveVC(vcJSONData)
        return credentialID.toString()
    }

    /**
     * Get the VC by type (vc_jwt or vc_json)
     */
    override fun getVCByType(userid:String,vcId: String, vcType: String): String {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$fiwareURL/v2/entities/$vcId?type=$vcType&user_ID=$userid"))
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

    override fun getVCs(userid: String): String {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$fiwareURL/v2/entities?user_ID=$userid"))
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
    override fun getVCsByFormat(userid: String, vcFormat: String): String {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$fiwareURL/v2/entities?type=$vcFormat&user_ID=$userid"))
            .GET()
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() == 404) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with type: $vcFormat not found")
        }
        return response.get().body()
    }

    override fun deleteVC(userid: String, vcId: String) {
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

    override fun getVCsByVCType(userid: String, vcTypeList: List<String>): ArrayList<String> {
        val result = arrayListOf<String>()
        for (vcType in vcTypeList){
            val vcTypeWithoutSpace = vcType.replace(" ".toRegex(), "")
            val client = HttpClient.newBuilder().build()
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$fiwareURL/v2/entities?q=user_ID==$userid&q=vc.vc.type:$vcTypeWithoutSpace"))
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
                result.add(vcID)
            }
        }
        return result
    }

    /**
     * This method is used to initialize the url of the FIWARE Context Broker only for testing purposes
     * @param url the url of the FIWARE Context Broker
     */
    fun initUrl(url: String){
        if (fiwareURL == null)
            fiwareURL = url
    }

}


