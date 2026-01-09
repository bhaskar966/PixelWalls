package com.bhaskar.pixelwalls.domain

import com.sun.jna.Native
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File


private interface User32Library : StdCallLibrary {
    fun SystemParametersInfoW(
        uiAction: Int,
        uiParam: Int,
        pvParam: String?,
        fWinIni: Int
    ): Boolean

    companion object {
        val INSTANCE: User32Library by lazy {
            Native.load("user32", User32Library::class.java, W32APIOptions.DEFAULT_OPTIONS)
        }

        const val SPI_SETDESKWALLPAPER = 0x0014
        const val SPIF_UPDATEINIFILE = 0x01
        const val SPIF_SENDCHANGE = 0x02
    }
}

actual class PlatformWallpaperSetter : WallpaperSetter {

    actual override val canApplyWallpaperInDifferentScreens: Boolean = false

    actual override suspend fun setWallpaper(
        imageBytes: ByteArray,
        target: WallpaperTarget
    ): WallpaperSetResult = withContext(Dispatchers.IO) {

        val tempFile = File(
            System.getProperty("java.io.tmpdir"),
            "pixelwalls_${System.currentTimeMillis()}.png"
        )
        tempFile.writeBytes(imageBytes)

        when (getOperatingSystem()) {
            OS.Windows -> setWindowsWallpaper(tempFile)
            OS.MacOS -> setMacOSWallpaper(tempFile)
            OS.Linux -> setLinuxWallpaper(tempFile)
            OS.Unknown -> WallpaperSetResult.Error("Unsupported OS")
        }
    }

    actual override fun canSetWallpaperDirectly(): Boolean {
        return when (getOperatingSystem()) {
            OS.Windows, OS.MacOS -> true
            OS.Linux -> true
            OS.Unknown -> false
        }
    }

    actual override suspend fun openWallpaperPicker(imageBytes: ByteArray): WallpaperSetResult {
        val picturesDir = File(System.getProperty("user.home"), "Pictures/PixelWalls")
        picturesDir.mkdirs()

        val file = File(picturesDir, "wallpaper_${System.currentTimeMillis()}.png")
        file.writeBytes(imageBytes)

        return try {
            Desktop.getDesktop().open(picturesDir)
            WallpaperSetResult.UserActionRequired(
                "Image saved to: ${file.absolutePath}\n\n" +
                        "Right-click the image and select 'Set as Desktop Background'"
            )
        } catch (e: Exception) {
            WallpaperSetResult.Error("Failed to open folder: ${e.message}")
        }
    }

    private fun setWindowsWallpaper(imageFile: File): WallpaperSetResult {
        return try {
            val success = User32Library.INSTANCE.SystemParametersInfoW(
                User32Library.SPI_SETDESKWALLPAPER,
                0,
                imageFile.absolutePath,
                User32Library.SPIF_UPDATEINIFILE or User32Library.SPIF_SENDCHANGE
            )

            if (success) {
                WallpaperSetResult.Success
            } else {
                val errorCode = Native.getLastError()
                WallpaperSetResult.Error("Windows API failed (error code: $errorCode)")
            }

        } catch (e: UnsatisfiedLinkError) {
            WallpaperSetResult.Error("JNA library not available: ${e.message}")
        } catch (e: Exception) {
            WallpaperSetResult.Error("Failed to set wallpaper: ${e.message}")
        }
    }

    private fun setMacOSWallpaper(imageFile: File): WallpaperSetResult {
        return try {
            // Use AppleScript
            val script = """
                tell application "Finder"
                    set desktop picture to POSIX file "${imageFile.absolutePath}"
                end tell
            """.trimIndent()

            val process = ProcessBuilder("osascript", "-e", script)
                .redirectErrorStream(true)
                .start()

            val exitCode = process.waitFor()

            if (exitCode == 0) WallpaperSetResult.Success
            else WallpaperSetResult.Error("AppleScript failed with code $exitCode")

        } catch (e: Exception) {
            WallpaperSetResult.Error("Failed: ${e.message}")
        }
    }

    private fun setLinuxWallpaper(imageFile: File): WallpaperSetResult {
        // Try common desktop environments
        val commands = listOf(
            // GNOME 3+
            arrayOf("gsettings", "set", "org.gnome.desktop.background", "picture-uri", "file://${imageFile.absolutePath}"),
            // KDE Plasma
            arrayOf("qdbus", "org.kde.plasmashell", "/PlasmaShell", "org.kde.PlasmaShell.evaluateScript",
                "var allDesktops = desktops();for (i=0;i<allDesktops.length;i++) {d = allDesktops[i];d.wallpaperPlugin = 'org.kde.image';d.currentConfigGroup = Array('Wallpaper', 'org.kde.image', 'General');d.writeConfig('Image', 'file://${imageFile.absolutePath}')}"),
            // XFCE
            arrayOf("xfconf-query", "-c", "xfce4-desktop", "-p", "/backdrop/screen0/monitor0/workspace0/last-image", "-s", imageFile.absolutePath)
        )

        for (command in commands) {
            try {
                val process = ProcessBuilder(*command)
                    .redirectErrorStream(true)
                    .start()

                if (process.waitFor() == 0) {
                    return WallpaperSetResult.Success
                }
            } catch (e: Exception) {
                // Try next command
                continue
            }
        }

        return WallpaperSetResult.Error("No supported desktop environment detected")
    }

    private fun getOperatingSystem(): OS {
        val osName = System.getProperty("os.name").lowercase()
        return when {
            osName.contains("win") -> OS.Windows
            osName.contains("mac") -> OS.MacOS
            osName.contains("nix") || osName.contains("nux") -> OS.Linux
            else -> OS.Unknown
        }
    }

    private enum class OS {
        Windows, MacOS, Linux, Unknown
    }
}