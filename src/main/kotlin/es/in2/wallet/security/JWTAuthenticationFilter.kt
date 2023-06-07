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
import es.in2.wallet.model.dto.BasicAuthCredentialsDTO
import es.in2.wallet.util.SIOP_AUDIENCE
import es.in2.wallet.waltid.CustomKeyService
import es.in2.wallet.waltid.impl.CustomDidServiceImpl
import es.in2.wallet.waltid.impl.CustomKeyServiceImpl
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

@Slf4j
class JWTAuthenticationFilter(
    private val authenticationManager: AuthenticationManager,
    private val customKeyService: CustomKeyService
) : UsernamePasswordAuthenticationFilter() {

    private val log: Logger = LoggerFactory.getLogger(JWTAuthenticationFilter::class.java)

    //generate did:key for the Wallet Organization
    private val walletIssuerDID = CustomDidServiceImpl(CustomKeyServiceImpl()).generateDidKey()

    @Throws(AuthenticationException::class)
    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        try {
            val credentials = ObjectMapper().readValue(request.inputStream, BasicAuthCredentialsDTO::class.java)
            return authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(credentials.username, credentials.password, ArrayList())
            )
        } catch (e: IOException) {
            log.error(e.message)
            throw AuthenticationServiceException(e.message)
        }
    }

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
            // create the access_token
            val accessToken = createAccessToken()
            // add access_token to the response HttpServletResponse
            response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
        } catch (e: Exception) {
            log.error(e.message)
        }
    }

    private fun createAccessToken(): String {
        // get ECKey
        val ecJWK: ECKey = customKeyService.getECKeyFromKid(walletIssuerDID)
        // build Signer of the JWS
        val signer: JWSSigner = ECDSASigner(ecJWK)
        // create the JWT Header
        val jwsHeader = JWSHeader.Builder(JWSAlgorithm.ES256)
            .keyID(walletIssuerDID)
            .type(JOSEObjectType.JWT)
            .build()
        // create the JWT Payload
        val instant = Instant.now()
        val claimsSet: JWTClaimsSet = JWTClaimsSet.Builder()
            .issuer(walletIssuerDID)
            .subject(walletIssuerDID)
            .audience(SIOP_AUDIENCE)
            .issueTime(Date.from(instant))
            .expirationTime(Date.from(instant.plusSeconds(6000)))
            .claim("", "")
            .build()
        // build JWT
        val signedJWT = SignedJWT(jwsHeader, claimsSet)
        // execute signature
        signedJWT.sign(signer)
        return signedJWT.serialize()
    }

}