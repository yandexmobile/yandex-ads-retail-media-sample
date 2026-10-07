@file:OptIn(com.yandex.mobile.ads.retailmedia.RetailMediaApi::class)

package com.yandex.retailmedia.sample.presentation.common

import com.yandex.mobile.ads.retailmedia.RetailMediaAd

sealed interface FeedItem

class AdSliderRow(val ads: List<RetailMediaAd>) : FeedItem
