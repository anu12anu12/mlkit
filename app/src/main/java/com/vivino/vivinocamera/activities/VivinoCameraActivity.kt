package com.vivino.vivinocamera.activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.vivino.vivinocamera.databinding.ActivityVivinoCameraBinding
import com.vivino.vivinocamera.interfaces.DetectedObjectListener
import com.vivino.vivinocamera.interfaces.VisionImageProcessor
import com.vivino.vivinocamera.managers.imageguide.ImageGuideManager
import com.vivino.vivinocamera.managers.objectdetector.ObjectDetectionManager
import com.vivino.vivinocamera.viewmodels.CameraXViewModel
import com.vivino.vivinocamera.utils.PreferenceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class VivinoCameraActivity : AppCompatActivity(), DetectedObjectListener {
    private lateinit var binding: ActivityVivinoCameraBinding
    private var cameraXViewModel: CameraXViewModel? = null

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var imageCaptureUseCase: ImageCapture? = null
    private var stillImageCaptureUseCase: ImageAnalysis? = null


    private var imageProcessor: VisionImageProcessor? = null
    private var stillImageProcessor: VisionImageProcessor? = null

    private var needUpdateGraphicOverlayImageSourceInfo = false
    private val lensFacing = CameraSelector.LENS_FACING_BACK
    private var cameraSelector: CameraSelector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        binding = ActivityVivinoCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        cameraXViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[CameraXViewModel::class.java]

        cameraXViewModel?.getProcessCameraProvider()?.observe(this) { cameraProviderIt ->
            cameraProvider = cameraProviderIt
            bindAllCameraUseCases()
        }
        binding.bottomControlsID.cameraButton.setOnClickListener {
            takePic()
        }
    }

    private fun bindAllCameraUseCases() {
        cameraProvider?.unbindAll()
        createPreviewUseCase()
        createAnalysisUseCase()
        createStillImageAnalysisUseCase()
        createImageCaptureUseCase()
        cameraSelector?.let {
            val camera = cameraProvider?.bindToLifecycle(this,
                it,
                previewUseCase,
                analysisUseCase,
                imageCaptureUseCase
            )
            camera?.let { cameraIt -> enableAutoFocus(cameraIt) }
        }
    }

    private fun enableAutoFocus(camera: Camera) {
        binding.previewView.previewStreamState.observe(this) { streamState ->
//            if (streamState.equals(PreviewView.StreamState.STREAMING)) {
//                setUpCameraAutoFocus(camera)
//            }
            setUpCameraAutoFocus(camera)
        }
    }

    private fun setUpCameraAutoFocus(camera: Camera) {
        val x = binding.previewView.x + binding.previewView.width
        val y = binding.previewView.y + binding.previewView.height
        val pointFactory = binding.previewView.meteringPointFactory
        val afPointWidth = 1.0f// Full  area
        val aePointWidth = afPointWidth * 1.5f
        val afPoint = pointFactory.createPoint(x, y, afPointWidth)
        val aePoint = pointFactory.createPoint(x, y, aePointWidth)
        camera.cameraControl.startFocusAndMetering(
            FocusMeteringAction.Builder(afPoint, FocusMeteringAction.FLAG_AF)
                .addPoint(aePoint, FocusMeteringAction.FLAG_AE
                )
                .setAutoCancelDuration(1, TimeUnit.SECONDS)
                .build()
        )
    }


    private fun createImageCaptureUseCase() {
        imageCaptureUseCase?.let {
            cameraProvider?.unbind(it)
        }
        val builder = ImageCapture.Builder()
        imageCaptureUseCase = builder.build()
    }

    private fun createStillImageAnalysisUseCase() {
        val localModel = LocalModel.Builder()
            .setAssetFilePath("custom_models/object_labeler.tflite")
            .build()
        val customObjectDetectorOptions =
            PreferenceUtils.getCustomObjectDetectorOptionsForStillImage(this, localModel)
        stillImageProcessor =
            ObjectDetectionManager(
                this,
                customObjectDetectorOptions,
                this@VivinoCameraActivity,
                CustomObjectDetectorOptions.SINGLE_IMAGE_MODE
            )

    }


    private fun takePic() {
        binding.bottomControlsID.guideText.visibility = View.GONE
        imageProcessor?.stop()
        val file = File(applicationContext.cacheDir, "takepic.jpg")
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCaptureUseCase?.takePicture(outputFileOptions, Executors.newSingleThreadExecutor(),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException) {
                    // insert your code here.
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    initiateWineBottleDetectionProcess(file)
                }
            })
    }

    private fun initiateWineBottleDetectionProcess(file: File) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Clear the overlay first
                val inputImage =
                    InputImage.fromFilePath(this@VivinoCameraActivity, Uri.fromFile(file))
                binding.graphicOverlay.clear()
                binding.graphicOverlay.setImageSourceInfo(
                    inputImage.width, inputImage.height, /* isFlipped= */false
                )
                (stillImageProcessor as? ObjectDetectionManager)?.apply {
                    setBitMapToScan(inputImage.bitmapInternal)
                    processImage(inputImage, binding.graphicOverlay)
                }
            } catch (e: IOException) {
                Log.e(
                    TAG,
                    "Error retrieving saved image"
                )
            }
        }
    }

    private fun createAnalysisUseCase() {
        analysisUseCase?.let {
            cameraProvider?.unbind(it)
        }
        imageProcessor?.stop()

        val localModel =
            LocalModel.Builder().setAssetFilePath("custom_models/object_labeler.tflite").build()
        val customObjectDetectorOptions =
            PreferenceUtils.getCustomObjectDetectorOptionsForLivePreview(this, localModel)
        imageProcessor =
            ObjectDetectionManager(
                this,
                customObjectDetectorOptions,
                this@VivinoCameraActivity,
                CustomObjectDetectorOptions.STREAM_MODE
            )
        val builder = ImageAnalysis.Builder()
        analysisUseCase = builder.build()
        needUpdateGraphicOverlayImageSourceInfo = true
        analysisUseCase?.setAnalyzer( // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(this)
        ) { imageProxy: ImageProxy ->
            if (needUpdateGraphicOverlayImageSourceInfo) {
                val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                if (rotationDegrees == 0 || rotationDegrees == 180) {
                    binding.graphicOverlay.setImageSourceInfo(
                        imageProxy.width, imageProxy.height, isImageFlipped
                    )
                } else {
                    binding.graphicOverlay.setImageSourceInfo(
                        imageProxy.height, imageProxy.width, isImageFlipped
                    )
                }
                needUpdateGraphicOverlayImageSourceInfo = false
            }
            try {
                imageProcessor?.processImageProxy(imageProxy, binding.graphicOverlay)
            } catch (e: MlKitException) {
                Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun createPreviewUseCase() {
        previewUseCase?.let { cameraProvider?.unbind(it) }
        val builder = Preview.Builder()
        previewUseCase = builder.build()
        previewUseCase?.setSurfaceProvider(binding.previewView.surfaceProvider)
    }


    companion object {
        private const val TAG = "VivinoCameraActivity"
    }

    override fun detectedObjects(results: List<DetectedObject>?, mode: Int) {
        when (mode) {
            CustomObjectDetectorOptions.SINGLE_IMAGE_MODE -> {
                results?.let { resultsIt ->
                    if (resultsIt.isNotEmpty() && resultsIt[0].labels.isNotEmpty()) {
                        Toast.makeText(
                            this,
                            "Object Detected ${resultsIt[0].labels[0].text}",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(this, "Object Detection Failed", Toast.LENGTH_LONG).show()
                    }

                } ?: run {
                    Toast.makeText(this, "Object Detection Failed", Toast.LENGTH_LONG).show()
                }
                bindAllCameraUseCases()
            }
            CustomObjectDetectorOptions.STREAM_MODE -> {
                results?.takeIf { it.isNotEmpty() }?.apply {
                    val imageGuideManager = ImageGuideManager(this@VivinoCameraActivity, binding.previewView)
                    val idealStatus = imageGuideManager.getDetectionPositionStatus(this)
                    binding.bottomControlsID.guideText.visibility = View.VISIBLE
                    binding.bottomControlsID.guideText.text = idealStatus.imageGuideText
                }
            }
        }
    }

}

