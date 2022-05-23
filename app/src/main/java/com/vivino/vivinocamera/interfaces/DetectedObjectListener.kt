package com.vivino.vivinocamera.interfaces

import android.graphics.Bitmap
import android.support.annotation.NonNull
import android.support.annotation.Nullable
import com.google.mlkit.vision.objects.DetectedObject

interface DetectedObjectListener {
    fun detectedObjects(
        @NonNull mode: Int,
        @Nullable results: List<DetectedObject>?,
        @Nullable originalBitmap: Bitmap?
    )
}