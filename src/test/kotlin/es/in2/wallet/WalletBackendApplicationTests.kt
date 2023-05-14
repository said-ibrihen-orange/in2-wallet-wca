package es.in2.wallet

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.web.client.RestTemplate

@SpringBootTest(
    classes = [WalletBackendApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.datasource.url=jdbc:h2:mem:testdb"]
)
class WalletBackendApplicationTests(@Autowired val client: RestTemplate){
    @Test
    fun testTestEndpoint() {
        val response = client.getForEntity("/api/test", String::class.java)
        assert(response.statusCode == org.springframework.http.HttpStatus.OK)

    }

}
