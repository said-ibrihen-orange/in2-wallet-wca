package es.in2.wallet.repository
import es.in2.wallet.model.AppCredentialRequestData
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AppCredentialRequestDataRepository : CrudRepository<AppCredentialRequestData, String> {
    fun findAppCredentialRequestDataByIssuerNameAndUserId(issuerName: String, userId: UUID): Optional<AppCredentialRequestData>
}