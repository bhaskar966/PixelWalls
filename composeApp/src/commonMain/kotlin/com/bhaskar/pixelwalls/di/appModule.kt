package com.bhaskar.pixelwalls.di

import com.bhaskar.pixelwalls.backgroundremoval.BackgroundRemover
import com.bhaskar.pixelwalls.backgroundremoval.createBackgroundRemover
import com.bhaskar.pixelwalls.presentation.editor.EditorScreenViewModel
import okio.FileSystem
import okio.SYSTEM
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


expect val platformModule: Module

val appModule = module {
    includes(platformModule)
    single<BackgroundRemover> { createBackgroundRemover() }
    single { FileSystem.SYSTEM }
    viewModel {
        EditorScreenViewModel(
            backgroundRemover = get(),
            fileSystem = get(),
            imageCache = get()
        )
    }
}