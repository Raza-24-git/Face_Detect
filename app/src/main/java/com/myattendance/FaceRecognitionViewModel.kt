package com.myattendance

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FaceRecognitionViewModel(application: Application) : AndroidViewModel(application) {

    private val _faceMatchResult = MutableLiveData<FaceMatchResult>()
    val faceMatchResult: LiveData<FaceMatchResult> = _faceMatchResult

    fun setFaceMatchResult(result: FaceMatchResult) {
        Log.d("FaceViewModel", "setFaceMatchResult called with: ${result.message}")
        _faceMatchResult.postValue(result)
    }
}
