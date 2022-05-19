package com.vivino.vivinocamera.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.StringRes;

import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.objects.ObjectDetectorOptionsBase;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
import com.vivino.vivinocamera.R;

public class PreferenceUtils {
    public static boolean isCameraLiveViewportEnabled(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String prefKey = context.getString(R.string.pref_key_camera_live_viewport);
        return sharedPreferences.getBoolean(prefKey, true);
    }

    public static boolean shouldHideDetectionInfo(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String prefKey = context.getString(R.string.pref_key_info_hide);
        return sharedPreferences.getBoolean(prefKey, false);
    }

    private static CustomObjectDetectorOptions getCustomObjectDetectorOptions(
            Context context,
            LocalModel localModel,
            @StringRes int prefKeyForMultipleObjects,
            @StringRes int prefKeyForClassification,
            @ObjectDetectorOptionsBase.DetectorMode int mode) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        boolean enableMultipleObjects =
                sharedPreferences.getBoolean(context.getString(prefKeyForMultipleObjects), false);
        boolean enableClassification =
                sharedPreferences.getBoolean(context.getString(prefKeyForClassification), true);

        CustomObjectDetectorOptions.Builder builder =
                new CustomObjectDetectorOptions.Builder(localModel).setDetectorMode(mode);
        if (enableMultipleObjects) {
            builder.enableMultipleObjects();
        }
        if (enableClassification) {
            builder.enableClassification().setMaxPerObjectLabelCount(1);
        }
        return builder.build();
    }

    public static CustomObjectDetectorOptions getCustomObjectDetectorOptionsForLivePreview(
            Context context, LocalModel localModel) {
        return getCustomObjectDetectorOptions(
                context,
                localModel,
                R.string.pref_key_live_preview_object_detector_enable_multiple_objects,
                R.string.pref_key_live_preview_object_detector_enable_classification,
                CustomObjectDetectorOptions.STREAM_MODE);
    }

    public static CustomObjectDetectorOptions getCustomObjectDetectorOptionsForStillImage(
            Context context, LocalModel localModel) {
        return getCustomObjectDetectorOptions(
                context,
                localModel,
                R.string.pref_key_still_image_object_detector_enable_multiple_objects,
                R.string.pref_key_still_image_object_detector_enable_classification,
                CustomObjectDetectorOptions.SINGLE_IMAGE_MODE);
    }


}
