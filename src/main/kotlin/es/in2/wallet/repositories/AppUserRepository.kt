package es.in2.wallet.repositories

import es.in2.wallet.domain.entities.AppUser
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AppUserRepository : CrudRepository<AppUser, UUID> {
    fun findByUsername(username: String): AppUser
    fun findByEmail(email: String): Optional<AppUser>
}