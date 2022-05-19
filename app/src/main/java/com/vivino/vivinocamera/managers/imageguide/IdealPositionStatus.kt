package com.vivino.vivinocamera.managers.imageguide

enum class IdealPositionStatus(
    val imageGuideText: String,
    val captureButtonState: CaptureButtonState
) {
    SIZE_WIDTH_SMALL("↕️ Move closer", CaptureButtonState.CAPTURE_DEFAULT),
    SIZE_WIDTH_LARGE("↕️ Move back", CaptureButtonState.CAPTURE_DEFAULT),
    SIZE_HEIGHT_SMALL("↕️ Move closer", CaptureButtonState.CAPTURE_DEFAULT),
    SIZE_HEIGHT_LARGE("↕️ Move back", CaptureButtonState.CAPTURE_DEFAULT),
    POSITION_LEFT("➡️ Move right", CaptureButtonState.CAPTURE_DEFAULT),
    POSITION_RIGHT("⬅️ Move left", CaptureButtonState.CAPTURE_DEFAULT),
    POSITION_TOP("⬇️ Move down", CaptureButtonState.CAPTURE_DEFAULT),
    POSITION_BOTTOM("⬆️ Move up", CaptureButtonState.CAPTURE_DEFAULT),

    IDEAL("✅ Hold Still ✅", CaptureButtonState.CAPTURE_IDEAL),

    // When we have the userVintage already
    CAPTURE_READY("✅ Ready to capture ✅", CaptureButtonState.CAPTURE_READY),
    NONE("Scan a Wine", CaptureButtonState.CAPTURE_DEFAULT);
}

enum class CaptureButtonState {
    CAPTURE_DEFAULT,
    CAPTURE_IDEAL,
    CAPTURE_READY;
}