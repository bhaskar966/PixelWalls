package com.bhaskar.pixelwalls.utils.navigationComps

import kotlinx.serialization.Serializable

sealed class SurfaceDestinationRoutes {

    @Serializable
    data object EditorScreen: SurfaceDestinationRoutes()

    @Serializable
    data object AIScreen: SurfaceDestinationRoutes()

    @Serializable
    data object CreationsScreen: SurfaceDestinationRoutes()

}