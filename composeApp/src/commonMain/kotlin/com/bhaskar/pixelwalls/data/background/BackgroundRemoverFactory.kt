package com.bhaskar.pixelwalls.data.background

import com.bhaskar.pixelwalls.domain.service.background.BackgroundRemover

/**
 * Factory to create platform-specific BackgroundRemover instances.
 * This belongs in the data layer.
 */
expect fun createBackgroundRemover(): BackgroundRemover