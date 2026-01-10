package com.bhaskar.pixelwalls.presentation.creations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhaskar.pixelwalls.domain.repository.CreationsRepository
import com.bhaskar.pixelwalls.utils.PermissionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreationsViewModel(
    private val repository: CreationsRepository
): ViewModel() {

    private val _creationsUiState = MutableStateFlow(CreationsUiState())
    val creationsUiState = _creationsUiState.asStateFlow()

    private var permissionHandler: PermissionHandler? = null


    fun onEvent(event: CreationsUiEvents) {
        when(event) {
            is CreationsUiEvents.DeleteCreation -> deleteWallpaper(event.path)
            is CreationsUiEvents.LoadCreations -> loadWallpapers()
            is CreationsUiEvents.RefreshCreations -> {
                _creationsUiState.update {
                    it.copy(isRefreshing = true, error = null)
                }
                checkPermissionAndLoad()
            }
            is CreationsUiEvents.RequestPermission -> requestPermission()
            is CreationsUiEvents.DismissError -> {
                _creationsUiState.update {
                    it.copy(error = null)
                }
            }
        }
    }

    private fun loadWallpapers() {
        viewModelScope.launch {
            println("CreationsViewModel" + "Loading wallpapers...")

            _creationsUiState.update {
                it.copy(isLoading = true, error = null)
            }

            repository.getSavedWallpaperPaths()
                .onSuccess { paths ->
                    println("CreationsViewModel" + "Loaded ${paths.size} wallpapers")
                    _creationsUiState.update {
                        it.copy(
                            wallpapers = paths,
                            isLoading = false,
                            isRefreshing = false,
                            needsPermission = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    println("CreationsViewModel" + "Failed to load: ${error.message}" + error.toString())
                    _creationsUiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = error.message ?: "Failed to load wallpapers"
                        )
                    }
                }
        }
    }

    private fun deleteWallpaper(path: String) {
        viewModelScope.launch {
            val handler = permissionHandler
            if (handler == null) {
                _creationsUiState.update {
                    it.copy(error = "Permission handler not initialized")
                }
                return@launch
            }

            _creationsUiState.update {
                it.copy(error = null)
            }

            println("CreationsViewModel Requesting delete permission for: $path")

            val permissionGranted = handler.requestDeletePermission(listOf(path))

            if (!permissionGranted) {
                _creationsUiState.update {
                    it.copy(error = "Delete cancelled")
                }
                return@launch
            }

            repository.deleteWallpaper(path)
                .onSuccess {
                    _creationsUiState.update {
                        it.copy(error = null)
                    }
                    loadWallpapers()
                }
                .onFailure { error ->
                    _creationsUiState.update {
                        it.copy(error = "Failed to delete: ${error.message}")
                    }
                }
        }
    }

    private fun checkPermissionAndLoad() {
        viewModelScope.launch {
            val handler = permissionHandler ?: return@launch

            _creationsUiState.update {
                it.copy(isLoading = true, error = null, needsPermission = false)
            }

            val hasPermission = handler.checkStoragePermission()

            if (!hasPermission) {
                _creationsUiState.update {
                    it.copy(
                        isLoading = false,
                        needsPermission = true,
                        error = null
                    )
                }
                return@launch
            }

            loadWallpapers()
        }
    }

    private fun requestPermission() {
        viewModelScope.launch {
            val handler = permissionHandler ?: return@launch

            _creationsUiState.update {
                it.copy(isLoading = true, error = null)
            }

            val granted = handler.requestStoragePermission()

            if (granted) {
                _creationsUiState.update {
                    it.copy(needsPermission = false, error = null)
                }
                loadWallpapers()
            } else {
                _creationsUiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Permission denied. Please enable in Settings.",
                        needsPermission = true
                    )
                }
            }
        }
    }

    fun setPermissionHandler(handler: PermissionHandler) {
        permissionHandler = handler
        checkPermissionAndLoad()
    }


}