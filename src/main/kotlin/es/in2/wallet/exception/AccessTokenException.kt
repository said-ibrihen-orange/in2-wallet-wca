package es.in2.wallet.exception

import org.springframework.security.core.AuthenticationException

class AccessTokenException(override var message: String) : AuthenticationException(message)