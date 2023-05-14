package es.in2.wallet.services

import es.in2.wallet.entities.AppUser
import es.in2.wallet.repositories.AppUserRepository
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito
import java.util.*

class AppUserServiceImplTest {
    private var mockRepository = Mockito.mock(AppUserRepository::class.java)
    private var appUserService = AppUserServiceImpl(mockRepository)
    @BeforeEach
    fun setUp() {
        mockRepository = Mockito.mock(AppUserRepository::class.java)
        appUserService = AppUserServiceImpl(mockRepository)
    }

    @Test
    fun getUserByUsername() {
        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()
        val uuid3 = UUID.randomUUID()
        val allAppUsers = listOf(
            AppUser(uuid1,"user1"),
            AppUser(uuid2,"user2"),
            AppUser(uuid3,"user3")
        )
        Mockito.`when`(mockRepository.findAll()).thenReturn(allAppUsers)
        val expected = AppUser(uuid2,"user2")
        val actual = appUserService.getUserByUsername("user2")
        assertEquals(expected, actual)

    }

    @Test
    fun getUsers() {
        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()
        val uuid3 = UUID.randomUUID()
        val allAppUsers = listOf(
            AppUser(uuid1,"user1"),
            AppUser(uuid2,"user2"),
            AppUser(uuid3,"user3")
        )
        Mockito.`when`(mockRepository.findAll()).thenReturn(allAppUsers)
        val expected = listOf(
            AppUser(uuid1,"user1"),
            AppUser(uuid2,"user2"),
            AppUser(uuid3,"user3")
        )
        val actual = appUserService.getUsers()
        assertEquals(expected, actual, "The lists of users are not equal")
    }

    @Test
    fun registerUser() {
        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()
        val uuid3 = UUID.randomUUID()
        val uuidRegister = UUID.randomUUID()
        val allAppUsers = listOf(
            AppUser(uuid1,"user1"),
            AppUser(uuid2,"user2"),
            AppUser(uuid3,"user3")
        )
        Mockito.`when`(mockRepository.findAll()).thenReturn(allAppUsers)
        Mockito.`when`(mockRepository.findByUsername("user4")).thenReturn(null)
        Mockito.`when`(mockRepository.save(AppUser(uuidRegister,"user4"))).thenReturn(AppUser(uuidRegister,"user4"))
        val actual = appUserService.registerUser("user4")
        // Can´t check the uuid because it is generated randomly -> assert it is not null
        assertNotNull( actual, "The uuid is null")

    }

    @Test
    fun registerUserAlreadyExists() {
        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()
        val uuid3 = UUID.randomUUID()
        val allAppUsers = listOf(
            AppUser(uuid1,"user1"),
            AppUser(uuid2,"user2"),
            AppUser(uuid3,"user3")
        )
        Mockito.`when`(mockRepository.findAll()).thenReturn(allAppUsers)
        Mockito.`when`(mockRepository.findByUsername("user3")).thenReturn(AppUser(uuid3,"user3"))
        assertThrows(Exception::class.java) {
            appUserService.registerUser("user3")
        }
    }
}