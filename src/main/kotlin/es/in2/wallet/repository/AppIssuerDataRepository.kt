package es.in2.wallet.repository

import es.in2.wallet.model.AppIssuerData
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AppIssuerDataRepository : CrudRepository<AppIssuerData, String> {
    fun findAppIssuerDataByIssuerName(issuerName: String): Optional<AppIssuerData>
}