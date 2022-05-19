package com.vivino.vivinocamera.interfaces

import android.graphics.Rect
import com.google.mlkit.vision.objects.DetectedObject
import com.vivino.vivinocamera.managers.imageguide.IdealPositionStatus

interface BestBottlePositionInterface {
    fun getDetectionPositionStatus(detectedObjects: List<DetectedObject>) : IdealPositionStatus
    fun getBottleDetectionCloserToCenter(
        detectedObjects: List<DetectedObject>): Rect
}