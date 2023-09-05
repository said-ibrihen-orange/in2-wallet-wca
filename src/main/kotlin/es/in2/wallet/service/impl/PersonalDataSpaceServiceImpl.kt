package es.in2.wallet.service.impl

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jwt.SignedJWT
import es.in2.wallet.exception.NoSuchVerifiableCredentialException
import es.in2.wallet.exception.UserNotFoundException
import es.in2.wallet.model.*
import es.in2.wallet.model.dto.VcBasicDataDTO
import es.in2.wallet.service.AppUserService
import es.in2.wallet.service.PersonalDataSpaceService
import es.in2.wallet.util.*
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
        val userId = getUserIdFromContextAuthentication()
        val vcJson = extractVcJsonFromVcJwt(vcJwt)
        val vcId = extractVerifiableCredentialIdFromVcJson(vcJson)
        // Fetch current user entity from Context Broker
        val userEntity : NGSILDUserEntity = getUserEntityFromContextBroker(userId)
        // Create new VC entity in both format
        val newVCJwt = VCAttribute(id = vcId, type = VC_JWT, value = vcJwt)
        val newVCJson = VCAttribute(id = vcId, type = VC_JSON, value = vcJson)
        // Add the new VC to the list
        val updatedVCs = userEntity.vcs.value.toMutableList()
        updatedVCs.add(newVCJwt)
        updatedVCs.add(newVCJson)
        val vcs = NGSILDAttribute(value = updatedVCs.toList())
        val updatedUserEntity = (
                NGSILDUserEntity(
                        id = userEntity.id,
                        userData = userEntity.userData,
                        vcs = vcs,
                        dids = userEntity.dids))

        // PATCH updated user entity back to Context Broker
        updateUserEntityInContextBroker(updatedUserEntity)

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

    override fun getUserVCsInJson(): MutableList<VcBasicDataDTO> {
        val result: MutableList<VcBasicDataDTO> = mutableListOf()
        val contextBrokerVcList = getVerifiableCredentialsByUserIdAndFormat(VC_JSON)
        contextBrokerVcList.forEach {
            val vcDataValue = it.value as LinkedHashMap<*, *>
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
            val vcDataValue = it.value as LinkedHashMap<*, *>
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
        val userId = getUserIdFromContextAuthentication()

        // Fetch  current user entity from Context Broker
        val userEntity: NGSILDUserEntity = getUserEntityFromContextBroker(userId)

        // Filter out the VC entities with the given ID
        val updatedVCs = userEntity.vcs.value.filterNot { it.id == id }

        val updatedUserEntity = NGSILDUserEntity(
                id = userEntity.id,
                userData = userEntity.userData,
                vcs = NGSILDAttribute(value = updatedVCs),
                dids = userEntity.dids
        )

        // PATCH updated user entity back to Context Broker
        updateUserEntityInContextBroker(updatedUserEntity)

        log.info("Verifiable Credential with ID: $id deleted successfully for user: $userId")
    }

    fun getVerifiableCredentialsByUserIdAndFormat(format: String): List<VCAttribute> {
        val userId = getUserIdFromContextAuthentication()
        // Fetch current user entity from Context Broker
        val userEntity: NGSILDUserEntity = getUserEntityFromContextBroker(userId)
        return userEntity.vcs.value.filter { it.type == format }
    }



    private fun getVcTypeListFromVcJson(jsonNode: JsonNode): MutableList<String> {
        val result = mutableListOf<String>()
        jsonNode["type"].forEach { result.add(it.asText()) }
        return result
    }

    override fun getVerifiableCredentialByIdAndFormat(id: String, format: String): VCAttribute? {
        val userId = getUserIdFromContextAuthentication()
        // Fetch  current user entity from Context Broker
        val userEntity: NGSILDUserEntity = getUserEntityFromContextBroker(userId)
        return userEntity.vcs.value.firstOrNull { vc ->
            vc.id == id && vc.type == format
        }
    }

    override fun deleteVCs() {
        // Fetch the userId from the authentication context
        val userId = getUserIdFromContextAuthentication()

        // Fetch the current user entity from the Context Broker
        val userEntity: NGSILDUserEntity = getUserEntityFromContextBroker(userId)

        // Clear all the verifiable credentials
        val emptyVCs = NGSILDAttribute(value = listOf<VCAttribute>())
        val updatedUserEntity = NGSILDUserEntity(
                id = userEntity.id,
                userData = userEntity.userData,
                vcs = emptyVCs,
                dids = userEntity.dids
        )

        // Update the user entity back to the Context Broker without VCs
        updateUserEntityInContextBroker(updatedUserEntity)

        log.info("All Verifiable Credentials deleted successfully for user: $userId")
    }

    override fun saveDid(did: String, didMethod: DidMethods) {
        // Fetch the userId from the authentication context
        val userId = getUserIdFromContextAuthentication()

        // Fetch the current user entity from the Context Broker
        val userEntity: NGSILDUserEntity = getUserEntityFromContextBroker(userId)

        // Create new DidAttribute for the provided DID
        val newDid = DidAttribute(type = didMethod.stringValue, value = did)

        // Add the new DID to the list of existing DIDs
        val updatedDids = userEntity.dids.value.toMutableList()
        updatedDids.add(newDid)

        val dids = NGSILDAttribute(value = updatedDids.toList())
        val updatedUserEntity = NGSILDUserEntity(
                id = userEntity.id,
                userData = userEntity.userData,
                vcs = userEntity.vcs,
                dids = dids
        )

        // Update the user entity back to the Context Broker with the new DID
        updateUserEntityInContextBroker(updatedUserEntity)

        log.info("DID saved successfully for user: $userId")

    }



    override fun getDidsByUserId(): List<String> {
        // Fetch the userId from the authentication context
        val userId = getUserIdFromContextAuthentication()

        // Fetch the current user entity from the Context Broker
        val userEntity: NGSILDUserEntity = getUserEntityFromContextBroker(userId)

        // Extract the DIDs from the user entity and return them as a list of strings
        return userEntity.dids.value.map { it.value }
    }


    override fun deleteSelectedDid(did: String) {
        // Fetch the userId from the authentication context
        val userId = getUserIdFromContextAuthentication()

        // Fetch the current user entity from the Context Broker
        val userEntity: NGSILDUserEntity = getUserEntityFromContextBroker(userId)

        // Remove the specific DID from the user entity's DIDs list
        val updatedDids = userEntity.dids.value.filterNot { it.value == did }

        // Create the updated DID attribute
        val dids = NGSILDAttribute(value = updatedDids)

        // Construct the updated user entity
        val updatedUserEntity = NGSILDUserEntity(
                id = userEntity.id,
                userData = userEntity.userData,
                vcs = userEntity.vcs,
                dids = dids
        )

        // Update the user entity in the Context Broker
        updateUserEntityInContextBroker(updatedUserEntity)

        log.info("Deleted DID: $did for user: $userId")
    }

    override fun registerUserInContextBroker(appUser: Optional<AppUser>) {
        if (appUser.isPresent) {
            val appUserPresent = appUser.get()
            val userEntity = NGSILDUserEntity(
                id = "urn:entities:userId:" + appUserPresent.id.toString(),
                userData = NGSILDAttribute(value = UserAttribute(username = appUserPresent.username, email = appUserPresent.email)),
                dids = NGSILDAttribute(value = emptyList()),
                vcs = NGSILDAttribute(value = emptyList())
            )
            storeUserInContextBroker(userEntity)
            log.debug("entity saved")
        } else {
            throw UserNotFoundException("User not found")
        }
    }
    private fun storeUserInContextBroker(userEntity: NGSILDUserEntity) {
        val url = contextBrokerEntitiesURL
        val requestBody = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(userEntity)
        log.info(requestBody)
        val headers = listOf(CONTENT_TYPE to CONTENT_TYPE_APPLICATION_JSON)
        applicationUtils.postRequest(url=url, headers=headers, body=requestBody)
    }

    private fun getUserEntityFromContextBroker(userId: String): NGSILDUserEntity {
        val url = "$contextBrokerEntitiesURL/urn:entities:userId:$userId"
        val response = applicationUtils.getRequest(url=url, headers= listOf())
        val objectMapper = ObjectMapper().apply {
            //  Enable deserialization of a single object as a list
            enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        }
        val valueTypeRef = objectMapper.typeFactory.constructType(NGSILDUserEntity::class.java)
        val userEntity: NGSILDUserEntity = objectMapper.readValue(response, valueTypeRef)
        log.debug("User Entity: {}", userEntity)
        return userEntity
    }


    private fun updateUserEntityInContextBroker(userEntity: NGSILDUserEntity) {
        val userId = getUserIdFromContextAuthentication()
        val url = "$contextBrokerEntitiesURL/urn:entities:userId:$userId/attrs"
        val requestBody = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(userEntity)
        val headers = listOf(CONTENT_TYPE to CONTENT_TYPE_APPLICATION_JSON)
        applicationUtils.patchRequest(url=url, headers=headers, body=requestBody)
    }
}