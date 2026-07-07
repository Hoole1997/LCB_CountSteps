package com.example.lcb.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StepCountingAlgorithmTest {
    @Test
    fun firstCounterSampleAnchorsWithoutChangingBusinessSteps() {
        val result = StepCountingAlgorithm.recordCounterSample(
            state = emptyState(),
            totalSinceBoot = 1_000,
            today = Today,
            goal = Goal,
        )

        assertEquals(Today, result.state.date)
        assertEquals(0, result.state.businessSteps)
        assertEquals(1_000, result.state.counterAnchorTotal)
        assertNull(result.finalizedRecord)
    }

    @Test
    fun activeCounterSamplesAddSystemDeltaToBusinessSteps() {
        val anchored = emptyState().copy(date = Today, businessSteps = 20, counterAnchorTotal = 1_000)

        val result = StepCountingAlgorithm.recordCounterSample(
            state = anchored,
            totalSinceBoot = 1_035,
            today = Today,
            goal = Goal,
        )

        assertEquals(55, result.state.businessSteps)
        assertEquals(1_035, result.state.counterAnchorTotal)
        assertEquals(35, result.countedDelta)
    }

    @Test
    fun counterSamplesDoNotDuplicateDetectorStepsAlreadyCounted() {
        val anchored = emptyState().copy(date = Today, businessSteps = 25, counterAnchorTotal = 1_000)

        val result = StepCountingAlgorithm.recordCounterSample(
            state = anchored,
            totalSinceBoot = 1_010,
            today = Today,
            goal = Goal,
            stepsAlreadyCounted = 7,
        )

        assertEquals(28, result.state.businessSteps)
        assertEquals(1_010, result.state.counterAnchorTotal)
        assertEquals(3, result.countedDelta)
    }

    @Test
    fun detectorStepReportsCountedDelta() {
        val active = emptyState().copy(date = Today, businessSteps = 12)

        val result = StepCountingAlgorithm.recordDetectorStep(
            state = active,
            today = Today,
            goal = Goal,
        )

        assertEquals(13, result.state.businessSteps)
        assertEquals(1, result.countedDelta)
    }

    @Test
    fun pausedCounterSamplesOnlyMoveAnchor() {
        val paused = emptyState().copy(
            date = Today,
            businessSteps = 80,
            counterAnchorTotal = 1_000,
            isPaused = true,
        )

        val result = StepCountingAlgorithm.recordCounterSample(
            state = paused,
            totalSinceBoot = 1_200,
            today = Today,
            goal = Goal,
        )

        assertEquals(80, result.state.businessSteps)
        assertEquals(1_200, result.state.counterAnchorTotal)
        assertTrue(result.state.isPaused)
    }

    @Test
    fun resumeRequestsFreshCounterAnchorBeforeAddingDeltas() {
        val paused = emptyState().copy(
            date = Today,
            businessSteps = 80,
            counterAnchorTotal = 1_000,
            isPaused = true,
        )

        val resumed = StepCountingAlgorithm.setPaused(paused, paused = false, today = Today, goal = Goal)
        val anchoredAfterResume = StepCountingAlgorithm.recordCounterSample(
            state = resumed.state,
            totalSinceBoot = 1_250,
            today = Today,
            goal = Goal,
        )
        val countedAfterAnchor = StepCountingAlgorithm.recordCounterSample(
            state = anchoredAfterResume.state,
            totalSinceBoot = 1_260,
            today = Today,
            goal = Goal,
        )

        assertFalse(resumed.state.isPaused)
        assertTrue(resumed.state.resumeAnchorPending)
        assertEquals(80, anchoredAfterResume.state.businessSteps)
        assertEquals(90, countedAfterAnchor.state.businessSteps)
    }

    @Test
    fun rolloverFinalizesPreviousDayAndResetsBusinessSteps() {
        val yesterday = emptyState().copy(
            date = Yesterday,
            businessSteps = 3_456,
            counterAnchorTotal = 8_000,
            isPaused = true,
        )

        val result = StepCountingAlgorithm.ensureToday(yesterday, today = Today, goal = Goal)

        assertEquals(Yesterday, result.finalizedRecord?.date)
        assertEquals(3_456, result.finalizedRecord?.steps)
        assertEquals(Today, result.state.date)
        assertEquals(0, result.state.businessSteps)
        assertNull(result.state.counterAnchorTotal)
        assertTrue(result.state.isPaused)
    }

    private fun emptyState(): StepCountingAlgorithm.State {
        return StepCountingAlgorithm.State(
            date = null,
            businessSteps = 0,
            counterAnchorTotal = null,
            isPaused = false,
            resumeAnchorPending = false,
        )
    }

    private companion object {
        const val Yesterday = "2026-07-01"
        const val Today = "2026-07-02"
        const val Goal = 8_000
    }
}
