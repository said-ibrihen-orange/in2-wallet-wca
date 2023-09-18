package es.in2.wallet.integration.orionLD.service

import com.fasterxml.jackson.databind.JsonNode
import es.in2.wallet.api.model.entity.AppUser
import es.in2.wallet.wca.model.dto.VcBasicDataDTO
import es.in2.wallet.wca.model.entity.DidMethods
import es.in2.wallet.wca.model.repository.VCRequestData
import java.util.*

interface OrionLDService {

    fun saveVC(vcJwt: String)
    fun getUserVCsInJson(): MutableList<VcBasicDataDTO>
    fun getVerifiableCredentialByIdAndFormat(id: String, format: String): String
    fun deleteVerifiableCredential(id: String)
    fun getSelectableVCsByVcTypeList(vcTypeList: List<String>): List<VcBasicDataDTO>
    fun deleteVCs()
    fun saveDid(did: String, didMethod: DidMethods)
    fun getDidsByUserId(): List<String>
    fun deleteSelectedDid(did: String)
    fun registerUserInContextBroker(appUser: Optional<AppUser>)
    fun saveIssuer(issuer: String, data: JsonNode)
    fun getIssuersByUserId(): List<String>
    fun getIssuerDataByIssuerName(issuer: String): JsonNode
    fun saveCredentialRequestData(issuer: String, nonce: String, token: String)
    fun updateNonceOnCredentialRequestData(issuer: String,nonce: String)

    fun getCredentialRequestData(issuer: String): VCRequestData

}