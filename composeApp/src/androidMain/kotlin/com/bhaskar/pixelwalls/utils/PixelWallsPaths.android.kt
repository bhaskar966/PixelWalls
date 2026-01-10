package com.bhaskar.pixelwalls.utils

import android.os.Environment

actual fun getPublicPicturesDir(): String {
    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
}