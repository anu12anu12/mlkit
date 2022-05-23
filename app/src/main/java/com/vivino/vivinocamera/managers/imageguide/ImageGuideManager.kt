package com.vivino.vivinocamera.managers.imageguide

import android.content.Context
import android.graphics.Rect
import android.util.Log
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.objects.DetectedObject
import com.vivino.vivinocamera.interfaces.BestBottlePositionInterface
import com.vivino.vivinocamera.utils.Common
import kotlin.math.pow


class ImageGuideManager(
    private val results: List<DetectedObject>,
    val context: Context,
    private val previewView: PreviewView
) : BestBottlePositionInterface {
    val screenHeight = Common.dpFromPx(context, previewView.height.toFloat())
    val screenWidth = Common.dpFromPx(context, previewView.width.toFloat())

    fun Rect.toDistance(rect: Rect) : Double {
        return (rect.left - left).toDouble().pow(2) + (rect.top - top).toDouble().pow(2)
    }

    override fun getDetectionPositionStatus(): IdealPositionStatus {
        for (detectedObject in results) {
            val bottleWidth =
                (detectedObject.boundingBox.right - detectedObject.boundingBox.left).toFloat()
            val bottleHeight =
                (detectedObject.boundingBox.bottom - detectedObject.boundingBox.top).toFloat()
            val heightRatio = (bottleHeight/screenHeight)
            val widthRatio = (bottleWidth/screenWidth)
            val bottleCentreX = detectedObject.boundingBox.centerX()
            val bottleCentreY = detectedObject.boundingBox.centerY()
            val correctX = previewView.pivotX - (bottleWidth/2.0)
            val correctY = previewView.pivotY - (bottleHeight/2.0)

            val marginOffsetX = bottleWidth/3
            val marginOffsetY = bottleHeight/4
            if (heightRatio < 0.50) return IdealPositionStatus.SIZE_HEIGHT_SMALL
            if (heightRatio > 0.65) return IdealPositionStatus.SIZE_HEIGHT_LARGE

            if (bottleCentreX < (correctX - marginOffsetX)) return IdealPositionStatus.POSITION_LEFT
            if (bottleCentreX > correctX) return IdealPositionStatus.POSITION_RIGHT
            if (detectedObject.boundingBox.top < 100) return IdealPositionStatus.POSITION_TOP
            if (detectedObject.boundingBox.bottom > (screenHeight - 150)) return IdealPositionStatus.POSITION_BOTTOM

//            if (bottleCentreY < (correctY - marginOffsetY)) return IdealPositionStatus.POSITION_TOP
//            if (bottleCentreY > (correctY))  return IdealPositionStatus.POSITION_BOTTOM
        }
        return IdealPositionStatus.IDEAL
    }
}