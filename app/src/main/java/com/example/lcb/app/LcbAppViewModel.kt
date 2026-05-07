package com.example.lcb.app

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lcb.app.data.AppData
import com.example.lcb.app.data.AppPreferences
import com.example.lcb.app.data.HomeData
import com.example.lcb.app.data.HydrateData
import com.example.lcb.app.data.ReportData
import com.example.lcb.app.data.StepSensorRepository
import com.example.lcb.app.data.StepSensorStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LcbAppViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = AppPreferences(application)
    private val stepSensorRepository = StepSensorRepository(application, preferences, viewModelScope)

    val appData: StateFlow<AppData> = preferences.data.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppData(),
    )

    val language: StateFlow<String> = preferences.language.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = "en",
    )

    val homeData: StateFlow<HomeData> = preferences.homeData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeData(),
    )

    val reportData: StateFlow<ReportData> = preferences.reportData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReportData(),
    )

    val hydrateData: StateFlow<HydrateData> = preferences.hydrateData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HydrateData(),
    )

    val sensorStatus: StateFlow<StepSensorStatus> = stepSensorRepository.status

    init {
        viewModelScope.launch(Dispatchers.IO) { preferences.ensureToday() }
    }

    fun activateStepSensor() {
        stepSensorRepository.start(hasActivityRecognitionPermission())
    }

    fun onPermissionResult() {
        stepSensorRepository.start(hasActivityRecognitionPermission())
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch(Dispatchers.IO) { preferences.addWater(amountMl) }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch(Dispatchers.IO) { preferences.setLanguage(language) }
    }

    fun setStepGoal(goal: Int) {
        viewModelScope.launch(Dispatchers.IO) { preferences.setStepGoal(goal) }
    }

    fun hasActivityRecognitionPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.ACTIVITY_RECOGNITION,
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCleared() {
        stepSensorRepository.stop()
        super.onCleared()
    }
}
