package com.bhaskar.pixelwalls.utils

import java.io.File

actual fun getPublicPicturesDir(): String {
    return System.getProperty("user.home") + File.separator + "Pictures"
}