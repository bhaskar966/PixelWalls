package com.bhaskar.pixelwalls.backgroundremoval

/**
 * Base exception for background remover errors
 */
open class BackgroundRemoverException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

/**
 * Thrown when image decoding fails
 */
class ImageDecodingException(message: String = "Failed to decode image", cause: Throwable? = null) :
    BackgroundRemoverException(message, cause)

/**
 * Thrown when ONNX model inference fails
 */
class InferenceException(message: String = "Model inference failed", cause: Throwable? = null) :
    BackgroundRemoverException(message, cause)

/**
 * Thrown when BackgroundRemover is not properly initialized
 */
class NotInitializedException(
    message: String = "BackgroundRemover not initialized. Call initialize() first.",
    cause: Throwable? = null
) : BackgroundRemoverException(message, cause)

/**
 * Thrown when model file is not found
 */
class ModelNotFoundException(
    message: String = "ONNX model file not found",
    cause: Throwable? = null
) : BackgroundRemoverException(message, cause)