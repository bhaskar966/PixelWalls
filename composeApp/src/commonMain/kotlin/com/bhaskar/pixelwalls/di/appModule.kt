package com.bhaskar.pixelwalls.di

import com.bhaskar.pixelwalls.data.background.createBackgroundRemover
import com.bhaskar.pixelwalls.domain.service.background.BackgroundRemover
import com.bhaskar.pixelwalls.data.capture.PlatformImageCaptureService
import com.bhaskar.pixelwalls.data.modelStatus.PlatformModelStatusService
import com.bhaskar.pixelwalls.data.repository.EditorRepositoryImpl
import com.bhaskar.pixelwalls.data.save.PlatformImageSaveService
import com.bhaskar.pixelwalls.data.wallpaper.PlatformWallpaperSetter
import com.bhaskar.pixelwalls.domain.repository.EditorRepository
import com.bhaskar.pixelwalls.domain.service.ModelStatusService
import com.bhaskar.pixelwalls.domain.service.WallpaperSetter
import com.bhaskar.pixelwalls.domain.service.ImageCaptureService
import com.bhaskar.pixelwalls.domain.service.ImageSaveService
import com.bhaskar.pixelwalls.presentation.editor.EditorScreenViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module


expect val platformModule: Module

val appModule = module {
    includes(platformModule)
    single<BackgroundRemover> { createBackgroundRemover() }
    single<ImageCaptureService> { PlatformImageCaptureService() }
    single<WallpaperSetter> { get<PlatformWallpaperSetter>() }
    single<ModelStatusService> { get<PlatformModelStatusService>() }
    single<ImageSaveService> { get<PlatformImageSaveService>() }

    single { EditorRepositoryImpl(
        backgroundRemover = get(),
        wallpaperSetter = get(),
        saveService = get(),
        modelStatusService = get()
    ) } bind EditorRepository::class

    viewModel {
        EditorScreenViewModel(
            repository = get()
        )
    }
}