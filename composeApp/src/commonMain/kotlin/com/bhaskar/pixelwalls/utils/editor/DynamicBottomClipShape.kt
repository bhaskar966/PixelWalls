package com.bhaskar.pixelwalls.utils.editor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class DynamicBottomClipShape(
    private val clipPercentage: Float,
    private val shapeCenter: Offset,
    private val shapeRadius: Float
): Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {

        val path = Path().apply {
            addRect(Rect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height
            ))

            val clipHeight = shapeRadius * 2f * clipPercentage
            val clipTop = shapeCenter.y + shapeRadius - clipHeight

            val clipRect = Rect(
                left = shapeCenter.x - shapeRadius,
                top = clipTop,
                right = shapeCenter.x + shapeRadius,
                bottom = shapeCenter.y + shapeRadius + (shapeRadius * 0.2f)
            )

            val clipPath = Path().apply {
                addOval(clipRect)
            }

            op(this, clipPath, PathOperation.Difference)
        }
        return Outline.Generic(path)

    }
}
class BottomCircularClipShape(
    private val clipPercentage: Float,
    private val shapeCenter: Offset,
    private val shapeRadius: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            // Start with the full image
            addRect(Rect(0f, 0f, size.width, size.height))

            // Calculate the circular region to subtract from bottom
            // This creates the "hidden in the portal" effect
            val clipTop = shapeCenter.y - shapeRadius * clipPercentage
            val clipBottom = shapeCenter.y + shapeRadius

            // Create circular cutout at the bottom
            val clipPath = Path().apply {
                addOval(
                    Rect(
                        left = shapeCenter.x - shapeRadius,
                        top = clipTop,
                        right = shapeCenter.x + shapeRadius,
                        bottom = clipBottom
                    )
                )
            }

            // Subtract the circular region
            op(this, clipPath, PathOperation.Difference)
        }

        return Outline.Generic(path)
    }
}


/**
 * Clips the image to show only the top portion of a circle
 * Used to create the "emerging from portal" effect
 */
class CircleTopClipShape(
    private val center: Offset,
    private val radius: Float,
    private val visibleHeightPercent: Float // 0.7 = show top 70% of circle
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            // Create a circle
            val circleRect = Rect(
                left = center.x - radius,
                top = center.y - radius,
                right = center.x + radius,
                bottom = center.y + radius
            )

            addOval(circleRect)

            // Now cut the bottom portion
            // If visibleHeightPercent = 0.7, we want to hide the bottom 30%
            val circleHeight = radius * 2f
            val visibleHeight = circleHeight * visibleHeightPercent
            val cutLineY = center.y - radius + visibleHeight

            // Create rectangle that covers the bottom part to cut
            val cutRect = Path().apply {
                addRect(
                    Rect(
                        left = 0f,
                        top = cutLineY,
                        right = size.width,
                        bottom = size.height
                    )
                )
            }

            // Subtract the bottom rectangle from the circle
            op(this, cutRect, PathOperation.Difference)
        }

        return Outline.Generic(path)
    }
}
