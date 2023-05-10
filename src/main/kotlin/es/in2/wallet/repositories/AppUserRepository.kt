package es.in2.wallet.repositories

import es.in2.wallet.entities.AppUser
import org.springframework.data.repository.CrudRepository
import java.util.*

interface AppUserRepository : CrudRepository<AppUser, UUID> {
    fun findByUsername(username: String): AppUser?
}