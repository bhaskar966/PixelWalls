package com.bhaskar.pixelwalls.di

import com.bhaskar.pixelwalls.data.modelStatus.PlatformModelStatusService
import com.bhaskar.pixelwalls.data.save.PlatformImageSaveService
import com.bhaskar.pixelwalls.data.wallpaper.PlatformWallpaperSetter
import com.bhaskar.pixelwalls.domain.service.ImageSaveService
import com.bhaskar.pixelwalls.utils.cache.ImageCache
import com.bhaskar.pixelwalls.utils.cache.PlatformImageCache
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module {
    factoryOf(::PlatformImageCache) bind ImageCache::class
    single<ImageSaveService> { PlatformImageSaveService { androidContext() } }
    single { PlatformWallpaperSetter { androidContext() } }
    single { PlatformModelStatusService(get()) }

}