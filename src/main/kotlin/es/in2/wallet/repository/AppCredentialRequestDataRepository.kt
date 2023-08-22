package es.in2.wallet.repository
import es.in2.wallet.model.AppCredentialRequestData
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AppCredentialRequestDataRepository : CrudRepository<AppCredentialRequestData, UUID> {
    fun findAppCredentialRequestDataByIssuerNameAndUserId(issuerName: String, userId: String): Optional<AppCredentialRequestData>
    @Modifying
    @Query("UPDATE AppCredentialRequestData c SET c.issuerNonce = :freshNonce WHERE c.issuerName = :issuerName AND c.userId = :userId")
    fun updateIssuerNonceWithNewValueByIssuerNameAndUserId(
            @Param("issuerName") issuerName: String,
            @Param("userId") userId: String,
            @Param("freshNonce") freshNonce: String
    )


}