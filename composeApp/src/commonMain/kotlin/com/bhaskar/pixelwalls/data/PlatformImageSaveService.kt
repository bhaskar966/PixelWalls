package com.bhaskar.pixelwalls.data

import com.bhaskar.pixelwalls.domain.capture.ImageFormat
import com.bhaskar.pixelwalls.domain.capture.ImageSaveService

expect class PlatformImageSaveService: ImageSaveService {
    override suspend fun saveToGallery(
        fileName: String,
        imageBytes: ByteArray,
        format: ImageFormat
    ): Result<String>

    override suspend fun saveToCache(
        fileName: String,
        imageBytes: ByteArray
    ): Result<String>

    override suspend fun shareImage(
        fileName: String,
        imageBytes: ByteArray
    ): Result<Unit>

    override val isShareSupported: Boolean
}