package com.example.lcb.app.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class StepSensorRepository(
    context: Context,
    private val preferences: AppPreferences,
    private val scope: CoroutineScope,
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val activeSensors = mutableListOf<Sensor>()
    private val detectorStepsPendingCounterSync = AtomicInteger(0)
    private var counterRefreshJob: Job? = null
    private var lastCounterTotal: Int? = null
    private val _status = MutableStateFlow(StepSensorStatus.Idle)

    val status: StateFlow<StepSensorStatus> = _status

    fun start(hasPermission: Boolean) {
        if (!hasPermission) {
            stop()
            _status.value = StepSensorStatus.PermissionRequired
            return
        }
        if (activeSensors.isNotEmpty()) return
        val counter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val detector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        val sensors = listOfNotNull(counter, detector).distinctBy { it.type }
        if (sensors.isEmpty()) {
            _status.value = StepSensorStatus.Unsupported
            return
        }
        sensors.forEach { sensor ->
            val registered = registerStepSensor(sensor)
            if (registered) activeSensors += sensor
        }
        counter
            ?.takeIf { activeSensors.any { active -> active.type == Sensor.TYPE_STEP_COUNTER } }
            ?.let(::startCounterRefresh)
        _status.value = if (activeSensors.isNotEmpty()) StepSensorStatus.Active else StepSensorStatus.Unsupported
    }

    fun stop() {
        counterRefreshJob?.cancel()
        counterRefreshJob = null
        activeSensors.forEach { sensorManager.unregisterListener(this, it) }
        activeSensors.clear()
        detectorStepsPendingCounterSync.set(0)
        lastCounterTotal = null
        if (_status.value == StepSensorStatus.Active) _status.value = StepSensorStatus.Idle
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val total = event.values.firstOrNull()?.toInt() ?: return
                if (lastCounterTotal == total) return
                val counterDelta = lastCounterTotal
                    ?.let { previous -> (total - previous).coerceAtLeast(0) }
                    ?: 0
                val detectorStepsAlreadyCounted = consumePendingDetectorSteps(counterDelta)
                lastCounterTotal = total
                scope.launch(Dispatchers.IO) {
                    preferences.recordStepCounter(
                        totalSinceBoot = total,
                        stepsAlreadyCounted = detectorStepsAlreadyCounted,
                    )
                }
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                scope.launch(Dispatchers.IO) {
                    val countedDelta = preferences.recordDetectedStep()
                    if (countedDelta > 0) detectorStepsPendingCounterSync.addAndGet(countedDelta)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun consumePendingDetectorSteps(counterDelta: Int): Int {
        if (counterDelta <= 0) return 0
        while (true) {
            val pending = detectorStepsPendingCounterSync.get()
            if (pending <= 0) return 0
            val consumed = minOf(pending, counterDelta)
            if (detectorStepsPendingCounterSync.compareAndSet(pending, pending - consumed)) {
                return consumed
            }
        }
    }

    private fun registerStepSensor(sensor: Sensor): Boolean {
        return sensorManager.registerListener(
            this,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL,
            0,
        )
    }

    private fun startCounterRefresh(counter: Sensor) {
        counterRefreshJob?.cancel()
        counterRefreshJob = scope.launch(Dispatchers.Main.immediate) {
            while (isActive && activeSensors.any { it.type == Sensor.TYPE_STEP_COUNTER }) {
                delay(CounterRefreshIntervalMs)

                // Some ROMs update the hardware step counter but do not push every
                // on-change event to an already registered foreground listener. A
                // lightweight re-register asks SensorManager for the latest system
                // counter snapshot without changing the business counting algorithm.
                sensorManager.unregisterListener(this@StepSensorRepository, counter)
                registerStepSensor(counter)
            }
        }
    }

    private companion object {
        const val CounterRefreshIntervalMs = 2_000L
    }
}
