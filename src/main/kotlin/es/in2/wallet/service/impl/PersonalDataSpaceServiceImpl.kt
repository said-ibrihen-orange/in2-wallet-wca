package es.in2.wallet.service.impl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jwt.SignedJWT
import es.in2.wallet.exception.InvalidDIDFormatException
import es.in2.wallet.exception.DIDNotFoundException
import es.in2.wallet.exception.NoSuchVerifiableCredentialException
import es.in2.wallet.model.ContextBrokerAttribute
import es.in2.wallet.model.DidContextBrokerEntity
import es.in2.wallet.model.DidMethods
import es.in2.wallet.model.VcContextBrokerEntity
import es.in2.wallet.model.dto.DidResponseDTO
import es.in2.wallet.model.dto.VcBasicDataDTO
import es.in2.wallet.service.AppUserService
import es.in2.wallet.service.PersonalDataSpaceService
import es.in2.wallet.util.*
import es.in2.wallet.util.ApplicationUtils.postRequest
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.net.URLEncoder
import java.util.*

@Service
class PersonalDataSpaceServiceImpl(
    private val appUserService: AppUserService,
    private val applicationUtils: ApplicationUtils,
    @Value("\${app.url.orion_context_broker}") private val contextBrokerEntitiesURL: String
) : PersonalDataSpaceService {

    private val log: Logger = LoggerFactory.getLogger(PersonalDataSpaceServiceImpl::class.java)

    override fun saveVC(vcJwt: String) {
        val userId = getUserIdFromContextAuthentication()
        val vcJson = extractVcJsonFromVcJwt(vcJwt)
        val vcId = extractVerifiableCredentialIdFromVcJson(vcJson)
        val userIdAttribute = ContextBrokerAttribute(type = STRING_FORMAT, value = userId)
        val vcJwtAttribute = ContextBrokerAttribute(type = STRING_FORMAT, value = vcJwt)
        val vcJsonAttribute = ContextBrokerAttribute(type = JSON_FORMAT, value = vcJson)
        // Log the start of the saveVC function
        log.info("Saving Verifiable Credential for user: $userId")
        // Store the VC as VC_JWT in Context Broker
        storeVcInContextBroker(
            VcContextBrokerEntity(id = vcId, type = VC_JWT, userId = userIdAttribute, vcData = vcJwtAttribute)
        )
        // Store the VC as VC_JSON in Context Broker
        storeVcInContextBroker(
            VcContextBrokerEntity(id = vcId, type = VC_JSON, userId = userIdAttribute, vcData = vcJsonAttribute)
        )
        // Log the successful completion of the saveVC function
        log.info("Verifiable Credential saved successfully for user: $userId")
    }

    private fun getUserIdFromContextAuthentication(): String {
        // Retrieve the user session using context authentication
        val userSession = appUserService.getUserWithContextAuthentication()
        // Extract and return the user ID as a string
        val userId = userSession.id!!.toString()
        // Log the user ID extraction
        log.debug("User ID extracted from context authentication: {}", userId)
        return userId
    }

    private fun extractVcJsonFromVcJwt(vcJwt: String): JsonNode {
        // Parse the vc_jwt into a readable JWT object
        val parsedVcJwt = SignedJWT.parse(vcJwt)
        // Get the payload from vc_jwt
        val jsonObject = ObjectMapper().readTree(parsedVcJwt.payload.toString())
        // Get the 'vc' claim of the payload
        val vcJson = jsonObject["vc"]
        // Log the VC JSON extraction
        log.debug("Verifiable Credential JSON extracted from VC JWT: {}", vcJson)
        return vcJson
    }

    private fun extractVerifiableCredentialIdFromVcJson(vcJson: JsonNode): String {
        // Get the ID of the Verifiable Credential and verify it is not null or empty
        val vcId = vcJson["id"].asText()
        checkIfCredentialIdIsNull(vcId)
        // Log the Verifiable Credential ID extraction
        log.debug("Verifiable Credential ID extracted: {}", vcId)
        return vcId
    }

    private fun checkIfCredentialIdIsNull(vcId: String) {
        if (vcId.isBlank()) {
            log.error("Verifiable Credential does not contain an ID")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Verifiable Credential does not contain an ID")
        }
    }

    private fun storeVcInContextBroker(contextBrokerEntity: VcContextBrokerEntity) {
        val url = contextBrokerEntitiesURL
        val requestBody = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(contextBrokerEntity)
        val headers = listOf(CONTENT_TYPE to CONTENT_TYPE_APPLICATION_JSON)
        postRequest(url=url, headers=headers, body=requestBody)
        log.info("Verifiable Credential stored in Context Broker")
    }

    override fun getUserVCsInJson(): MutableList<VcBasicDataDTO> {
        val result: MutableList<VcBasicDataDTO> = mutableListOf()
        val contextBrokerVcList = getVerifiableCredentialsByUserIdAndFormat(VC_JSON)
        contextBrokerVcList.forEach {
            val vcDataValue = it.vcData.value as LinkedHashMap<*, *>
            val jsonNode = ObjectMapper().convertValue(vcDataValue, JsonNode::class.java)
            val vcTypeList = getVcTypeListFromVcJson(jsonNode)
            result.add(
                VcBasicDataDTO(
                    id = it.id,
                    vcType = vcTypeList,
                    credentialSubject = jsonNode["credentialSubject"]
                )
            )
        }
        return result
    }

    override fun getSelectableVCsByVcTypeList(vcTypeList: List<String>): List<VcBasicDataDTO> {

        val result = mutableListOf<VcBasicDataDTO>()
        val vcListInJsonByUser = getVerifiableCredentialsByUserIdAndFormat(VC_JSON)

        vcListInJsonByUser.forEach {
            // Parse the VC stored into a JsonNode object
            val vcDataValue = it.vcData.value as LinkedHashMap<*, *>
            val jsonNode = ObjectMapper().convertValue(vcDataValue, JsonNode::class.java)

            // Create a list of the VC IDs
            val vcDataTypeList = getVcTypeListFromVcJson(jsonNode)
            // If vc_types matches with vc_types requested, build VcBasicDataDTO and store it in a List
            if (vcDataTypeList.containsAll(vcTypeList)) {
                result.add(
                    VcBasicDataDTO(
                        id = jsonNode["id"].asText(),
                        vcType = vcDataTypeList,
                        credentialSubject = jsonNode["credentialSubject"]
                    )
                )
            }
        }

        checkIfResultIsEmpty(result)

        return result
    }

    private fun checkIfResultIsEmpty(result: MutableList<VcBasicDataDTO>) {
        if (result.isEmpty()) {
            throw NoSuchVerifiableCredentialException("There is no Verifiable Credential stored in Context Broker")
        }
    }

    override fun deleteVerifiableCredential(id: String) {
        applicationUtils.deleteRequest(url = "$contextBrokerEntitiesURL/$id?type=$VC_JWT", headers = listOf())
        applicationUtils.deleteRequest(url = "$contextBrokerEntitiesURL/$id?type=$VC_JSON", headers = listOf())
    }

    fun getVerifiableCredentialsByUserIdAndFormat(format: String): MutableList<VcContextBrokerEntity> {
        val userUUID = getUserIdFromContextAuthentication()
        val response = applicationUtils.getRequest(url="$contextBrokerEntitiesURL?type=$format&user_ID=$userUUID",
            headers=listOf())
        return parseResponseBodyIntoContextBrokerVcMutableList(response)
    }

    private fun parseResponseBodyIntoContextBrokerVcMutableList(response: String): MutableList<VcContextBrokerEntity> {
        val result: MutableList<VcContextBrokerEntity> = mutableListOf()
        JSONArray(response).forEach {
            val contextBrokerVcEntityDTO = ObjectMapper().readValue(it.toString(), VcContextBrokerEntity::class.java)
            result.add(contextBrokerVcEntityDTO)
        }
        return result
    }

    private fun getVcTypeListFromVcJson(jsonNode: JsonNode): MutableList<String> {
        val result = mutableListOf<String>()
        jsonNode["type"].forEach { result.add(it.asText()) }
        return result
    }

    override fun getVerifiableCredentialByIdAndFormat(id: String, format: String): String {
        // Get user session
        val userUUID = getUserIdFromContextAuthentication()
        val response = applicationUtils.getRequest(url="$contextBrokerEntitiesURL/$id?type=$format&user_ID=$userUUID",
            headers=listOf())
        val objectMapper = ObjectMapper()
        return if (format == VC_JWT) {
            objectMapper.readValue(response, VcContextBrokerEntity::class.java).vcData.value.toString()
        } else {
            objectMapper.writeValueAsString(
                objectMapper.readValue(
                    response,
                    VcContextBrokerEntity::class.java
                ).vcData.value
            )
        }
    }

    override fun deleteVCs() {
        val userUUID = getUserIdFromContextAuthentication()
        val response = applicationUtils.getRequest(url="$contextBrokerEntitiesURL?user_ID=$userUUID", headers=listOf())
        val vcs = parseResponseBodyIntoContextBrokerVcMutableList(response)
        val vcIdList = getDistinctIds(vcs)
        // get all VCs from user
        vcIdList.forEach {
            // delete VCs by Id
            deleteVerifiableCredential(it)
        }
    }

    @Throws(InvalidDIDFormatException::class)
    override fun saveDid(did: String, didMethod: DidMethods) {
        val userId = getUserIdFromContextAuthentication()
        val userIdAttribute = ContextBrokerAttribute(type = STRING_FORMAT, value = userId)
        // Log the start of saveDID func
        log.debug("Saving the new DID $did for user: $userId")

        if (didMethod == DidMethods.DID_ELSI && !did.startsWith("did:elsi:")) {

            throw InvalidDIDFormatException("DID does not match the pattern")

        }

        storeDIDInContextBroker(
            DidContextBrokerEntity(id = did, type = didMethod.stringValue, userId = userIdAttribute)
        )

        log.info("DID Stored Successfully")
    }




    private fun storeDIDInContextBroker(didContextBrokerEntity: DidContextBrokerEntity) {
        val url = contextBrokerEntitiesURL
        val requestBody = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(didContextBrokerEntity)
        val headers = listOf(CONTENT_TYPE to CONTENT_TYPE_APPLICATION_JSON)
        applicationUtils.postRequest(url=url, body=requestBody, headers=headers)
        log.info("DID Stored in Context Broker")
    }

    override fun getDidsByUserId(): MutableList<DidResponseDTO> {
        val userUUID = getUserIdFromContextAuthentication()
        // We need to encode ^ character to avoid interpretation issues in the HTTP request
        val typePattern = URLEncoder.encode("^did", "UTF-8")
        // get all DIDs from user
        val response = applicationUtils.getRequest(
            url="$contextBrokerEntitiesURL/?typePattern=$typePattern&userId.value=$userUUID", headers=listOf())
        return parseResponseBodyIntoContextBrokerDidMutableList(response)

    }
    private fun parseResponseBodyIntoContextBrokerDidMutableList(response: String): MutableList<DidResponseDTO> {
        val result: MutableList<DidResponseDTO> = mutableListOf()
        JSONArray(response).forEach {
            val jsonObject = JSONObject(it.toString())
            val did = jsonObject.getString("id")
            val didResponseDTO = DidResponseDTO(did)
            result.add(didResponseDTO)
        }
        return result
    }

    override fun deleteSelectedDid(didResponseDTO: DidResponseDTO){
        val userUUID = getUserIdFromContextAuthentication()
        didExists(didResponseDTO,userUUID)
        applicationUtils.deleteRequest(url="$contextBrokerEntitiesURL/${didResponseDTO.did}?userId.value=$userUUID",
            headers=listOf())
    }

    private fun didExists(didResponseDTO: DidResponseDTO,userUUID: String): Boolean {
        try {
            val response = applicationUtils.getRequest(
                url="$contextBrokerEntitiesURL/${didResponseDTO.did}?userId.value=$userUUID",
                headers=listOf())
            return response.isNotEmpty()

        } catch (e: NoSuchElementException) {
            throw DIDNotFoundException("DID not found: ${didResponseDTO.did}")
        }
    }


    fun getDistinctIds(vcs: MutableList<VcContextBrokerEntity>): List<String> {
        return vcs.map { it.id }.distinct()
    }

}