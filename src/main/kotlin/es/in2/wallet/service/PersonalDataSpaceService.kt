package es.in2.wallet.service

interface PersonalDataSpaceService {
    fun saveVC(vcJwt: String)
    fun getAllVerifiableCredentials(): MutableList<String>

    fun getAllVerifiableCredentialsByFormat(vcFormat: String): MutableList<String>

    fun getVerifiableCredentialByIdAndFormat(id: String, format: String): String

    fun deleteVerifiableCredential(id: String)
    fun getVcIdListByVcTypeList(vcTypeList: List<String>): List<String>

}




