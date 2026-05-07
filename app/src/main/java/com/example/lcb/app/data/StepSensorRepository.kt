package com.example.lcb.app.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StepSensorRepository(
    context: Context,
    private val preferences: AppPreferences,
    private val scope: CoroutineScope,
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var activeSensor: Sensor? = null
    private var detectorJob: Job? = null
    private var lastCounterTotal: Int? = null
    private val _status = MutableStateFlow(StepSensorStatus.Idle)

    val status: StateFlow<StepSensorStatus> = _status

    fun start(hasPermission: Boolean) {
        if (!hasPermission) {
            stop()
            _status.value = StepSensorStatus.PermissionRequired
            return
        }
        if (activeSensor != null) return
        val counter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val detector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        val sensor = counter ?: detector
        if (sensor == null) {
            _status.value = StepSensorStatus.Unsupported
            return
        }
        activeSensor = sensor
        val registered = sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        _status.value = if (registered) StepSensorStatus.Active else StepSensorStatus.Unsupported
    }

    fun stop() {
        activeSensor?.let { sensorManager.unregisterListener(this, it) }
        detectorJob?.cancel()
        detectorJob = null
        lastCounterTotal = null
        activeSensor = null
        if (_status.value == StepSensorStatus.Active) _status.value = StepSensorStatus.Idle
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val total = event.values.firstOrNull()?.toInt() ?: return
                if (lastCounterTotal == total) return
                lastCounterTotal = total
                scope.launch(Dispatchers.IO) { preferences.recordStepCounter(total) }
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                detectorJob = scope.launch(Dispatchers.IO) { preferences.recordDetectedStep() }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
