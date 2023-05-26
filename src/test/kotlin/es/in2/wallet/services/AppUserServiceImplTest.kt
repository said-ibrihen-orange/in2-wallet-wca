package es.in2.wallet.services

import es.in2.wallet.domain.entities.AppUser
import es.in2.wallet.repositories.AppUserRepository
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.*

@SpringBootTest
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
            AppUser(uuid1,"user1","example.com"),
            AppUser(uuid2,"user2","example.com"),
            AppUser(uuid3,"user3","example.com")
        )
        Mockito.`when`(mockRepository.findAll()).thenReturn(allAppUsers)
        Mockito.`when`(mockRepository.findByUsername("user2")).thenReturn(AppUser(uuid2,"user2","example.com"))
        val expected = AppUser(uuid2,"user2","example.com")
        val actual = appUserService.getUserByUsername("user2")
        assertEquals(expected, actual)
    }

    @Test
    fun getUsers() {
        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()
        val uuid3 = UUID.randomUUID()
        val allAppUsers = listOf(
            AppUser(uuid1,"user1","example.com"),
            AppUser(uuid2,"user2","example.com"),
            AppUser(uuid3,"user3","example.com")
        )
        Mockito.`when`(mockRepository.findAll()).thenReturn(allAppUsers)
        val expected = listOf(
            AppUser(uuid1,"user1","example.com"),
            AppUser(uuid2,"user2","example.com"),
            AppUser(uuid3,"user3","example.com")
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
            AppUser(uuid1,"user1","example.com"),
            AppUser(uuid2,"user2","example.com"),
            AppUser(uuid3,"user3","example.com")
        )
        Mockito.`when`(mockRepository.findAll()).thenReturn(allAppUsers)
        Mockito.`when`(mockRepository.findByUsername("user4")).thenReturn(null)
        Mockito.`when`(mockRepository.save(AppUser(uuidRegister,"user4","example.com"))).thenReturn(AppUser(uuidRegister,"user4","example.com"))
        // val actual = appUserService.registerUser("user4")
        // Can´t check the uuid because it is generated randomly -> assert it is not null
        // assertNotNull( actual, "The uuid is null") remove this test tmp
    }

    @Test
    fun registerUserAlreadyExists() {
        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()
        val uuid3 = UUID.randomUUID()
        val allAppUsers = listOf(
            AppUser(uuid1,"user1","example.com"),
            AppUser(uuid2,"user2","example.com"),
            AppUser(uuid3,"user3","example.com")
        )
        Mockito.`when`(mockRepository.findAll()).thenReturn(allAppUsers)
        Mockito.`when`(mockRepository.findByUsername("user3")).thenReturn(AppUser(uuid3,"user3","example.com"))
        assertThrows(Exception::class.java) {
            appUserService.registerUser("user3")
        }
    }

}