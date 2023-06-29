package es.in2.wallet.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import es.in2.wallet.configuration.WalletDidKeyGenerator
import es.in2.wallet.exception.AccessTokenCreationException
import es.in2.wallet.service.AppUserService
import es.in2.wallet.util.BEARER_PREFIX
import es.in2.wallet.util.SIOP_AUDIENCE
import es.in2.wallet.util.USER_ROLE
import es.in2.wallet.waltid.CustomKeyService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.io.IOException
import java.time.Instant
import java.util.*

class JWTAuthenticationFilter(
    private val authenticationManager: AuthenticationManager,
    walletDidKeyGenerator: WalletDidKeyGenerator,
    private val customKeyService: CustomKeyService,
    private val appUserService: AppUserService,
) : UsernamePasswordAuthenticationFilter() {

    private val log: Logger = LoggerFactory.getLogger(JWTAuthenticationFilter::class.java)
    private val walletDID = walletDidKeyGenerator.getDidKey()

    @Throws(AuthenticationException::class)
    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        try {
            val credentials = ObjectMapper().readTree(request.inputStream)
            val username = credentials["username"]
            val password = credentials["password"]
            log.info("Attempting authentication for username: {}", username.asText())
            return authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(username.asText(), password.asText(), ArrayList())
            )
        } catch (e: IOException) {
            log.error("Authentication failed. Error: {}", e.message)
            throw AuthenticationServiceException(e.message)
        }
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    override fun successfulAuthentication(
        request: HttpServletRequest, response: HttpServletResponse,
        filterChain: FilterChain, authentication: Authentication
    ) {
        try {
            SecurityContextHolder.getContext().authentication = authentication
            val authClaims: MutableList<String> = mutableListOf()
            authentication.authorities?.let { element ->
                element.forEach { authClaims.add(it.toString()) }
            }
            val accessToken = createAccessToken(authentication)
            response.addHeader(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            log.info("Authentication successful for username: {}", authentication.name)
        } catch (e: Exception) {
            log.error("Authentication success handling failed. Error: {}", e.message)
        }
    }

    private fun createAccessToken(authentication: Authentication): String {
        try {
            // Get ECKey
            val ecJWK: ECKey = customKeyService.getECKeyFromKid(walletDID)
            log.debug("Retrieved ECKey for walletDID: {}", walletDID)

            // Building the Signer of the JWT
            val signer: JWSSigner = ECDSASigner(ecJWK)
            log.debug("Built JWSSigner with ECKey")

            // Create the JWT Header
            val jwsHeader = JWSHeader.Builder(JWSAlgorithm.ES256)
                .keyID(walletDID)
                .type(JOSEObjectType.JWT)
                .build()
            log.debug("Created JWT Header")

            // Create the JWT Payload
            val userData = appUserService.getUserByUsername(authentication.name).get()
            log.debug("Retrieved user data for username: {}", authentication.name)

            val instant = Instant.now()
            val claimsSet: JWTClaimsSet = JWTClaimsSet.Builder()
                .issuer(walletDID)
                .subject(userData.id.toString())
                .audience(SIOP_AUDIENCE)
                .issueTime(Date.from(instant))
                .expirationTime(Date.from(instant.plusSeconds(6000)))
                .claim("username", userData.username)
                .claim("email", userData.email)
                .claim("roles", listOf(USER_ROLE))
                .build()
            log.debug("Created JWT ClaimsSet")

            // build JWT
            val signedJWT = SignedJWT(jwsHeader, claimsSet)
            log.debug("Signed JWT")

            // execute signature
            signedJWT.sign(signer)
            return signedJWT.serialize()
        } catch (e: Exception) {
            log.error("Failed to create token. Error: {}", e.message)
            throw AccessTokenCreationException("Failed to create access_token: ${e.message}")
        }
    }

}