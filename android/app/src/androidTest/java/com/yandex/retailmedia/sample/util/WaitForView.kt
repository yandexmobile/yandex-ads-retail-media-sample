package com.yandex.retailmedia.sample.util

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.hamcrest.Matcher

fun waitUntil(timeoutMs: Long = 5_000, condition: () -> Boolean): Boolean {
    val deadline = System.currentTimeMillis() + timeoutMs
    while (System.currentTimeMillis() < deadline) {
        if (condition()) return true
        Thread.sleep(100)
    }
    return condition()
}

fun waitForView(matcher: Matcher<View>, timeoutMs: Long = 5_000): ViewInteraction {
    val deadline = System.currentTimeMillis() + timeoutMs
    while (System.currentTimeMillis() < deadline) {
        try {
            return onView(matcher).check(matches(isDisplayed()))
        } catch (_: NoMatchingViewException) {
            Thread.sleep(200)
        } catch (_: AssertionError) {
            Thread.sleep(200)
        }
    }
    return onView(matcher).check(matches(isDisplayed()))
}
