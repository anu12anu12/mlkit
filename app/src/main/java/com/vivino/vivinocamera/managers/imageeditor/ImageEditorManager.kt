package com.vivino.vivinocamera.managers.imageeditor

import android.graphics.*
import com.vivino.vivinocamera.interfaces.ImageEditorInterface

class ImageEditorManager: ImageEditorInterface {
    override fun cropBitmapByRect(source: Bitmap, rectF: RectF): Bitmap {
        val resultBitmap = Bitmap.createBitmap(rectF.width().toInt(), rectF.height().toInt(), Bitmap.Config.RGB_565)
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        paint.color = Color.WHITE
        canvas.drawRect(RectF(0F, 0F, rectF.width(), rectF.height()), paint)
        val matrix = Matrix()
        matrix.postTranslate(-rectF.left, -rectF.top)
        canvas.drawBitmap(source, matrix, paint)
        return resultBitmap
    }

    override fun cutBottleAndGetBitMap(bottleBitmap: Bitmap, topCutOffValueInInt: Int): Bitmap {
        val topBottleCutArea = bottleBitmap.height * topCutOffValueInInt / 100
        val wineBottleHeight  = bottleBitmap.height - topBottleCutArea
        return Bitmap.createBitmap(
            bottleBitmap,
            0,
            topBottleCutArea,
            bottleBitmap.width,
            wineBottleHeight
        )
    }
}