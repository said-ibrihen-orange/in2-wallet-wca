package es.in2.wallet.model

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "app_credential_request_data")
data class AppCredentialRequestData(

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        val id: UUID?,

        @Column(nullable = false)
        val issuerName: String,

        @Column(nullable = false)
        val userId: String,

        @Column(length = 50)
        val issuerNonce: String,

        @Column(length = 2000,unique = true)
        val issuerAccessToken: String
)

