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

        //Do not remove the columnDefinition annotation to prevent MySQL data truncation
        @Column(nullable = false, columnDefinition = "json")
        val metadata: String

)
