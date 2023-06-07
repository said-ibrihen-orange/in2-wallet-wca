package es.in2.wallet.repository

import es.in2.wallet.model.AppUser
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AppUserRepository : CrudRepository<AppUser, UUID> {
    fun findAppUserByUsername(username: String): Optional<AppUser>
    fun findAppUserByEmail(email: String): Optional<AppUser>
}