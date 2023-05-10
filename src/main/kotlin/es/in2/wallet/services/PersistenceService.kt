package es.in2.wallet.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jwt.SignedJWT
import es.in2.wallet.FIWARE_URL
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

interface PersistenceService {
    fun saveVC(vc: String, userid: String)
    fun getVCs(userid: String): String
    fun deleteVC(userid: String, vcId: String)

}

@Service
class PersistenceServiceImpl:PersistenceService{
    override fun saveVC(vc: String, userid: String) {

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

        val vcJWTdata = HashMap<String,Any>()
        vcJWTdata["id"] = userid
        vcJWTdata["credential_ID"] = credentialID
        vcJWTdata["credential_type"] = "vc_jwt"
        vcJWTdata["vc"] = vc

        val vcJSONdata = HashMap<String,Any>()
        vcJSONdata["user_ID"] = userid
        vcJSONdata["credential_ID"] = credentialID
        vcJSONdata["credential_type"] = "vc_json"
        vcJSONdata["vc"] = vc

        val vcJWT = HashMap<String,Any>()
        vcJWT["id"] = "Room1"
        vcJWT["type"] = "Room"
        vcJWT["vc"] = vcJWTdata
        saveVC(vcJWT)
//        saveVC(vcJSON)

    }

    private fun saveVC(vcJWT: HashMap<String, Any>) {
        val objectMapper = ObjectMapper()
        val requestBody = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(vcJWT)

        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$FIWARE_URL/v2/entities"))
            .headers("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() == 422) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Entity already exists")
        }
    }



    override fun getVCs(userid: String): String {
        TODO()
    }

    override fun deleteVC(userid: String, vcId: String) {
        TODO()
    }
}

