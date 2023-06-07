package es.in2.wallet.service

import java.util.*

fun interface QrCodeProcessorService {
    fun processQrContent(userUUID: UUID, qrContent: String): Any
}


