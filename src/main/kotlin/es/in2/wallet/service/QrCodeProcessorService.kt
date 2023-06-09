package es.in2.wallet.service

import java.util.*

fun interface QrCodeProcessorService {
    fun processQrContent(qrContent: String): Any
}


