package com.vivino.vivinocamera.interfaces

import com.vivino.vivinocamera.managers.imageguide.IdealPositionStatus

interface BestBottlePositionInterface {
    fun getDetectionPositionStatus() : IdealPositionStatus
}