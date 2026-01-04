package com.bhaskar.pixelwalls

import android.app.Application
import com.bhaskar.pixelwalls.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class PixelWallsApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@PixelWallsApplication)
        }

    }

}