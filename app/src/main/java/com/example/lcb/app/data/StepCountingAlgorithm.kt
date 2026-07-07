package com.example.lcb.app.data

/**
 * Converts raw Android step sensor events into app-level business steps.
 *
 * TYPE_STEP_COUNTER reports a device-wide total since boot. The app stores its
 * own daily total and only adds sensor deltas while business counting is active.
 */
object StepCountingAlgorithm {
    data class State(
        val date: String?,
        val businessSteps: Int,
        val counterAnchorTotal: Int?,
        val isPaused: Boolean,
        val resumeAnchorPending: Boolean,
    )

    data class Result(
        val state: State,
        val finalizedRecord: StepDailyRecord?,
        val countedDelta: Int = 0,
    )

    fun ensureToday(state: State, today: String, goal: Int): Result {
        return rolloverIfNeeded(state, today, goal)
    }

    fun recordCounterSample(
        state: State,
        totalSinceBoot: Int,
        today: String,
        goal: Int,
        stepsAlreadyCounted: Int = 0,
    ): Result {
        val rolled = rolloverIfNeeded(state, today, goal)
        val current = rolled.state
        val total = totalSinceBoot.coerceAtLeast(0)
        val anchor = current.counterAnchorTotal
        var countedDelta = 0

        val updated = when {
            current.isPaused -> {
                current.copy(counterAnchorTotal = total, resumeAnchorPending = false)
            }
            current.resumeAnchorPending || anchor == null || anchor > total -> {
                current.copy(counterAnchorTotal = total, resumeAnchorPending = false)
            }
            else -> {
                val delta = total - anchor
                countedDelta = (delta - stepsAlreadyCounted.coerceIn(0, delta)).coerceAtLeast(0)
                current.copy(
                    businessSteps = (current.businessSteps + countedDelta).coerceAtLeast(0),
                    counterAnchorTotal = total,
                    resumeAnchorPending = false,
                )
            }
        }
        return rolled.copy(state = updated, countedDelta = countedDelta)
    }

    fun recordDetectorStep(state: State, today: String, goal: Int): Result {
        val rolled = rolloverIfNeeded(state, today, goal)
        val current = rolled.state
        val countedDelta: Int
        val updated = if (current.isPaused) {
            countedDelta = 0
            current.copy(resumeAnchorPending = false)
        } else {
            countedDelta = 1
            current.copy(
                businessSteps = current.businessSteps + 1,
                resumeAnchorPending = false,
            )
        }
        return rolled.copy(state = updated, countedDelta = countedDelta)
    }

    fun setPaused(state: State, paused: Boolean, today: String, goal: Int): Result {
        val rolled = rolloverIfNeeded(state, today, goal)
        val current = rolled.state
        val updated = when {
            paused -> current.copy(isPaused = true, resumeAnchorPending = false)
            current.isPaused -> current.copy(isPaused = false, resumeAnchorPending = true)
            else -> current.copy(isPaused = false, resumeAnchorPending = false)
        }
        return rolled.copy(state = updated)
    }

    fun setBusinessSteps(state: State, steps: Int, today: String, goal: Int): Result {
        val rolled = rolloverIfNeeded(state, today, goal)
        return rolled.copy(state = rolled.state.copy(businessSteps = steps.coerceAtLeast(0)))
    }

    private fun rolloverIfNeeded(state: State, today: String, goal: Int): Result {
        if (state.date == today) return Result(state = state, finalizedRecord = null)

        val finalizedRecord = state.date?.let { previousDay ->
            StepDailyRecord(
                date = previousDay,
                steps = state.businessSteps.coerceAtLeast(0),
                goal = goal.coerceAtLeast(1),
            )
        }
        return Result(
            state = state.copy(
                date = today,
                businessSteps = 0,
                counterAnchorTotal = null,
                resumeAnchorPending = false,
            ),
            finalizedRecord = finalizedRecord,
        )
    }
}
