package es.in2.wallet.model

import jakarta.persistence.*

@Entity
@Table(name = "app_issuer_data")
data class AppIssuerData(

        @Id
        @Column(unique = true, nullable = false)
        val issuerName: String,

        @Column(columnDefinition = "json", nullable = false)
        val issuerMetadata: String

)
