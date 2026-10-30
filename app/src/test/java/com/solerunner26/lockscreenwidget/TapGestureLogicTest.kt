package com.solerunner26.lockscreenwidget

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM unit tests for the pure double-tap logic.
 * Run with: ./gradlew :app:testDebugUnitTest
 */
class TapGestureLogicTest {

    private val window = 450L

    @Test
    fun firstTapNeverLocks() {
        assertFalse(TapGestureLogic.isDoubleTap(TapGestureLogic.NO_TAP, 1000L, window))
    }

    @Test
    fun twoTapsWithinWindowLock() {
        val first = 1000L
        assertTrue(TapGestureLogic.isDoubleTap(first, first + 1L, window))
        assertTrue(TapGestureLogic.isDoubleTap(first, first + 300L, window))
    }

    @Test
    fun windowBoundaryIsInclusive() {
        assertTrue(TapGestureLogic.isDoubleTap(1000L, 1000L + window, window))
    }

    @Test
    fun tapsOutsideWindowDoNotLock() {
        assertFalse(TapGestureLogic.isDoubleTap(1000L, 1000L + window + 1L, window))
        assertFalse(TapGestureLogic.isDoubleTap(1000L, 1000L + 60_000L, window))
    }

    @Test
    fun backwardsClockIsNeverADoubleTap() {
        // Clock moved backwards (NTP fix etc.) — must not lock.
        assertFalse(TapGestureLogic.isDoubleTap(2000L, 1000L, window))
    }

    @Test
    fun sameMillisecondCountsAsDoubleTap() {
        assertTrue(TapGestureLogic.isDoubleTap(5000L, 5000L, window))
    }

    @Test
    fun zeroWindowOnlyAcceptsSameInstant() {
        assertTrue(TapGestureLogic.isDoubleTap(5L, 5L, 0L))
        assertFalse(TapGestureLogic.isDoubleTap(5L, 6L, 0L))
    }
}
