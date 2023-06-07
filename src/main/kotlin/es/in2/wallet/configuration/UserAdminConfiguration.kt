package es.in2.wallet.configuration

import es.in2.wallet.model.AppUser
import es.in2.wallet.repository.AppUserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

@Configuration
class UserAdminConfiguration {

    private val defaultUsername = "in2admin"
    private val defaultEmail = "in2admin@example.com"
    private val defaultPassword = "in2pass"

    @Bean
    fun defaultUserAdmin(appUserRepository: AppUserRepository): AppUser {
        val existingUser = appUserRepository.findAppUserByEmail(defaultEmail)
        return if (existingUser.isPresent) {
            existingUser.get()
        } else {
            val encodedPassword = BCryptPasswordEncoder().encode(defaultPassword)
            val adminUser = AppUser(
                id = UUID.randomUUID(),
                username = defaultUsername,
                email = defaultEmail,
                password = encodedPassword
            )
            appUserRepository.save(adminUser)
            adminUser
        }
    }

}
