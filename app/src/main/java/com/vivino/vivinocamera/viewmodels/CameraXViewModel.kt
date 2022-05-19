package com.vivino.vivinocamera.viewmodels

import android.app.Application
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.ExecutionException

class CameraXViewModel(application: Application) : AndroidViewModel(application) {
    private val _cameraProviderLiveData = MutableLiveData<ProcessCameraProvider>()
    fun getProcessCameraProvider(): LiveData<ProcessCameraProvider> {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(getApplication())
        ProcessCameraProvider.getInstance(getApplication()).addListener(
            {
                try {
                    _cameraProviderLiveData.setValue(cameraProviderFuture.get())
                } catch (e: ExecutionException) {
                    // Handle any errors (including cancellation) here.
                } catch (e: InterruptedException) {
                }
            },
            ContextCompat.getMainExecutor(getApplication())
        )

        return _cameraProviderLiveData
    }
}