package es.in2.wallet.services

import es.in2.wallet.entities.AppUser
import es.in2.wallet.repositories.AppUserRepository
import org.springframework.stereotype.Service
import java.util.*

interface AppUserService {
    fun getUserByUsername(username: String): AppUser?
    fun saveUser(appUser: AppUser)
    fun deleteUsers()
    fun getUsers(): List<AppUser>
}

@Service
class AppUserServiceImpl(
    private val appUserRepository: AppUserRepository
) : AppUserService {

    override fun getUserByUsername(username: String): AppUser? {
        return appUserRepository.findByUsername(username)
    }

    override fun saveUser(appUser: AppUser) {
        appUserRepository.save(appUser)
    }

    override fun deleteUsers() {
        appUserRepository.deleteAll()
    }

    override fun getUsers(): List<AppUser> {
        return listOf(appUserRepository.findAll()).flatten()
    }


}