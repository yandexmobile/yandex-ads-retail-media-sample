@file:OptIn(com.yandex.mobile.ads.retailmedia.RetailMediaApi::class)

package com.yandex.retailmedia.sample

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.yandex.mobile.ads.common.AdBindingResult
import com.yandex.mobile.ads.common.AdInfo
import com.yandex.mobile.ads.common.Creative
import com.yandex.mobile.ads.retailmedia.RetailMediaAd
import com.yandex.mobile.ads.retailmedia.RetailMediaAdViewBinder
import com.yandex.mobile.ads.retailmedia.type.RetailMediaAdType
import com.yandex.retailmedia.sample.app.MainActivity
import com.yandex.retailmedia.sample.di.AdTestDoubles
import com.yandex.retailmedia.sample.util.MockBackendRule
import com.yandex.retailmedia.sample.util.waitForView
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList

@HiltAndroidTest
class SponsoredAddToCartTest {

    private val cartPosts = CopyOnWriteArrayList<String>()

    private val homeBody =
        """{"products":{"items":[{"id":"p1","name":"Milk","price":1.0,"currencyId":"RUB","picture":"http://localhost/p1.png","categoryId":"c1","url":"http://localhost/p1","available":true,"sponsored":true}],"page":1,"pageSize":20,"hasMore":false},"adSlot":{"adUnitId":"R-M-home","readyResponse":"RR"}}"""

    private val dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse = when {
            request.path == "/home" -> MockResponse().setBody(homeBody)
            request.path?.startsWith("/cart") == true -> {
                if (request.method == "POST") cartPosts += request.body.readUtf8()
                MockResponse().setBody("""{"items":[]}""")
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

        val promo = mockk<RetailMediaAd>()
        every { promo.adType } returns RetailMediaAdType.PRODUCT_PROMO
        val creative = mockk<Creative>(relaxed = true)
        every { creative.offerId } returns "p1"
        val adInfo = mockk<AdInfo>(relaxed = true)
        every { adInfo.creatives } returns listOf(creative)
        every { promo.adInfo } returns adInfo
        every { promo.bindRetailMediaAd(any(), any()) } answers {
            val binder = secondArg<RetailMediaAdViewBinder>()
            binder.customAssets.firstOrNull { it.name == "cart" }?.let { cav ->
                cav.customView.setOnClickListener {
                    cav.customClickListener.onCustomAssetClicked("http://localhost/click")
                }
            }
            AdBindingResult.Success
        }
        coEvery { AdTestDoubles.repository.loadProductPromo(any(), any(), any()) } returns listOf(promo)
        coEvery { AdTestDoubles.repository.loadDisplayAds(any(), any()) } returns emptyList()
    }

    @Test
    fun addToCartOnSponsoredCardAddsProductOnce() {
        ActivityScenario.launch(MainActivity::class.java)
        waitForView(withText("Milk"), timeoutMs = 10_000)
        waitForView(withId(R.id.addToCart), timeoutMs = 5_000)
        onView(withId(R.id.addToCart)).perform(click())

        val deadline = System.currentTimeMillis() + 10_000
        while (System.currentTimeMillis() < deadline && cartPosts.isEmpty()) {
            Thread.sleep(100)
        }
        Thread.sleep(DUPLICATE_POST_GRACE_MS)
        assertEquals("expected p1 added to the cart once, got $cartPosts", 1, cartPosts.count { "p1" in it })
    }

    private companion object {
        const val DUPLICATE_POST_GRACE_MS = 500L
    }
}
