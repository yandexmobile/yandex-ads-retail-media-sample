@file:OptIn(com.yandex.mobile.ads.retailmedia.RetailMediaApi::class)

package com.yandex.retailmedia.sample

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.yandex.retailmedia.sample.app.MainActivity
import com.yandex.retailmedia.sample.di.AdTestDoubles
import com.yandex.retailmedia.sample.util.MockBackendRule
import com.yandex.retailmedia.sample.util.waitForView
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class HomeSmokeTest {

    private val dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse = when {
            request.path == "/home" -> MockResponse().setBody(
                """{"products":{"items":[{"id":"p1","name":"Milk","price":1.0,"currencyId":"RUB","picture":"http://localhost/p1.png","categoryId":"c1","url":"http://localhost/p1","available":true,"sponsored":false}],"page":1,"pageSize":20,"hasMore":false},"adSlot":null}"""
            )
            request.path?.startsWith("/cart") == true -> MockResponse().setBody(
                """{"items":[]}"""
            )
            else -> MockResponse().setResponseCode(404)
        }
    }

    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val backend = MockBackendRule(dispatcher)

    @Before
    fun setUp() {
        AdTestDoubles.reset()
        coEvery { AdTestDoubles.repository.loadProductPromo(any(), any(), any()) } returns emptyList()
        coEvery { AdTestDoubles.repository.loadDisplayAds(any(), any()) } returns emptyList()
    }

    @Test
    fun homeShowsProducts() {
        ActivityScenario.launch(MainActivity::class.java)
        waitForView(withText("Milk"), timeoutMs = 10_000)
        onView(withText("Milk")).check(matches(isDisplayed()))
    }
}
