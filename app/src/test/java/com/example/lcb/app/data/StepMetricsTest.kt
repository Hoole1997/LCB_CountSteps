package com.example.lcb.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class StepMetricsTest {
    @Test
    fun progressIsClampedAtGoal() {
        val metrics = StepMetrics(steps = 12_000, goal = 8_000)

        assertEquals(1f, metrics.progress)
        assertEquals(100, metrics.percent)
        assertEquals("100", metrics.percentText)
    }

    @Test
    fun progressUsesRealStepRatio() {
        val metrics = StepMetrics(steps = 4_000, goal = 8_000)

        assertEquals(0.5f, metrics.progress)
        assertEquals(50, metrics.percent)
        assertEquals("50", metrics.percentText)
    }

    @Test
    fun progressTextKeepsSmallRealPercentVisible() {
        val metrics = StepMetrics(steps = 50, goal = 8_000)

        assertEquals(0.00625f, metrics.progress)
        assertEquals(0, metrics.percent)
        assertEquals("0.6", metrics.percentText)
    }

    @Test
    fun distanceAndCaloriesAreDerivedFromSteps() {
        val metrics = StepMetrics(steps = 1_000, goal = 8_000)

        assertEquals(0.57, metrics.distanceKm, 0.001)
        assertEquals(71, metrics.calories)
    }

    @Test
    fun distanceMatchesFigmaExampleScale() {
        val metrics = StepMetrics(steps = 4_200, goal = 8_000)

        assertEquals(2.394, metrics.distanceKm, 0.001)
    }

    @Test
    fun distanceKeepsSmallStepCountsNonZero() {
        val metrics = StepMetrics(steps = 50, goal = 8_000)

        assertEquals(0.0285, metrics.distanceKm, 0.0001)
    }
}
