package com.solerunner26.lockscreenwidget

/**
 * Pure, framework-free tap-gesture logic.
 *
 * Kept separate from Android classes on purpose so it can be unit-tested on a plain
 * JVM (see app/src/test/java/.../TapGestureLogicTest.kt) and mirrored by the
 * Python spec test in tools/logic_spec_test.py.
 */
object TapGestureLogic {

    /** Sentinel meaning "no previous tap". */
    const val NO_TAP: Long = -1L

    /**
     * Decides whether a tap at [now] completes a double tap with the previous tap at
     * [lastTapAt].
     *
     * @param lastTapAt timestamp of the previous tap (same clock as [now]), or [NO_TAP]
     * @param now       timestamp of the current tap
     * @param windowMs  maximum allowed gap between the two taps, inclusive
     * @return true when the two taps form a double tap
     */
    fun isDoubleTap(lastTapAt: Long, now: Long, windowMs: Long): Boolean {
        if (lastTapAt == NO_TAP) return false
        val delta = now - lastTapAt
        // A negative delta means the clock moved backwards (NTP adjustment etc.);
        // never treat that as a double tap.
        return delta in 0..windowMs
    }
}
