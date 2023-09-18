package es.in2.wallet.integration.orionLD.service.impl

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jwt.SignedJWT
import es.in2.wallet.api.exception.NoSuchVerifiableCredentialException
import es.in2.wallet.api.model.entity.AppUser
import es.in2.wallet.api.service.AppUserService
import es.in2.wallet.api.util.*
import es.in2.wallet.integration.orionLD.model.*
import es.in2.wallet.integration.orionLD.service.OrionLDService
import es.in2.wallet.wca.model.dto.VcBasicDataDTO
import es.in2.wallet.wca.model.entity.DidMethods
import es.in2.wallet.wca.model.repository.CacheStore
import es.in2.wallet.wca.model.repository.UserIssuerKey
import es.in2.wallet.wca.model.repository.VCRequestData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*
@Service
class OrionLDServiceImpl(
    private val appUserService: AppUserService,
    private val applicationUtils: ApplicationUtils,
    private val cacheStore: CacheStore<UserIssuerKey, VCRequestData>,
    @Value("\${app.url.orion_context_broker}") private val contextBrokerEntitiesURL: String
) : OrionLDService {

    private val log: Logger = LoggerFactory.getLogger(OrionLDServiceImpl::class.java)

    override fun saveVC(vcJwt: String) {
        val userId = getUserIdFromContextAuthentication()
        val vcJson = extractVcJsonFromVcJwt(vcJwt)
        val vcId = extractVerifiableCredentialIdFromVcJson(vcJson)
        // Fetch current user entity from Context Broker
        val userEntity : UserEntity = getUserEntityFromContextBroker(userId)
        // Create new VC entity in both format
        val newVCJwt = VCAttribute(id = vcId, type = VC_JWT, value = vcJwt)
        val newVCJson = VCAttribute(id = vcId, type = VC_JSON, value = vcJson)
        // Add the new VC to the list
        val updatedVCs = userEntity.vcs.value.toMutableList()
        updatedVCs.add(newVCJwt)
        updatedVCs.add(newVCJson)
        val vcs = EntityAttribute(value = updatedVCs.toList())
        val updatedUserEntity = (
                UserEntity(
                    id = userEntity.id,
                    userData = userEntity.userData,
                    vcs = vcs,
                    issuers = userEntity.issuers,
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
        val userEntity: UserEntity = getUserEntityFromContextBroker(userId)

        // Filter out the VC entities with the given ID
        val updatedVCs = userEntity.vcs.value.filterNot { it.id == id }

        val updatedUserEntity = UserEntity(
            id = userEntity.id,
            userData = userEntity.userData,
            vcs = EntityAttribute(value = updatedVCs),
            issuers = userEntity.issuers,
            dids = userEntity.dids
        )

        // PATCH updated user entity back to Context Broker
        updateUserEntityInContextBroker(updatedUserEntity)

        log.info("Verifiable Credential with ID: $id deleted successfully for user: $userId")
    }

    fun getVerifiableCredentialsByUserIdAndFormat(format: String): List<VCAttribute> {
        val userId = getUserIdFromContextAuthentication()
        // Fetch current user entity from Context Broker
        val userEntity: UserEntity = getUserEntityFromContextBroker(userId)
        return userEntity.vcs.value.filter { it.type == format }
    }



    private fun getVcTypeListFromVcJson(jsonNode: JsonNode): MutableList<String> {
        val result = mutableListOf<String>()
        jsonNode["type"].forEach { result.add(it.asText()) }
        return result
    }

    override fun getVerifiableCredentialByIdAndFormat(id: String, format: String): String {
        val userId = getUserIdFromContextAuthentication()
        // Fetch  current user entity from Context Broker
        val userEntity: UserEntity = getUserEntityFromContextBroker(userId)
        val vcAttribute = userEntity.vcs.value.firstOrNull { vc ->
            vc.id == id && vc.type == format
        } ?: throw NoSuchElementException("No VCAttribute found for id $id and format $format for user $userId")

        // Convert the 'value' of VCAttribute to String
        return when (vcAttribute.value) {
            is String -> vcAttribute.value as String
            else -> {
                // Convert complex objects to String (JSON format) using ObjectMapper
                val objectMapper = ObjectMapper()
                objectMapper.writeValueAsString(vcAttribute.value)
            }
        }
    }

    override fun deleteVCs() {
        // Fetch the userId from the authentication context
        val userId = getUserIdFromContextAuthentication()

        // Fetch the current user entity from the Context Broker
        val userEntity: UserEntity = getUserEntityFromContextBroker(userId)

        // Clear all the verifiable credentials
        val emptyVCs = EntityAttribute(value = listOf<VCAttribute>())
        val updatedUserEntity = UserEntity(
            id = userEntity.id,
            userData = userEntity.userData,
            vcs = emptyVCs,
            issuers = userEntity.issuers,
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
        val userEntity: UserEntity = getUserEntityFromContextBroker(userId)

        // Create new DidAttribute for the provided DID
        val newDid = DidAttribute(type = didMethod.stringValue, value = did)

        // Add the new DID to the list of existing DIDs
        val updatedDids = userEntity.dids.value.toMutableList()
        updatedDids.add(newDid)

        val dids = EntityAttribute(value = updatedDids.toList())
        val updatedUserEntity = UserEntity(
            id = userEntity.id,
            userData = userEntity.userData,
            vcs = userEntity.vcs,
            issuers = userEntity.issuers,
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
        val userEntity: UserEntity = getUserEntityFromContextBroker(userId)

        // Extract the DIDs from the user entity and return them as a list of strings
        return userEntity.dids.value.map { it.value }
    }


    override fun deleteSelectedDid(did: String) {
        // Fetch the userId from the authentication context
        val userId = getUserIdFromContextAuthentication()

        // Fetch the current user entity from the Context Broker
        val userEntity: UserEntity = getUserEntityFromContextBroker(userId)

        // Remove the specific DID from the user entity's DIDs list
        val updatedDids = userEntity.dids.value.filterNot { it.value == did }

        // Create the updated DID attribute
        val dids = EntityAttribute(value = updatedDids)

        // Construct the updated user entity
        val updatedUserEntity = UserEntity(
            id = userEntity.id,
            userData = userEntity.userData,
            vcs = userEntity.vcs,
            issuers = userEntity.issuers,
            dids = dids
        )

        // Update the user entity in the Context Broker
        updateUserEntityInContextBroker(updatedUserEntity)

        log.info("Deleted DID: $did for user: $userId")
    }

    override fun saveIssuer(issuer: String, data: JsonNode) {
        // Fetch the userId from the authentication context
        val userId = getUserIdFromContextAuthentication()

        // Fetch the current user entity from the Context Broker
        val userEntity: UserEntity = getUserEntityFromContextBroker(userId)

        // Create new IssuerAttribute
        val newIssuer = IssuerAttribute(issuer = issuer, data = data)

        // Add the new issuer to the list of existing issuers
        val updatedIssuers = userEntity.issuers.value.toMutableList()
        updatedIssuers.add(newIssuer)

        val issuers = EntityAttribute(value = updatedIssuers.toList())
        val updatedUserEntity = UserEntity(
            id = userEntity.id,
            userData = userEntity.userData,
            vcs = userEntity.vcs,
            issuers = issuers,
            dids = userEntity.dids
        )

        // Update the user entity back to the Context Broker with the new issuer
        updateUserEntityInContextBroker(updatedUserEntity)

        log.info("DID saved successfully for user: $userId")

    }

    override fun getIssuersByUserId(): List<String> {
        // Fetch the userId from the authentication context
        val userId = getUserIdFromContextAuthentication()

        // Fetch the current user entity from the Context Broker
        val userEntity: UserEntity = getUserEntityFromContextBroker(userId)

        // Extract the issuers from the user entity and return them as a list of strings
        return userEntity.issuers.value.map { it.issuer }
    }

    override fun getIssuerDataByIssuerName(issuer: String): JsonNode {
        // Fetch the userId from the authentication context
        val userId = getUserIdFromContextAuthentication()

        // Fetch the current user entity from the Context Broker
        val userEntity: UserEntity = getUserEntityFromContextBroker(userId)
        val data = userEntity.issuers.value.firstOrNull { it.issuer == issuer }?.data
        return data ?: throw NoSuchElementException("No issuer found with name $issuer for user $userId")
    }
    override fun saveCredentialRequestData(issuer: String, nonce: String, token: String,) {
        val userId = getUserIdFromContextAuthentication()
        cacheStore.add(UserIssuerKey(userId,issuer), VCRequestData(nonce,token))
    }

    override fun updateNonceOnCredentialRequestData(issuer: String, nonce: String) {
        val userId = getUserIdFromContextAuthentication()
        val currentData = getCredentialRequestData(issuer)
        // Create a new VCRequestData with the new nonce and the current token
        val updatedData = VCRequestData(nonce = nonce, token = currentData.token)

        // Update the cache with the new value
        cacheStore.add(UserIssuerKey(userId, issuer), updatedData)
    }
    override fun getCredentialRequestData(issuer: String): VCRequestData{
        val userId = getUserIdFromContextAuthentication()
        return cacheStore.get(UserIssuerKey(userId, issuer))
            ?: throw NoSuchElementException("Cache is expired.")
    }
    override fun registerUserInContextBroker(appUser: Optional<AppUser>) {
        if (appUser.isPresent) {
            val appUserPresent = appUser.get()
            val userEntity = UserEntity(
                id = "urn:entities:userId:" + appUserPresent.id.toString(),
                userData = EntityAttribute(value = UserAttribute(username = appUserPresent.username, email = appUserPresent.email)),
                dids = EntityAttribute(value = emptyList()),
                vcs = EntityAttribute(value = emptyList()),
                issuers = EntityAttribute(value = emptyList())
            )
            storeUserInContextBroker(userEntity)
            log.debug("entity saved")
        } else {
            throw UsernameNotFoundException("User not found")
        }
    }



    private fun storeUserInContextBroker(userEntity: UserEntity) {
        val url = contextBrokerEntitiesURL
        val requestBody = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(userEntity)
        log.info(requestBody)
        val headers = listOf(CONTENT_TYPE to CONTENT_TYPE_APPLICATION_JSON)
        applicationUtils.postRequest(url=url, headers=headers, body=requestBody)
    }

    private fun getUserEntityFromContextBroker(userId: String): UserEntity {
        val url = "$contextBrokerEntitiesURL/urn:entities:userId:$userId"
        val response = applicationUtils.getRequest(url=url, headers= listOf())
        val objectMapper = ObjectMapper().apply {
            //  Enable deserialization of a single object as a list
            enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        }
        val valueTypeRef = objectMapper.typeFactory.constructType(UserEntity::class.java)
        val userEntity: UserEntity = objectMapper.readValue(response, valueTypeRef)
        log.debug("User Entity: {}", userEntity)
        return userEntity
    }


    private fun updateUserEntityInContextBroker(userEntity: UserEntity) {
        val userId = getUserIdFromContextAuthentication()
        val url = "$contextBrokerEntitiesURL/urn:entities:userId:$userId/attrs"
        val requestBody = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(userEntity)
        val headers = listOf(CONTENT_TYPE to CONTENT_TYPE_APPLICATION_JSON)
        applicationUtils.patchRequest(url=url, headers=headers, body=requestBody)
    }
}