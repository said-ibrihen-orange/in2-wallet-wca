package es.in2.dome.verifier.gateway.security

import es.in2.dome.issuer.services.CustomKeyService
import es.in2.dome.verifier.gateway.ALL
import es.in2.dome.verifier.gateway.security.filters.JWTAuthenticationFilter
import es.in2.dome.verifier.gateway.security.filters.JWTAuthorizationFilter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
class WebSecurityConfig(
    private val authConfiguration: AuthenticationConfiguration,
    private val customKeyService: CustomKeyService
) {

    private val log: Logger = LogManager.getLogger(WebSecurityConfig::class.java)

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
                authorize("/security/test/customers", hasAuthority("CustomerCredential"))
                authorize("/security/test/providers", hasAuthority("ProviderCredential"))
            }
            addFilterAt<JWTAuthenticationFilter>(
                JWTAuthenticationFilter(authenticationManager(), customKeyService)
            )
            addFilterAt<JWTAuthorizationFilter>(
                JWTAuthorizationFilter(authenticationManager(), customKeyService)
            )
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
        }
        return http.build()
    }

    @Order(2)
    @Bean
    fun relyingPartyApiFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeRequests {

                // PORTAL component
                // generates a login_request
                authorize(HttpMethod.GET, "/portal/login/vc", permitAll)
                // return request_status for a specific state value
                authorize(HttpMethod.GET, "/portal/login/vc/status", permitAll)
                // no debería ser un endpoint porqué es fácilmente crackeable.
                // permit to post a notification that updates the state value
                authorize(HttpMethod.POST, "/portal/login/notifications", permitAll)

                // OIDC4VP component
                // generates the request_token
                authorize(HttpMethod.GET, "/relying-party/authentication-requests", permitAll)
                authorize(HttpMethod.POST, "/relying-party/siop-sessions", permitAll)

                // ISSUER
                // generates a customer VC to access to the Portal
                authorize(HttpMethod.POST, "/issuer/onboarding", permitAll)
            }
        }
        return http.build()
    }

    @Order(3)
    @Bean
    fun walletApiFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeRequests {
                // verifies the request_token
                authorize(HttpMethod.POST, "/wallet/siop/request-token", permitAll)
                // generates the VP with the VCs within
                authorize(HttpMethod.POST, "/wallet/siop/vp", permitAll)
            }
        }
        return http.build()
    }

    @Order(4)
    @Bean
    fun issuerApiFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeRequests {
                // ISSUER
                // generates a customer VC to access to the Portal
                authorize(HttpMethod.POST, "/issuer/onboarding", permitAll)
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
            "https://domeportaldev.in2.es",
            "https://domewalletdev.in2.es",
            "http://10.15.0.178:4200",
            "http://localhost:4200"
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