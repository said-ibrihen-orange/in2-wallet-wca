package es.in2.wallet.service

interface PersonalDataSpaceService {
    fun saveVC(vcJwt: String)
    fun getAllVerifiableCredentialsByAppUser(): MutableList<String>
    fun getVcIdListByVcTypeList(vcTypeList: List<String>): List<String>
    fun getVcByFormat(vcId: String, vcFormat: String): String
    fun getVcListByFormat(vcFormat: String): String
    fun deleteVC(id: String)
}




