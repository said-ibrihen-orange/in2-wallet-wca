package es.in2.wallet.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "app_issuer_data")
data class AppIssuerData(

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        val id: UUID?,

        @Column(unique = true, nullable = false)
        val name: String,

        @Column(columnDefinition = "json", nullable = false)
        val metadata: String

)
