package es.in2.wallet.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jwt.SignedJWT
import es.in2.wallet.configuration.WalletDidKeyGenerator
import es.in2.wallet.exception.AccessTokenException
import es.in2.wallet.util.BEARER_PREFIX
import es.in2.wallet.util.USER_ROLE
import es.in2.wallet.waltid.CustomKeyService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import java.io.IOException

class JWTAuthorizationFilter(
    authenticationManager: AuthenticationManager,
    walletDidKeyGenerator: WalletDidKeyGenerator,
    private val customKeyService: CustomKeyService,
) : BasicAuthenticationFilter(authenticationManager) {

    private val log: Logger = LogManager.getLogger(JWTAuthorizationFilter::class.java)

    private val walletDID = walletDidKeyGenerator.getDidKey()

    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        // get access_token from header
        val accessToken = request.getHeader(HttpHeaders.AUTHORIZATION)
        // verify if access_token starts with bearer prefix and is not null
        if (accessToken == null || !accessToken.startsWith(BEARER_PREFIX)) {
            log.info("No JWT token found in request headers")
            chain.doFilter(request, response)
            return
        }
        // parse access_token to SignedJWT
        val signedJwtAccessToken = SignedJWT.parse(accessToken.replace(BEARER_PREFIX, ""))
        // Verify access_token signature
        accessTokenVerification(signedJwtAccessToken)
        // get JWT Claims Set
        val jwtClaimsSet = ObjectMapper().readTree(signedJwtAccessToken.jwtClaimsSet.toString())
        // build user
        val username = jwtClaimsSet["username"].asText()
        val authorities = ArrayList<GrantedAuthority>()
        authorities.add(SimpleGrantedAuthority(USER_ROLE))
        val principalDetails = User(username, "", authorities)
        val authentication = UsernamePasswordAuthenticationToken(principalDetails, null, authorities)
        SecurityContextHolder.getContext().authentication = authentication

        chain.doFilter(request, response)
    }

    private fun accessTokenVerification(signedJwtAccessToken: SignedJWT): Boolean {
        val ecKey: ECKey = customKeyService.getECKeyFromKid(walletDID)
        val ecPublicJWK: ECKey = ecKey.toPublicJWK()
        val verifier: JWSVerifier = ECDSAVerifier(ecPublicJWK)
        return if (signedJwtAccessToken.verify(verifier)) {
            true
        } else {
            throw AccessTokenException("The 'access_token' is not valid")
        }
    }

}
