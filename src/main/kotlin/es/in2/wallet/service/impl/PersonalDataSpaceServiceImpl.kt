package es.in2.wallet.service.impl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jwt.SignedJWT
import es.in2.wallet.exception.NoSuchVerifiableCredentialException
import es.in2.wallet.model.dto.contextBroker.VerifiableCredentialEntityContextBrokerDTO
import es.in2.wallet.service.AppUserService
import es.in2.wallet.service.PersonalDataSpaceService
import es.in2.wallet.util.ApplicationUtils
import es.in2.wallet.util.VC_JSON
import es.in2.wallet.util.VC_JWT
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class PersonalDataSpaceServiceImpl(
    private val appUserService: AppUserService,
    private val applicationUtils: ApplicationUtils,
    @Value("\${app.url.orion_context_broker}") private val contextBrokerEntitiesURL: String
) : PersonalDataSpaceService {

    private val log: Logger = LoggerFactory.getLogger(PersonalDataSpaceServiceImpl::class.java)
    override fun saveVC(vcJwt: String) {
        log.info("PersonalDataSpaceServiceImpl.saveVC()")
        val userUUID = getUserUUIDFromContextAuthentication()
        // Get the vc stored in the vc_jwt and parsed in JSON format
        val vcInJsonFormat = getVcInJsonFormatFromVcInJwtFormat(vcJwt)
        // Get VerifiableCredentialId
        val verifiableCredentialId = getVerifiableCredentialIdFromVcInVcJwt(vcInJsonFormat)
        // Persist the Verifiable Credential in JWT format
        val vcAsJwtContextBrokerObject =
            buildContextBrokerObjectWithVcInJwtFormat(userUUID, verifiableCredentialId, vcJwt)
        persistVcInContextBroker(vcAsJwtContextBrokerObject)
        // Persist the Verifiable Credential in JSON format
        val vcAsJsonContextBrokerObject =
            buildContextBrokerObjectWithVcInJsonFormat(userUUID, verifiableCredentialId, vcInJsonFormat)
        persistVcInContextBroker(vcAsJsonContextBrokerObject)
    }

    override fun getAllVerifiableCredentials(): MutableList<String> {
        log.info("PersonalDataSpaceServiceImpl.getAllVerifiableCredentialsByAppUser()")
        val userUUID = getUserUUIDFromContextAuthentication()
        val response = applicationUtils.getRequest("$contextBrokerEntitiesURL?user_ID=$userUUID")

        return parserToMutableListVerifiableCredential(response)
    }

    override fun getAllVerifiableCredentialsByFormat(vcFormat: String): MutableList<String> {
        // Get user session
        val userUUID = getUserUUIDFromContextAuthentication()
        val response = applicationUtils.getRequest("$contextBrokerEntitiesURL?type=$vcFormat&user_ID=$userUUID")

        return parserToMutableListVerifiableCredential(response)
    }

    private fun parserToMutableListVerifiableCredential(response: String): MutableList<String> {
        val result = mutableListOf<String>()
        val objectMapper = ObjectMapper()
        val parsedBody = objectMapper.readTree(response)
        parsedBody.forEach {
            val vcEntity = objectMapper.readValue(it.toString(), VerifiableCredentialEntityContextBrokerDTO::class.java)

            result.add(vcEntity.vc.value.toString())
        }
        return result
    }

    override fun getVcIdListByVcTypeList(vcTypeList: List<String>): List<String> {
        log.info("PersonalDataSpaceServiceImpl.getVcListByVcTypeList()")
        val result = mutableListOf<String>()
        val vcListByFormat = getAllVerifiableCredentialsByFormat(VC_JSON)
        vcListByFormat.forEach {
            val vc = ObjectMapper().readTree(it)
            // Capture vc_types from VC (it)
            val tempList = mutableListOf<String>()
            vc["type"].forEach { at -> tempList.add(at.asText()) }
            // If vc_types matches with vc_types requested, save vc_id
            if (tempList.containsAll(vcTypeList)) {
                result.add(vc["id"].asText())
            }
        }
        checkIfResultIsEmpty(result)
        return result
    }

    override fun getVerifiableCredentialByIdAndFormat(id: String, format: String): String {
        // Get user session
        val userUUID = getUserUUIDFromContextAuthentication()
        val response = applicationUtils.getRequest("$contextBrokerEntitiesURL/$id?type=$format&user_ID=$userUUID")
        val objectMapper = ObjectMapper()
        return objectMapper.readValue(response, VerifiableCredentialEntityContextBrokerDTO::class.java).vc.value.toString()
    }



    override fun deleteVerifiableCredential(id: String) {
        applicationUtils.deleteRequest("$contextBrokerEntitiesURL/$id?type=$VC_JWT")
        applicationUtils.deleteRequest("$contextBrokerEntitiesURL/$id?type=$VC_JSON")
    }

    private fun checkIfResultIsEmpty(result: MutableList<String>) {
        if (result.isEmpty()) {
            throw NoSuchVerifiableCredentialException("There is no Verifiable Credential stored in Context Broker")
        }
    }

    private fun getUserUUIDFromContextAuthentication(): String {
        val userSession = appUserService.getUserWithContextAuthentication()
        return userSession.id!!.toString()
    }

    private fun getVcInJsonFormatFromVcInJwtFormat(vcJwt: String): JsonNode {
        log.info("PersonalDataSpaceServiceImpl.getVcInJsonFormatFromVcInJwtFormat()")
        // Parse the vc_jwt to a readable JSWObject
        val parsedVcJwt = SignedJWT.parse(vcJwt)
        // Get the payload from vc_jwt
        val parsedVcJwtPayload = ObjectMapper().readTree(parsedVcJwt.payload.toString())
        // Get 'vc' claim of the payload
        return parsedVcJwtPayload["vc"]
    }

    private fun getVerifiableCredentialIdFromVcInVcJwt(vcInJsonFormat: JsonNode): String {
        log.info("PersonalDataSpaceServiceImpl.getVerifiableCredentialIdFromVcInVcJwt()")
        // Get the ID of the Verifiable Credential and verify it is not null or empty
        val verifiableCredentialId = vcInJsonFormat["id"]
        checkIfCredentialIdIsNull(verifiableCredentialId)
        // return the verifiable_credential_id as a String
        return verifiableCredentialId.asText()
    }

    private fun buildContextBrokerObjectWithVcInJwtFormat(
        userUUID: String, verifiableCredentialId: String,
        vcJwtFormat: String
    ): MutableMap<String, Any> {
        log.info("PersonalDataSpaceServiceImpl.buildContextBrokerObjectWithVcInJwtFormat()")
        return mutableMapOf(
            Pair("id", verifiableCredentialId),
            Pair("type", "vc_jwt"),
            Pair(
                "user_ID", mutableMapOf(
                    Pair("type", "String"),
                    Pair("value", userUUID)
                )
            ),
            Pair(
                "vc", mutableMapOf(
                    Pair("type", "String"),
                    Pair("value", vcJwtFormat)
                )
            )
        )
    }

    private fun buildContextBrokerObjectWithVcInJsonFormat(
        userUUID: String, verifiableCredentialId: String,
        vcInJsonFormat: JsonNode
    ): MutableMap<String, Any> {
        log.info("PersonalDataSpaceServiceImpl.buildContextBrokerObjectWithVcInJsonFormat()")
        return mutableMapOf(
            Pair("id", verifiableCredentialId),
            Pair("type", "vc_json"),
            Pair(
                "user_ID", mutableMapOf(
                    Pair("type", "String"),
                    Pair("value", userUUID)
                )
            ),
            Pair(
                "vc", mutableMapOf(
                    Pair("type", "String"),
                    Pair("value", vcInJsonFormat)
                )
            )
        )
    }

    private fun checkIfCredentialIdIsNull(verifiableCredentialId: JsonNode) {
        log.info("PersonalDataSpaceServiceImpl.checkIfCredentialIdIsNullOrEmpty()")
        if (verifiableCredentialId.isNull) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Verifiable Credential does not contain an id")
        }
    }

    private fun persistVcInContextBroker(contextBrokerObject: MutableMap<String, Any>) {
        log.info("PersonalDataSpaceServiceImpl.persistVcInContextBroker()")
        val url = contextBrokerEntitiesURL
        val requestBody = ObjectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(contextBrokerObject)
        applicationUtils.postRequest(url, requestBody)
    }

}