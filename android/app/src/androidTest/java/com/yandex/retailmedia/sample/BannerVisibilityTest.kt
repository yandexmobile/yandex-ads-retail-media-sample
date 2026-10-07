@file:OptIn(com.yandex.mobile.ads.retailmedia.RetailMediaApi::class)

package com.yandex.retailmedia.sample

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.yandex.mobile.ads.common.AdBindingResult
import com.yandex.mobile.ads.retailmedia.RetailMediaAd
import com.yandex.mobile.ads.retailmedia.type.RetailMediaAdType
import com.yandex.retailmedia.sample.app.MainActivity
import com.yandex.retailmedia.sample.di.AdTestDoubles
import com.yandex.retailmedia.sample.util.MockBackendRule
import com.yandex.retailmedia.sample.util.waitForView
import com.yandex.retailmedia.sample.util.waitUntil
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.Matchers.not
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList

@HiltAndroidTest
class BannerVisibilityTest {

    private var adsReturnsSlot: Boolean = false

    private val adsRequests = CopyOnWriteArrayList<String>()

    private val dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse = when {
            request.path == "/home" -> MockResponse().setBody(
                """{"products":{"items":[$HOME_PRODUCT],"page":1,"pageSize":20,"hasMore":false},"adSlot":null}"""
            )
            request.path == "/categories" -> MockResponse().setBody(
                """[{"id":"c1","name":"Food"}]"""
            )
            request.path?.startsWith("/cart") == true -> MockResponse().setBody(
                """{"items":[]}"""
            )
            request.method == "POST" && request.path == "/ads" -> if (adsReturnsSlot) {
                adsRequests += request.body.readUtf8()
                MockResponse().setBody("""{"adUnitId":"R-M-cart","readyResponse":"RR"}""")
            } else {
                MockResponse().setResponseCode(204)
            }
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
    fun categoriesBannerShownWhenSlotReturned() {
        adsReturnsSlot = true
        stubLoadedBanner()

        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.categoriesFragment)).perform(click())
        waitForView(withId(R.id.ad_banner), timeoutMs = 10_000)
        onView(withId(R.id.ad_banner)).check(matches(isDisplayed()))
    }

    @Test
    fun categoriesBannerHiddenOn204() {
        adsReturnsSlot = false
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.categoriesFragment)).perform(click())
        waitForView(withText("Food"), timeoutMs = 10_000)
        onView(withId(R.id.ad_banner)).check(matches(not(isDisplayed())))
    }

    @Test
    fun homeSliderShownWhenSlotReturnsSeveralAds() {
        adsReturnsSlot = true
        coEvery { AdTestDoubles.repository.loadDisplayAds(any(), any()) } returns
            listOf(boundAd(), boundAd())

        ActivityScenario.launch(MainActivity::class.java)
        waitForView(withId(R.id.ad_slider), timeoutMs = 10_000)
        onView(withId(R.id.ad_slider))
            .check(matches(isDescendantOfA(withId(R.id.recycler))))
            .check(matches(isDisplayed()))
    }

    @Test
    fun categoriesBannerHiddenWhenBindingFails() {
        adsReturnsSlot = true
        val banner = mockk<RetailMediaAd>(relaxed = true) {
            every { adType } returns RetailMediaAdType.CONTENT
            every { bindRetailMediaAd(any(), any()) } returns
                AdBindingResult.Failure("media", RuntimeException("missing asset"))
        }
        coEvery { AdTestDoubles.repository.loadDisplayAds(any(), any()) } returns listOf(banner)

        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.categoriesFragment)).perform(click())
        waitForView(withText("Food"), timeoutMs = 10_000)
        onView(withId(R.id.ad_banner)).check(matches(not(isDisplayed())))
    }

    @Test
    fun homeSliderHiddenOn204() {
        adsReturnsSlot = false
        ActivityScenario.launch(MainActivity::class.java)
        waitForView(withText("Milk"), timeoutMs = 10_000)
        onView(withId(R.id.ad_slider)).check(doesNotExist())
    }

    @Test
    fun pullToRefreshAsksTheSlotAgain() {
        adsReturnsSlot = true
        stubLoadedBanner()

        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.categoriesFragment)).perform(click())
        waitForView(withText("Food"), timeoutMs = 10_000)
        waitUntil(timeoutMs = 10_000) { categoriesAdRequests() >= 1 }
        val beforeRefresh = categoriesAdRequests()

        onView(withId(R.id.swipe)).perform(swipeDown())

        val refreshed = waitUntil { categoriesAdRequests() > beforeRefresh }
        assertTrue("expected a second /ads call for categories after pull-to-refresh", refreshed)
    }

    private fun categoriesAdRequests(): Int =
        adsRequests.count { """"screen":"categories"""" in it }

    private fun stubLoadedBanner() {
        coEvery { AdTestDoubles.repository.loadDisplayAds(any(), any()) } returns listOf(boundAd())
    }

    private fun boundAd(): RetailMediaAd = mockk(relaxed = true) {
        every { adType } returns RetailMediaAdType.CONTENT
        every { bindRetailMediaAd(any(), any()) } returns AdBindingResult.Success
    }

    private companion object {
        private const val HOME_PRODUCT =
            """{"id":"p1","name":"Milk","price":1.0,"currencyId":"RUB",""" +
                """"picture":"http://localhost/p1.png","categoryId":"c1",""" +
                """"url":"http://localhost/p1","available":true}"""
    }
}
