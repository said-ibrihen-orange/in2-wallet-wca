package es.in2.wallet.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import java.io.IOException
import java.security.Key

class JWTAuthorizationFilter(
    authenticationManager: AuthenticationManager,
    private val customUserDetailsService: CustomUserDetailsService
) : BasicAuthenticationFilter(authenticationManager) {

    private val log: Logger = LogManager.getLogger(JWTAuthorizationFilter::class.java)

    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.info("No JWT token found in request headers")
            chain.doFilter(request, response)
            return
        }
        val secret = "dRgUkXp2s5v8x/A?D(G+KbPeShVmYq3t6w9z/B&E)H@McQfTjWnZr4u7x!A%D*G-"
        val key: Key? = Keys.hmacShaKeyFor(secret.toByteArray())
        val claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(authorizationHeader.replace("Bearer ", ""))
        val userDetail = customUserDetailsService.loadUserByUsername(claims.body.subject)
        if (userDetail.username != null) {
            log.info("Username: ${userDetail.username}")
            val principalDetails = User(userDetail.username, "", userDetail.authorities)
            val authentication = UsernamePasswordAuthenticationToken(
                principalDetails, null, userDetail.authorities
            )
            SecurityContextHolder.getContext().authentication = authentication
        }
        chain.doFilter(request, response)
    }

}