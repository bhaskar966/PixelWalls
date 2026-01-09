package com.bhaskar.pixelwalls.di

import com.bhaskar.pixelwalls.data.modelStatus.PlatformModelStatusService
import com.bhaskar.pixelwalls.data.save.PlatformImageSaveService
import com.bhaskar.pixelwalls.data.wallpaper.PlatformWallpaperSetter
import com.bhaskar.pixelwalls.domain.service.ImageSaveService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single<ImageSaveService> { PlatformImageSaveService { androidContext() } }
    single { PlatformWallpaperSetter { androidContext() } }
    single { PlatformModelStatusService(get()) }

}