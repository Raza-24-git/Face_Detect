package com.myattendance

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import java.util.Locale
import java.util.concurrent.Executors

class StartFaceActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var faceDetector: FaceDetector
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var textToSpeech: TextToSpeech
    private val faceViewModel: FaceRecognitionViewModel by viewModels() // ViewModel for LiveData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face)

        previewView = findViewById(R.id.previewView)
        sharedPreferences = getSharedPreferences("FaceData", Context.MODE_PRIVATE)

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) textToSpeech.language = Locale.US
        }

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .build()
        faceDetector = FaceDetection.getClient(options)

        if (allPermissionsGranted()) startCamera()
        else ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                processImage(imageProxy)
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)

        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val faceData = extractFaceData(faces[0])

                        if (isFirstTimeUser()) {
                            saveFaceData(faceData)
                            val result = FaceMatchResult(
                                isMatched = true,
                                message = "Face Registered Successfully!",
                                statusType = FaceStatusType.REGISTERED
                            )
                          //  faceViewModel.setFaceMatchResult(result)
                            faceViewModel.setFaceMatchResult(result)
                            runOnUiThread { speakText(result.message) }  // Debugging step
                            Handler(Looper.getMainLooper()).postDelayed({
                                finish()
                            }, 500)

                           // showToast(result.message)
                          //  finish()
                        } else {
                            val storedFaceData = getStoredFaceData()
                            val isMatched = compareFaceData(storedFaceData, faceData)

                            val result = if (isMatched) {
                                FaceMatchResult(
                                    isMatched = true,
                                    message = "Face Matched! Attendance Done.",
                                    statusType = FaceStatusType.MATCHED
                                )
                            } else {
                                FaceMatchResult(
                                    isMatched = false,
                                    message = "Face Not Matched! Try Again.",
                                    statusType = FaceStatusType.NOT_MATCHED
                                )
                            }

                           // faceViewModel.setFaceMatchResult(result)
                           // showToast(result.message)
                            faceViewModel.setFaceMatchResult(result)
                            runOnUiThread { speakText(result.message) }  // Debugging step
                            Handler(Looper.getMainLooper()).postDelayed({
                                finish()
                            }, 500)
                            if (isMatched) {
                                finish()
                            }
                        }
                    }
                }
                .addOnFailureListener { Log.e("Face Detection", "Failed to process image", it) }
                .addOnCompleteListener { imageProxy.close() }
        }
    }

    private fun extractFaceData(face: Face): FloatArray {
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position
        val nose = face.getLandmark(FaceLandmark.NOSE_BASE)?.position
        return floatArrayOf(
            leftEye?.x ?: 0f, leftEye?.y ?: 0f,
            rightEye?.x ?: 0f, rightEye?.y ?: 0f,
            nose?.x ?: 0f, nose?.y ?: 0f
        )
    }

    private fun saveFaceData(faceData: FloatArray) {
        with(sharedPreferences.edit()) {
            putString("face_data", faceData.joinToString(","))
            apply()
        }
    }

    private fun getStoredFaceData(): FloatArray {
        val storedData = sharedPreferences.getString("face_data", null) ?: return FloatArray(2)
        return storedData.split(",").map { it.toFloat() }.toFloatArray()
    }

    private fun isFirstTimeUser(): Boolean = sharedPreferences.getString("face_data", null) == null

    private fun compareFaceData(storedData: FloatArray, newData: FloatArray): Boolean {
        if (storedData.isEmpty() || newData.isEmpty() || storedData.size != newData.size) return false

        val squaredDifferences = storedData.zip(newData) { s, n -> (s - n) * (s - n) }
        val sum = squaredDifferences.sum()
        val distance = Math.sqrt(sum.toDouble())

        return distance < 50.0  // Adjust threshold as needed
    }


    private fun showToast(message: String) = runOnUiThread {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun speakText(text: String) = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    }
}


