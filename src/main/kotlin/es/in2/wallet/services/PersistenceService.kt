package es.in2.wallet.services

import org.springframework.stereotype.Service

interface PersistenceService {
    fun saveVC(vc: String)
    fun getVCs(userid: String): String
    fun deleteVC(userid: String, vcId: String)

}

@Service
class PersistenceServiceImpl:PersistenceService{
    override fun saveVC(vc: String) {
        TODO()
    }

    override fun getVCs(userid: String): String {
        TODO()
    }

    override fun deleteVC(userid: String, vcId: String) {
        TODO()
    }
}