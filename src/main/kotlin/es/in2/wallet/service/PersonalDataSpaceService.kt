package es.in2.wallet.service

import es.in2.wallet.model.DidMethods
import es.in2.wallet.model.dto.DidResponseDTO
import es.in2.wallet.model.dto.VcBasicDataDTO


interface PersonalDataSpaceService {

    fun saveVC(vcJwt: String)
    fun getUserVCsInJson(): MutableList<VcBasicDataDTO>
    fun getVerifiableCredentialByIdAndFormat(id: String, format: String): String
    fun deleteVerifiableCredential(id: String)
    fun getSelectableVCsByVcTypeList(vcTypeList: List<String>): List<VcBasicDataDTO>
    fun deleteVCs()
    fun saveDid(did: String, didMethod: DidMethods)
    fun getDidsByUserId(): MutableList<DidResponseDTO>



}




