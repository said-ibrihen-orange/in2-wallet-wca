package es.in2.wallet.services

import es.in2.wallet.domain.entities.AppUser
import es.in2.wallet.repositories.AppUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

interface AppUserService {
    fun getUserByUsername(username: String): AppUser?
    fun getUserById(uuid: UUID): Optional<AppUser>
    fun saveUser(appUser: AppUser): UUID?
    fun deleteUsers()
    fun getUsers(): List<AppUser>
    fun registerUser(username: String):UUID
}

@Service
class AppUserServiceImpl(
    @Autowired
  private val appUserRepository: AppUserRepository
) : AppUserService {

    override fun getUserByUsername(username: String): AppUser? {
        return appUserRepository.findByUsername(username)
    }

    override fun getUserById(uuid: UUID): Optional<AppUser> {
        println(appUserRepository.findAll())
        return appUserRepository.findById(uuid)
    }

    override fun saveUser(appUser: AppUser): UUID? {
        return appUserRepository.save(appUser).id

    }

    override fun deleteUsers() {
        appUserRepository.deleteAll()
    }

    override fun getUsers(): List<AppUser> {
        return listOf(appUserRepository.findAll()).flatten()
    }

    override fun registerUser(username: String): UUID {
        // Check if user exists
        if (this.getUserByUsername(username) != null) {
            throw Exception("User already exists")
        }
        val uuid = UUID.randomUUID()
        // Save user
        return this.saveUser(AppUser(uuid,username,"example.com"))!!

    }


}