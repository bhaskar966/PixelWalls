package com.bhaskar.pixelwalls.data.file

import com.bhaskar.pixelwalls.domain.service.FileService
import com.bhaskar.pixelwalls.utils.PixelWallsPaths
import com.bhaskar.pixelwalls.utils.getPublicPicturesDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class PlatformFileService : FileService {

    actual override suspend fun getSavedWallpaperPaths(): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val folder = File(getPublicPicturesDir(), PixelWallsPaths.FOLDER_NAME)

                if (!folder.exists()) return@withContext Result.success(emptyList())

                val files = folder.listFiles { file ->
                    val ext = file.extension.lowercase()
                    file.isFile && (ext == "png" || ext == "jpg" || ext == "jpeg")
                }?.map {
                    val path = it.absolutePath.replace("\\", "/")
                    if (path.startsWith("/")) "file://$path" else "file:///$path"
                }?.reversed() ?: emptyList()

                Result.success(files)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    actual override suspend fun deleteFile(path: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val file = File(path.removePrefix("file://").removePrefix("file:/"))
            if(file.exists() && file.delete()) Result.success(Unit)
            else Result.failure(Exception("File not found"))
        }
    }
}