package com.bhaskar.pixelwalls.di

import com.bhaskar.pixelwalls.data.background.createBackgroundRemover
import com.bhaskar.pixelwalls.domain.service.background.BackgroundRemover
import com.bhaskar.pixelwalls.data.capture.PlatformImageCaptureService
import com.bhaskar.pixelwalls.data.file.PlatformFileService
import com.bhaskar.pixelwalls.data.modelStatus.PlatformModelStatusService
import com.bhaskar.pixelwalls.data.repository.AiRepositoryImpl
import com.bhaskar.pixelwalls.data.repository.CreationsRepositoryImpl
import com.bhaskar.pixelwalls.data.repository.EditorRepositoryImpl
import com.bhaskar.pixelwalls.data.save.PlatformImageSaveService
import com.bhaskar.pixelwalls.data.wallpaper.PlatformWallpaperSetter
import com.bhaskar.pixelwalls.domain.repository.AiRepository
import com.bhaskar.pixelwalls.domain.repository.CreationsRepository
import com.bhaskar.pixelwalls.domain.repository.EditorRepository
import com.bhaskar.pixelwalls.domain.service.FileService
import com.bhaskar.pixelwalls.domain.service.ModelStatusService
import com.bhaskar.pixelwalls.domain.service.WallpaperSetter
import com.bhaskar.pixelwalls.domain.service.ImageCaptureService
import com.bhaskar.pixelwalls.domain.service.ImageSaveService
import com.bhaskar.pixelwalls.presentation.ai.AiViewModel
import com.bhaskar.pixelwalls.presentation.creations.CreationsViewModel
import com.bhaskar.pixelwalls.presentation.editor.EditorScreenViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
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
    single<FileService> { get<PlatformFileService>() }
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("KTOR: $message")
                    }
                }
                level = LogLevel.ALL
            }
        }
    }

    single { EditorRepositoryImpl(
        backgroundRemover = get(),
        wallpaperSetter = get(),
        saveService = get(),
        modelStatusService = get()
    ) } bind EditorRepository::class

    single {
        CreationsRepositoryImpl(
            fileService = get(),
            wallpaperSetter = get(),
            saveService = get()
        )
    } bind CreationsRepository::class

    single<AiRepository> {
        AiRepositoryImpl(
            httpClient = get()
        )
    } bind AiRepository::class

    viewModel {
        EditorScreenViewModel(
            repository = get()
        )
    }

    viewModel {
        CreationsViewModel(
            repository = get()
        )
    }

    viewModel {
        AiViewModel(
            aiRepository = get(),
            editorRepository = get()
        )
    }
}