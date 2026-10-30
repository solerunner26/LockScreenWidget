#!/usr/bin/env python3
"""
Algorithm spec test for the double-tap logic.

This mirrors app/src/main/java/com/solerunner26/lockscreenwidget/TapGestureLogic.kt
line-for-line and property-tests it with randomized tap sequences, so the gesture
behavior is verified even in environments without a JDK/Android SDK (where the
Kotlin JUnit tests in app/src/test cannot run).

Run: python3 tools/logic_spec_test.py
"""
import random
import sys

NO_TAP = -1


def is_double_tap(last_tap_at: int, now: int, window_ms: int) -> bool:
    """Mirror of TapGestureLogic.isDoubleTap."""
    if last_tap_at == NO_TAP:
        return False
    delta = now - last_tap_at
    return 0 <= delta <= window_ms


def simulate(taps, window_ms, mode="double"):
    """Mirror of LockWidgetProvider.handleTap: returns list of indices that locked."""
    last = NO_TAP
    locked = []
    for i, t in enumerate(taps):
        should_lock = True if mode == "single" else is_double_tap(last, t, window_ms)
        if should_lock:
            last = NO_TAP
            locked.append(i)
        else:
            last = t
    return locked


def check(cond, msg):
    if not cond:
        print(f"FAIL: {msg}")
        sys.exit(1)


def main():
    W = 450

    # Edge cases
    check(not is_double_tap(NO_TAP, 1000, W), "first tap must not lock")
    check(is_double_tap(1000, 1000, W), "same-instant tap locks")
    check(is_double_tap(1000, 1450, W), "window boundary inclusive")
    check(not is_double_tap(1000, 1451, W), "just outside window must not lock")
    check(not is_double_tap(2000, 1000, W), "backwards clock must not lock")
    check(is_double_tap(1000, 1300, W), "inside window locks")

    # Double-tap mode: exactly two taps -> exactly one lock, on the second tap
    check(simulate([1000, 1300], W) == [1], "two quick taps lock on 2nd tap")
    # Slow second tap -> no lock
    check(simulate([1000, 2000], W) == [], "slow taps do not lock")
    # Triple tap -> exactly one lock (no retrigger on 3rd tap)
    check(simulate([1000, 1200, 1400], W) == [1], "triple tap locks exactly once")
    # Two separate double taps -> two locks
    check(simulate([1000, 1200, 5000, 5200], W) == [1, 3], "two double taps -> two locks")
    # Single-tap mode locks on every tap
    check(simulate([1000, 1001, 1002], W, mode="single") == [0, 1, 2], "single mode locks each tap")

    # Randomized property test: no lock may ever occur without a preceding tap
    # within the window; and two taps within the window must lock exactly once.
    rng = random.Random(20261030)
    for trial in range(20000):
        n = rng.randint(1, 8)
        taps = sorted(rng.randint(0, 10_000) for _ in range(n))
        window = rng.choice([300, 450, 600])
        locked = simulate(taps, window)
        # recompute expected independently
        expected = []
        last = NO_TAP
        for i, t in enumerate(taps):
            if last != NO_TAP and 0 <= t - last <= window:
                expected.append(i)
                last = NO_TAP
            else:
                last = t
        check(locked == expected, f"mismatch on {taps} w={window}: {locked} != {expected}")
        # a lock at index i requires taps[i-1] within window and taps[i-1] not itself a lock
        for i in locked:
            check(i > 0 and taps[i] - taps[i - 1] <= window, "lock without recent prior tap")

    print("logic_spec_test: ALL PASSED (edge cases + 20000 randomized sequences)")


if __name__ == "__main__":
    main()
