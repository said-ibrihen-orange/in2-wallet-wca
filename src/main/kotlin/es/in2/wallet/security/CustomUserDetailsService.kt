package es.in2.wallet.security

import es.in2.wallet.model.AppUser
import es.in2.wallet.service.AppUserService
import es.in2.wallet.util.USER_ROLE
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import java.util.*
import kotlin.collections.ArrayList

@Slf4j
@Component
class CustomUserDetailsService(
    private val appUserService: AppUserService
) : UserDetailsService {

    private val log: Logger = LoggerFactory.getLogger(CustomUserDetailsService::class.java)

    override fun loadUserByUsername(username: String): UserDetails {
        log.debug("CustomUserDetailsService.loadUserByUsername()")
        val userFound = checkIfUserExists(username)
        val authorities = ArrayList<GrantedAuthority>()
        // For now, we do not have roles, but we set USER as default
        authorities.add(SimpleGrantedAuthority(USER_ROLE))
        return User(userFound.username, userFound.password, authorities)
    }

    private fun checkIfUserExists(username: String): AppUser {
        log.debug("CustomUserDetailsService.checkIfUserExists()")
        val userFound = appUserService.getUserByUsername(username)
        if (userFound.isPresent) {
            return userFound.get()
        } else {
            throw NoSuchElementException("The username $username does not exist.")
        }
    }

}