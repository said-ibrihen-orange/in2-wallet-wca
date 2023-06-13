package es.in2.wallet.service

import es.in2.wallet.model.dto.contextBroker.VerifiableCredentialEntityContextBrokerDTO

interface PersonalDataSpaceService {
    fun saveVC(vcJwt: String)
    fun getAllVerifiableCredentials(): MutableList<VerifiableCredentialEntityContextBrokerDTO>

    fun getAllVerifiableCredentialsByFormat(vcFormat: String): MutableList<VerifiableCredentialEntityContextBrokerDTO>

    fun getVerifiableCredentialByIdAndFormat(id: String, format: String): VerifiableCredentialEntityContextBrokerDTO

    fun deleteVerifiableCredential(id: String)
    fun getVcIdListByVcTypeList(vcTypeList: List<String>): List<String>

}




