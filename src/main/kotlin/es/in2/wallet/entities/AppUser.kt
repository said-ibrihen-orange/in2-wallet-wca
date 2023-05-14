package es.in2.wallet.entities
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "app_users")
data class AppUser(
    @Id @GeneratedValue(strategy = GenerationType.UUID) val id: UUID?,
    @Column(unique = true, nullable = false)
    val username: String,

){

}
