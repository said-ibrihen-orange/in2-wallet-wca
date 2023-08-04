package es.in2.wallet.model

import jakarta.persistence.*
import java.io.Serializable
import java.util.UUID

@Entity
@IdClass(AppCredentialRequestDataId::class)
@Table(name = "app_credential_request_data")
data class AppCredentialRequestData(

        @Id
        @Column(nullable = false)
        val issuerName: String,

        @Id
        @Column(nullable = false)
        val userId: UUID?,

        @Column
        val issuerNonce: String,

        @Column(length = 2000,unique = true)
        val issuerAccessToken: String
)
/**
 * Data class representing the composite primary key for the AppCredentialRequestData entity.
 * It consists of the issuerName and userId fields, uniquely identifying each record in the table.
 */
data class AppCredentialRequestDataId(
        val issuerName: String,
        val userId: UUID?
) : Serializable {

        // Default constructor required by JPA to instantiate the class when interacting with the database.
        // This constructor initializes the fields with default values (empty string for issuerName and null for userId).
        constructor() : this("", null)
}