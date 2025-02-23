package com.myattendance

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.myattendance.StartFaceActivity.Companion

import java.util.Locale

class MainActivity : AppCompatActivity() {


    private val faceViewModel: FaceRecognitionViewModel by viewModels()
    private lateinit var faceDetector: FaceDetector

    private lateinit var textToSpeech: TextToSpeech
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (allPermissionsGranted())
        else ActivityCompat.requestPermissions(
            this,
            MainActivity.REQUIRED_PERMISSIONS,
            MainActivity.REQUEST_CODE_PERMISSIONS
        )

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) textToSpeech.language = Locale.US
        }


        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .build()
        faceDetector = FaceDetection.getClient(options)

        findViewById<Button>(R.id.btnCapture).setOnClickListener {
            startActivity(Intent(this, StartFaceActivity::class.java))
        }

        faceViewModel.faceMatchResult.observe(this) { result ->
            when (result.statusType) {
                FaceStatusType.REGISTERED -> {
                    showToast(result.message)
                    speakText(result.message)
                    println("🔹 Network error: ${result.message}") // ✅ Log network error
                }

                FaceStatusType.MATCHED -> {
                    showToast(result.message)
                    // Navigate or mark attendance
                    speakText(result.message)
                    println("🔹 Network error: ${result.message}") // ✅ Log network error
                }

                FaceStatusType.NOT_MATCHED -> {
                    showToast(result.message)
                    // Handle failure case
                    speakText(result.message)
                }
            }
        }


    }
    /* override fun onResume() {
         super.onResume()
         faceViewModel.faceMatchResult.value?.let { result ->
             handleFaceMatchResult(result)
         }
     }

     private fun handleFaceMatchResult(result: FaceMatchResult) {
         when (result.statusType) {
             FaceStatusType.REGISTERED, FaceStatusType.MATCHED -> {
                 showToast(result.message)
                 speakText(result.message)
             }
             FaceStatusType.NOT_MATCHED -> {
                 showToast(result.message)
                 speakText(result.message)
             }
         }
     }*/

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    }

    private fun speakText(text: String) =
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)

    private fun showToast(message: String) = runOnUiThread {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
