package es.in2.wallet.services

import es.in2.wallet.exception.EmailAlreadyExistsException
import es.in2.wallet.exception.UsernameAlreadyExistsException
import es.in2.wallet.model.AppUser
import es.in2.wallet.model.dto.AppUserRequestDTO
import es.in2.wallet.repository.AppUserRepository
import es.in2.wallet.service.AppUserService
import es.in2.wallet.service.impl.AppUserServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
class AppUserServiceImplTest {

    private val appUserRepository: AppUserRepository = mock(AppUserRepository::class.java)
    private val appUserService: AppUserService = AppUserServiceImpl(appUserRepository)

    @Test
    fun testRegisterUser() {
        val appUserRequestDTO = AppUserRequestDTO(username = "jdoe", email = "jdoe@example.com", password = "1234")
        val appUser = AppUser(UUID.randomUUID(), "jdoe", "jdoe@example.com", "hashedPassword")
        `when`(appUserRepository.findAppUserByUsername(appUserRequestDTO.username)).thenReturn(Optional.empty())
        `when`(appUserRepository.findAppUserByEmail(appUserRequestDTO.email)).thenReturn(Optional.empty())
        `when`(appUserRepository.save(any(AppUser::class.java))).thenReturn(appUser)
        appUserService.registerUser(appUserRequestDTO)
        verify(appUserRepository).findAppUserByUsername(appUserRequestDTO.username)
        verify(appUserRepository).findAppUserByEmail(appUserRequestDTO.email)
        verify(appUserRepository).save(any(AppUser::class.java))
    }

    @Test
    fun testRegisterUser_UsernameAlreadyExists() {
        val appUserRequestDTO = AppUserRequestDTO(username = "jdoe", email = "jdoe@example.com", password = "1234")
        val existingUser = AppUser(UUID.randomUUID(), "jdoe", "jdoe@example.com", "hashedPassword")
        `when`(appUserRepository.findAppUserByUsername(appUserRequestDTO.username)).thenReturn(Optional.of(existingUser))
        try {
            appUserService.registerUser(appUserRequestDTO)
        } catch (e: UsernameAlreadyExistsException) {
            assertThat(e.message).isEqualTo("Username already exists: ${appUserRequestDTO.username}")
        }
        verify(appUserRepository).findAppUserByUsername(appUserRequestDTO.username)
        verifyNoMoreInteractions(appUserRepository)
    }

    @Test
    fun testRegisterUser_EmailAlreadyExists() {
        val appUserRequestDTO = AppUserRequestDTO(username = "jdoe", email = "jdoe@example.com", password = "1234")
        val existingUser = AppUser(UUID.randomUUID(), "jdoe", "jdoe@example.com", "hashedPassword")
        `when`(appUserRepository.findAppUserByUsername(appUserRequestDTO.username)).thenReturn(Optional.empty())
        `when`(appUserRepository.findAppUserByEmail(appUserRequestDTO.email)).thenReturn(Optional.of(existingUser))
        try {
            appUserService.registerUser(appUserRequestDTO)
        } catch (e: EmailAlreadyExistsException) {
            assertThat(e.message).isEqualTo("Email already exists: ${appUserRequestDTO.email}")
        }
        verify(appUserRepository).findAppUserByUsername(appUserRequestDTO.username)
        verify(appUserRepository).findAppUserByEmail(appUserRequestDTO.email)
        verifyNoMoreInteractions(appUserRepository)
    }

    @Test
    fun testGetUsers() {
        // Mock the behavior of the appUserRepository
        val user1 = AppUser(UUID.randomUUID(), "user1", "user1@example.com", "password1")
        val user2 = AppUser(UUID.randomUUID(), "user2", "user2@example.com", "password2")
        val userList = listOf(user1, user2)
        `when`(appUserRepository.findAll()).thenReturn(userList)
        // Call the getUsers method
        val result = appUserService.getUsers()
        // Verify the result
        Assertions.assertEquals(userList, result)
    }

    @Test
    fun testGetUserById() {
        // Mock the behavior of the appUserRepository
        val userId = UUID.randomUUID()
        val user = AppUser(userId, "user", "user@example.com", "password")
        `when`(appUserRepository.findById(userId)).thenReturn(Optional.of(user))
        // Call the getUserById method
        val result = appUserService.getUserById(userId)
        // Verify the result
        Assertions.assertTrue(result.isPresent)
        Assertions.assertEquals(user, result.get())
    }

}
