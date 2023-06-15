package es.in2.wallet.controller

import es.in2.wallet.exception.DidVerificationException
import es.in2.wallet.exception.InvalidTokenException
import es.in2.wallet.exception.JwtInvalidFormatException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class VerifiablePresentationExceptionHandler {

    private val log: Logger = LoggerFactory.getLogger(VerifiablePresentationExceptionHandler::class.java)

    @ExceptionHandler(DidVerificationException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleDidVerificationException(e: Exception): ResponseEntity<Unit> {
        log.error(e.message)
        return ResponseEntity(HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(JwtInvalidFormatException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleVerificationException(e: Exception): ResponseEntity<Unit> {
        log.error(e.message)
        return ResponseEntity(HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(InvalidTokenException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleRequestTokenVerificationException(e: Exception): ResponseEntity<Unit> {
        log.error(e.message)
        return ResponseEntity(HttpStatus.BAD_REQUEST)
    }

}