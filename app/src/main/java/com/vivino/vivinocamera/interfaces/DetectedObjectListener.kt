package com.vivino.vivinocamera.interfaces

import android.graphics.Bitmap
import android.support.annotation.NonNull
import android.support.annotation.Nullable
import com.google.mlkit.vision.objects.DetectedObject

interface DetectedObjectListener {
    fun detectedObjects(
        @Nullable results: List<DetectedObject>?,
        @NonNull mode: Int
    )
}