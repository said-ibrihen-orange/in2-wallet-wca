package es.in2.wallet.repository
import es.in2.wallet.model.AppCredentialRequestData
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AppCredentialRequestDataRepository : CrudRepository<AppCredentialRequestData, UUID> {
    fun findAppCredentialRequestDataByIssuerNameAndUserId(issuerName: String, userId: String): Optional<AppCredentialRequestData>
}