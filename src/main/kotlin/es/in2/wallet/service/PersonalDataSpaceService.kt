package es.in2.wallet.service

import es.in2.wallet.model.AppUser
import es.in2.wallet.model.DidMethods
import es.in2.wallet.model.VCAttribute
import es.in2.wallet.model.dto.VcBasicDataDTO
import java.util.*


interface PersonalDataSpaceService {

    fun saveVC(vcJwt: String)
    fun getUserVCsInJson(): MutableList<VcBasicDataDTO>
    fun getVerifiableCredentialByIdAndFormat(id: String, format: String): VCAttribute?
    fun deleteVerifiableCredential(id: String)
    fun getSelectableVCsByVcTypeList(vcTypeList: List<String>): List<VcBasicDataDTO>
    fun deleteVCs()
    fun saveDid(did: String, didMethod: DidMethods)
    fun getDidsByUserId(): List<String>
    fun deleteSelectedDid(did: String)
    fun registerUserInContextBroker(appUser: Optional<AppUser>)



}




