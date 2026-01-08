package com.bhaskar.pixelwalls.domain.capture


interface ImageSaveService {

    suspend fun saveToGallery(
        fileName: String,
        imageBytes: ByteArray,
        format: ImageFormat = ImageFormat.PNG
    ): Result<String>

    suspend fun saveToCache(
        fileName: String,
        imageBytes: ByteArray,
    ): Result<String>

    suspend fun shareImage(
        fileName: String,
        imageBytes: ByteArray
    ): Result<Unit>
}

enum class ImageFormat(val extension: String, val mimeType: String) {
    PNG("png", "image/png"),
    JPEG("jpg", "image/jpeg"),
}