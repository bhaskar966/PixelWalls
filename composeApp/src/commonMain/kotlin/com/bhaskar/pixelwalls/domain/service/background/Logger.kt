package com.bhaskar.pixelwalls.domain.service.background

/**
 * Simple internal logger for debugging
 */
internal object Logger {

    private const val TAG = "BackgroundRemover"
    var isDebugEnabled = false

    fun debug(message: String) {
        if (isDebugEnabled) {
            println("[$TAG] DEBUG: $message")
        }
    }

    fun info(message: String) {
        println("[$TAG] INFO: $message")
    }

    fun error(message: String, throwable: Throwable? = null) {
        println("[$TAG] ERROR: $message")
        throwable?.printStackTrace()
    }
}