@file:OptIn(com.yandex.mobile.ads.retailmedia.RetailMediaApi::class)

package com.yandex.retailmedia.sample.presentation.common

import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.yandex.mobile.ads.common.AdBindingResult
import com.yandex.mobile.ads.nativeads.MediaView
import com.yandex.mobile.ads.retailmedia.RetailMediaAd
import com.yandex.mobile.ads.retailmedia.RetailMediaAdViewBinder
import com.yandex.retailmedia.sample.databinding.ViewAdBannerBinding

internal const val RETAIL_MEDIA_LOG_TAG = "RetailMediaSample"

private const val DEFAULT_CREATIVE_RATIO = 16f / 9f

/**
 * Fits the view to the creative's aspect ratio: `MediaView` draws its content with `CENTER_CROP`, so
 * anything the view isn't fitted to gets cropped. The ratio comes from `media.aspectRatio`, which the
 * SDK reports for video, HTML and carousel creatives; 16:9 when it's missing or zero.
 */
internal fun MediaView.applyCreativeRatio(ad: RetailMediaAd) {
    val media = ad.adAssets.media
    isVisible = media != null
    val ratio = media?.aspectRatio?.takeIf { it > 0f } ?: DEFAULT_CREATIVE_RATIO
    updateLayoutParams<ConstraintLayout.LayoutParams> { dimensionRatio = "H,$ratio:1" }
}

// [Step 4]
/**
 * Binds a display ad.
 *
 * Three places where it's easy to go wrong:
 *
 * - `NativeAdView` draws nothing by itself. The ad appears only through the asset views passed to
 *   `RetailMediaAdViewBinder`. The set is dictated by the creative, not the layout: an asset the ad
 *   unit returned with no view for it fails the whole binding — `AdBindingResult.Failure` carries
 *   `missingAssetName`;
 * - the first argument of `bindRetailMediaAd` is not the `NativeAdView` itself but the **parent**
 *   view the slot lives in: the SDK tracks the ad by it;
 * - an impression counts only when all required asset views are shown. The SDK treats `GONE`, zero
 *   alpha and zero size the same, so hiding a filled view means losing the impression.
 */
fun bindDisplayAd(banner: ViewAdBannerBinding, parent: View, ad: RetailMediaAd?) {
    if (ad == null) {
        banner.root.isVisible = false
        return
    }
    banner.adMedia.applyCreativeRatio(ad)
    val viewBinder = RetailMediaAdViewBinder.Builder(banner.root)
        .setMediaView(banner.adMedia)
        .setTitleView(banner.adTitle)
        .setBodyView(banner.adBody)
        .setSponsoredView(banner.adSponsored)
        .setWarningView(banner.adWarning)
        .setFeedbackView(banner.adFeedback)
        .build()

    when (val result = ad.bindRetailMediaAd(parent, viewBinder)) {
        AdBindingResult.Success -> banner.root.isVisible = true
        is AdBindingResult.Failure -> {
            Log.e(
                RETAIL_MEDIA_LOG_TAG,
                "Display ad binding failed: ${result.missingAssetName}",
                result.exception,
            )
            banner.root.isVisible = false
        }
    }
}
