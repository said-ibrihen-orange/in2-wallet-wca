package es.in2.wallet.controllers

import es.in2.wallet.exceptions.DidVerificationException
import es.in2.wallet.exceptions.NoSuchQrContentException
import es.in2.wallet.exceptions.RequestTokenException
import es.in2.wallet.exceptions.VerificationException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class WalletGlobalControllerExceptionHandler {

    private val log: Logger = LogManager.getLogger(WalletGlobalControllerExceptionHandler::class.java)

    @ExceptionHandler(DidVerificationException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleDidVerificationException(e: Exception): ResponseEntity<Unit> {
        log.error(e.message)
        return ResponseEntity(HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(VerificationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleVerificationException(e: Exception): ResponseEntity<Unit> {
        log.error(e.message)
        return ResponseEntity(HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(RequestTokenException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleRequestTokenVerificationException(e: Exception): ResponseEntity<Unit> {
        log.error(e.message)
        return ResponseEntity(HttpStatus.BAD_REQUEST)
    }
    @ExceptionHandler(NoSuchQrContentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleNoSuchQrContentException(e: Exception): ResponseEntity<Unit> {
        log.error(e.message)
        return ResponseEntity(HttpStatus.BAD_REQUEST)
    }

}