package es.in2.wallet.service

import java.util.*
import kotlin.collections.ArrayList

interface PersonalDataSpaceService {

    fun getVerifiableCredentialsByVcType(userUUID: UUID, vcTypeList: List<String>): List<String>

    fun saveVC(userUUID: UUID, vc: String): String

    // TODO to refactor

    fun getVCByFormat(userUUID: UUID, vcId:String, vcFormat: String): String
    fun getVCs(userUUID: UUID): String
    fun getVCsByFormat(userUUID: UUID, vcFormat: String): String
    fun deleteVC(userUUID: UUID, vcId: String)
    fun getVCsByVCTypes(userUUID: UUID, vcTypeList: List<String>): ArrayList<String>
}




