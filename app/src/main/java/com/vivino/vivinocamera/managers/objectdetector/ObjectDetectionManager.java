package com.vivino.vivinocamera.managers.objectdetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.ObjectDetectorOptionsBase;
import com.vivino.vivinocamera.interfaces.DetectedObjectListener;
import com.vivino.vivinocamera.view.GraphicOverlay;
import com.vivino.vivinocamera.view.ObjectGraphic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A processor to run object detector. */
public class ObjectDetectionManager extends VisionProcessorBase<List<DetectedObject>> {

  private static final String TAG = "ObjectDetectionManager";

  private final ObjectDetector detector;
  private final DetectedObjectListener detectObjectListener;
  private @Nullable Bitmap originalBitmapImage;
  private final int processingMode;
  private Map<String, Boolean> bottleKeywordList  = new HashMap<String, Boolean>() {{
    put("bottle", true);
    put("container", true);
    put("tableware", true);
    put("wine bottle", true);
    put("packaged goods", true);
  }};


  public void setBitMapToScan(final Bitmap originalBitmapImage) {
    this.originalBitmapImage = originalBitmapImage;
  }

  public ObjectDetectionManager(
          Context context,
          ObjectDetectorOptionsBase options,
          final DetectedObjectListener detectObjectListener,
          final int mode
  ) {
    super(context);
    detector = ObjectDetection.getClient(options);
    this.processingMode = mode;
    this.detectObjectListener = detectObjectListener;
  }

  @Override
  public void stop() {
    super.stop();
    detector.close();
  }

  @Override
  protected Task<List<DetectedObject>> detectInImage(InputImage image) {
    return detector.process(image);
  }

  @Override
  protected void onSuccess(
      @NonNull List<DetectedObject> results, @NonNull GraphicOverlay graphicOverlay) {
    final List<DetectedObject> detectedObjects = new ArrayList<>();
    for (final DetectedObject object : results) {
      if (isBottle(object)) {
        graphicOverlay.add(new ObjectGraphic(graphicOverlay, object));
        detectedObjects.add(object);
      }
    }
    notifyObservers(detectedObjects);
  }

  private boolean isBottle(final DetectedObject object) {
    boolean isBottle = false;
    for (DetectedObject.Label label : object.getLabels()) {
      Log.d(TAG, "Label : " + label.getText());
      if (!TextUtils.isEmpty(label.getText()) &&
              bottleKeywordList.get(label.getText().toLowerCase()) != null) {
          isBottle = true;
          break;
      }
    }
    return isBottle;
  }

  private void notifyObservers(@Nullable final List<DetectedObject> results) {
    if (detectObjectListener != null) {
      detectObjectListener.detectedObjects(processingMode, results, originalBitmapImage);
    }
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Object detection failed!", e);
    notifyObservers(null);
  }
}
