package es.in2.wallet.service

import java.util.*

interface PersonalDataSpaceService {

    fun getVcListByVcTypeList(vcTypeList: List<String>): List<String>

    fun saveVC(vcJwt: String): String


    fun getVCByFormat(vcId: String, vcFormat: String): String
    fun getVCs(): String
    fun getVCsByFormat(vcFormat: String): String
    fun deleteVC(vcId: String)
//    fun getVCsByVCTypes(userUUID: UUID, vcTypeList: List<String>): ArrayList<String>
}




