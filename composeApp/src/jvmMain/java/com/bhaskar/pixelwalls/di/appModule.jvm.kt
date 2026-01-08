package com.bhaskar.pixelwalls.di

import com.bhaskar.pixelwalls.data.PlatformImageSaveService
import com.bhaskar.pixelwalls.domain.capture.ImageSaveService
import com.bhaskar.pixelwalls.utils.cache.ImageCache
import com.bhaskar.pixelwalls.utils.cache.PlatformImageCache
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module {
    factoryOf(::PlatformImageCache) bind ImageCache::class
    single<ImageSaveService> { PlatformImageSaveService() }
}