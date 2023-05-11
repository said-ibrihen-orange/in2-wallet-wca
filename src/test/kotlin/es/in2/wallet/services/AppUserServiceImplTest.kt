package es.in2.wallet.services

import es.in2.wallet.entities.AppUser
import es.in2.wallet.repositories.AppUserRepository
import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.stereotype.Service
import io.mockk.mockk
import io.mockk.verify

@Service
class AppUserServiceImplTest(){
    private val appUserRepository: AppUserRepository = mockk()
    private val appUserService = AppUserServiceImpl(appUserRepository)

    private var appUser: AppUser? = null

    @BeforeEach
    fun setup() {
        //employeeRepository = Mockito.mock(EmployeeRepository.class);
        //employeeService = new EmployeeServiceImpl(employeeRepository);
        appUser = AppUser("testUser")

    }



//
//    // JUnit test for saveEmployee method
//    @DisplayName("JUnit test for save AppUser method")
//    @Test
//    fun registerUser() {
//        //Mockito.when(appUserRepository.save(appUser)).thenReturn(appUser)
//        //assertEquals(appUser, appUserService.registerUser("testUser"))
//        appUserService?.registerUser("testUser")
//
//        appUserService?.getUsers()
//        assertEquals(appUser, appUserService?.getUsers()?.get(0)  )
//    }
    @Test
    fun testAdd() {
        assertEquals(42, Integer.sum(19, 23))
    }

    @Test
    fun testDivide() {
        assertThrows(ArithmeticException::class.java) { Integer.divideUnsigned(42, 0) }
    }
}