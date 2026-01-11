package com.bhaskar.pixelwalls.domain.repository

import com.bhaskar.pixelwalls.domain.model.AiAspectRatio

interface AiRepository {

    fun canUseAi(): Boolean
    suspend fun generateImage(prompt: String, aspectRatio: AiAspectRatio = AiAspectRatio.SQUARE): Result<ByteArray>
}