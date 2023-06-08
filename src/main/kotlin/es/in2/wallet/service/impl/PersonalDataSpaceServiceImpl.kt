package es.in2.wallet.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jwt.SignedJWT
import es.in2.wallet.service.PersonalDataSpaceService
import es.in2.wallet.exception.NoSuchVerifiableCredentialException
import es.in2.wallet.service.AppUserService
import org.json.JSONArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

@Service
class PersonalDataSpaceServiceImpl(
    private val appUserService : AppUserService,
    @Value("\${app.url.orion_context_broker}") private val contextBrokerEntitiesURL: String
): PersonalDataSpaceService {

    private val log: Logger = LoggerFactory.getLogger(PersonalDataSpaceServiceImpl::class.java)

    override fun getVcListByVcTypeList(vcTypeList: List<String>): List<String> {
        log.info("PersonalDataSpaceServiceImpl.getVcListByVcTypeList()")

        // get user session
        val userSession = appUserService.getUserWithContextAuthentication()
        val userUUID = userSession.id!!

        val result = mutableListOf<String>()

        vcTypeList.forEach {
            val vcTypeFormatted = it.replace(" ".toRegex(), "")
            val tmpResult = arrayListOf<String>()

            val responseBody =
                getRequestToContextBroker("$contextBrokerEntitiesURL?user_ID=$userUUID&q=vc.type:$vcTypeFormatted")

            val vcArray = JSONArray(responseBody)
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
        checkIfResultIsEmpty(result)
        return result
    }

    private fun getRequestToContextBroker(url: String): String {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        checkStatusResponse(response.get().statusCode())
        return response.get().body()
    }


    // To refactor

    /**
     * Save the Verifiable Credential in the User Personal Data Space
     */
    override fun saveVC(vcJwt: String): String {
        // get user session
        val userSession = appUserService.getUserWithContextAuthentication()
        val userUUID = userSession.id!!

        // Parse the VC to get the credential ID the json
        val verifiableCredentialId = getVerifiableCredentialIdFromVcJwt(vcJwt)
        // Persist the Verifiable Credential in JWT format
        val vcJwtContextBrokerObject = buildVcJwtFormatObject(verifiableCredentialId, userUUID, vcJwt)
        persistVcInContextBroker(vcJwtContextBrokerObject)
        // Persist the Verifiable Credential in JSON format
        val vcJsonContextBrokerObject = buildVcJsonFormatObject(verifiableCredentialId, userUUID)
        persistVcInContextBroker(vcJsonContextBrokerObject)
        // FIXME por qué devolemos algo?
        return verifiableCredentialId
    }

    private fun buildVcJwtFormatObject(verifiableCredentialId: String, userUUID: UUID, vcJwtFormat: String):
            MutableMap<String, Any> {
        return mutableMapOf(
            Pair("id", verifiableCredentialId),
            Pair("type", "vc_jwt"),
            Pair("user_ID", mutableMapOf(
                Pair("type", "String"),
                Pair("value", userUUID.toString())
            )),
            Pair("vc", mutableMapOf(
                Pair("type", "String"),
                Pair("value", vcJwtFormat)
            ))
        )
    }

    private fun buildVcJsonFormatObject(verifiableCredentialId: String, userUUID: UUID): MutableMap<String, Any> {
        return mutableMapOf(
            Pair("id", verifiableCredentialId),
            Pair("type", "vc_json"),
            Pair("user_ID", mutableMapOf(
                Pair("type", "String"),
                Pair("value", userUUID.toString())
            )),
            Pair("vc", mutableMapOf(
                Pair("type", "JSON"),
                Pair("value", verifiableCredentialId)
            ))
        )
    }

    private fun persistVcInContextBroker(vcJWT: MutableMap<String, Any>) {
        val objectMapper = ObjectMapper()
        val requestBody = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(vcJWT)
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(contextBrokerEntitiesURL))
            .headers("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() == 422) {
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Entity already exists")
        }
    }

    private fun getVerifiableCredentialIdFromVcJwt(vc: String): String {
        val parsedVerifiableCredential = SignedJWT.parse(vc)
        val payloadToJson = parsedVerifiableCredential.payload.toJSONObject()
        val verifiableCredential = payloadToJson["vc"]
        val verifiableCredentialID = if (verifiableCredential is MutableMap<*, *>) {
            verifiableCredential["id"].toString()
        } else {
            null
        }
        checkIfCredentialIdIsNullOrEmpty(verifiableCredentialID)
        return verifiableCredentialID.toString()
    }

    private fun checkIfCredentialIdIsNullOrEmpty(credentialID: Any?) {
        if(credentialID == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Verifiable Credential does not contain an id")
        }
    }

    private fun checkStatusResponse(statusCode: Int) {
        if (statusCode == 404) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found")
        }
    }

    private fun checkIfResultIsEmpty(result: MutableList<String>) {
        if (result.isEmpty()) {
            throw NoSuchVerifiableCredentialException("There is no Verifiable Credential stored in Context Broker")
        }
    }



    // TODO to refactor





    /**
     * Get the VC by format (vc_jwt or vc_json)
     */
    override fun getVCByFormat(vcId: String, vcFormat: String): String {
        // get user session
        val userSession = appUserService.getUserWithContextAuthentication()
        val userUUID = userSession.id!!

        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$contextBrokerEntitiesURL/$vcId?type=$vcFormat&user_ID=$userUUID"))
            .GET()
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() == 404) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found")
        }
        return response.get().body()
    }



    override fun getVCs(): String {
        // get user session
        val userSession = appUserService.getUserWithContextAuthentication()
        val userUUID = userSession.id!!

        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$contextBrokerEntitiesURL?user_ID=$userUUID"))
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
    override fun getVCsByFormat(vcFormat: String): String {

        // get user session
        val userSession = appUserService.getUserWithContextAuthentication()
        val userUUID = userSession.id!!

        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$contextBrokerEntitiesURL?type=$vcFormat&user_ID=$userUUID"))
            .GET()
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() == 404) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with type: $vcFormat not found")
        }
        return response.get().body()
    }

    override fun deleteVC(vcId: String) {

        // get user session
        val userSession = appUserService.getUserWithContextAuthentication()
        val userUUID = userSession.id!!

        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$contextBrokerEntitiesURL/$vcId?type=vc_jwt"))
            .DELETE()
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        if (response.get().statusCode() == 404) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Credential $vcId with type jwt not found")
        }

        val request2 = HttpRequest.newBuilder()
            .uri(URI.create("$contextBrokerEntitiesURL/$vcId?type=vc_json"))
            .DELETE()
            .build()
        val response2 = client.sendAsync(request2, HttpResponse.BodyHandlers.ofString())
        if (response2.get().statusCode() == 404) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Credential $vcId with type json not found")
        }
    }



//    /**
//     * This method is used to initialize the url of the FIWARE Context Broker only for testing purposes
//     * @param url the url of the FIWARE Context Broker
//     */
//    fun initUrl(url: String) {
//        if (contextBrokerEntitiesURL.isBlank()) { contextBrokerEntitiesURL = url }
//    }

}