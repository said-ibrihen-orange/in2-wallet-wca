package es.in2.wallet.service

import es.in2.wallet.model.AppUser
import es.in2.wallet.model.dto.AppUserRequestDTO
import java.util.*

interface AppUserService {
    fun getUserWithContextAuthentication(): AppUser
    fun registerUser(appUserRequestDTO: AppUserRequestDTO)
    fun getUsers(): List<AppUser>
    fun getUserById(uuid: UUID): Optional<AppUser>
    fun getUserByUsername(username: String): Optional<AppUser>
    fun getUserByEmail(email: String): Optional<AppUser>
}

