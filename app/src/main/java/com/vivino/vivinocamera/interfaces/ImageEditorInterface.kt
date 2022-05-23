package com.vivino.vivinocamera.interfaces

import android.graphics.Bitmap
import android.graphics.RectF

interface ImageEditorInterface {
    fun cropBitmapByRect(source: Bitmap, rect: RectF) : Bitmap
    fun cutBottleAndGetBitMap(bottleBitmap: Bitmap, topCutOffValueInInt: Int): Bitmap
}