@file:OptIn(com.yandex.mobile.ads.retailmedia.RetailMediaApi::class)

package com.yandex.retailmedia.sample.data.ads

import android.content.Context
import com.yandex.mobile.ads.common.BidderTokenLoadListener
import com.yandex.mobile.ads.common.BidderTokenLoader
import com.yandex.mobile.ads.common.BidderTokenRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject

// [Step 1]
/**
 * Gets the bidder token from the SDK. The token is sent in the body of POST requests to
 * our backend, which forwards it in the s2s request to Yandex RetailMedia.
 */
class RetailMediaBidderTokenProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun bidderToken(): String? = suspendCancellableCoroutine { cont ->
        BidderTokenLoader(context).loadBidderToken(
            BidderTokenRequest.retailMedia(),
            object : BidderTokenLoadListener {
                override fun onBidderTokenLoaded(bidderToken: String) {
                    if (cont.isActive) cont.resume(bidderToken)
                }

                override fun onBidderTokenFailedToLoad(failureReason: String) {
                    if (cont.isActive) cont.resume(null)
                }
            },
        )
    }
}
