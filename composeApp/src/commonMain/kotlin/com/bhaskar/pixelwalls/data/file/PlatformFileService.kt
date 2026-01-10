package com.bhaskar.pixelwalls.data.file

import com.bhaskar.pixelwalls.domain.service.FileService

expect class PlatformFileService: FileService {
    override suspend fun getSavedWallpaperPaths(): Result<List<String>>
    override suspend fun deleteFile(path: String): Result<Unit>
}