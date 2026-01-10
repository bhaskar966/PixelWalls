package com.bhaskar.pixelwalls.data.file

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.bhaskar.pixelwalls.domain.service.FileService
import com.bhaskar.pixelwalls.utils.PixelWallsPaths
import com.bhaskar.pixelwalls.utils.getPublicPicturesDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import androidx.core.net.toUri

actual class PlatformFileService(
    private val context: Context
) : FileService {
    actual override suspend fun getSavedWallpaperPaths(): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val paths = mutableListOf<String>()

                // Query MediaStore
                val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.RELATIVE_PATH
                )

                val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
                } else {
                    null
                }

                val selectionArgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    arrayOf("%${PixelWallsPaths.FOLDER_NAME}%")
                } else {
                    null
                }

                val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

                context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )?.use { cursor ->

                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        paths.add(contentUri.toString())
                    }
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    val baseDir = getPublicPicturesDir()
                    val folder = File(baseDir, PixelWallsPaths.FOLDER_NAME)

                    if (folder.exists() && folder.isDirectory) {
                        folder.listFiles { file ->
                            val name = file.name.lowercase()
                            file.isFile && (name.endsWith(".png") ||
                                    name.endsWith(".jpg") ||
                                    name.endsWith(".jpeg"))
                        }?.forEach { file ->
                            val path = file.absolutePath
                            if (!paths.contains(path)) {
                                paths.add(path)
                            }
                        }
                    }
                }

                Result.success(paths)

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }


    actual override suspend fun deleteFile(path: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Result.success(Unit)
                } else if (path.startsWith("content://")) {
                    val uri = path.toUri()
                    val deleted = context.contentResolver.delete(uri, null, null)

                    if (deleted > 0) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception("Failed to delete from MediaStore"))
                    }
                } else {
                    val file = File(path)
                    if (file.exists() && file.delete()) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception("File not found"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
