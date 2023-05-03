package es.in2.wallet.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

const val ALL = "*"

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
class WebSecurityConfig(
    private val authConfiguration: AuthenticationConfiguration
) {

    @Bean
    fun authenticationManager(): AuthenticationManager = authConfiguration.authenticationManager

    @Bean
    fun bCryptEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()

    @Order(1)
    @Bean
    fun apiFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf {
                disable()
            }
            cors {
                corsConfigurationSource()
            }
            httpBasic {
                disable()
            }
            authorizeRequests {
                authorize(HttpMethod.GET, "/api/**", permitAll)
            }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
        }
        return http.build()
    }

    @Bean
    fun loginFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeRequests {
                authorize(anyRequest, authenticated)
            }
            formLogin { }
        }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf(
            "https://issuerdev.in2.es",
            "https://portaldev.in2.es",
            "https://walletdev.in2.es",
            "https://issuerapidev.in2.es",
            "https://portalapidev.in2.es",
            "https://walletapidev.in2.es",
            "http://localhost:8080",
            "http://localhost:8081",
            "http://localhost:8082",
            "http://localhost:8083",
            "http://localhost:4200",
            "http://localhost:4201",
            "http://localhost:4202",
            "http://localhost:4203",
            "http://localhost:8100"
        )
        configuration.allowedMethods = listOf(
            HttpMethod.GET.name(),
            HttpMethod.HEAD.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.OPTIONS.name()
        )
        configuration.maxAge = 1800L
        configuration.allowedHeaders = listOf(ALL)
        configuration.exposedHeaders = listOf(ALL)
        configuration.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

}