package es.in2.wallet.entities
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "app_users")
class AppUser(
    @Id @GeneratedValue(strategy = GenerationType.UUID) val id: UUID?,
    val username: String,

){
    constructor(username: String):this(null,username){
    }
}
