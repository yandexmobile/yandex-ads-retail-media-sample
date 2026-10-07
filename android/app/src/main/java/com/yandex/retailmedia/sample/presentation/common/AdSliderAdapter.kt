@file:OptIn(com.yandex.mobile.ads.retailmedia.RetailMediaApi::class)

package com.yandex.retailmedia.sample.presentation.common

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yandex.mobile.ads.common.AdBindingResult
import com.yandex.mobile.ads.retailmedia.RetailMediaAd
import com.yandex.mobile.ads.retailmedia.RetailMediaAdViewBinder
import com.yandex.retailmedia.sample.databinding.ItemAdSlideBinding

// [Step 4]
/**
 * Binds display ads in the slider: one `readyResponse` may carry several `RetailMediaAd`s, and each
 * of them is shown here.
 *
 * The tracking container is the slider's `RecyclerView` itself, while the slide's `NativeAdView` goes
 * into the binder. Mixing them up is a classic mistake: the first argument of `bindRetailMediaAd` is
 * the slot's parent, not the ad view.
 */
class AdSliderAdapter : ListAdapter<RetailMediaAd, AdSliderAdapter.ViewHolder>(DIFF) {

    private var container: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        container = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        container = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemAdSlideBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), container ?: return)
    }

    class ViewHolder(private val binding: ItemAdSlideBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ad: RetailMediaAd, container: RecyclerView) {
            binding.adMedia.applyCreativeRatio(ad)

            val viewBinder = RetailMediaAdViewBinder.Builder(binding.root)
                .setMediaView(binding.adMedia)
                .setTitleView(binding.adTitle)
                .setBodyView(binding.adBody)
                .setSponsoredView(binding.adSponsored)
                .setWarningView(binding.adWarning)
                .setFeedbackView(binding.adFeedback)
                .build()

            val result = ad.bindRetailMediaAd(container, viewBinder)
            if (result is AdBindingResult.Failure) {
                Log.e(
                    RETAIL_MEDIA_LOG_TAG,
                    "Slider ad binding failed: ${result.missingAssetName}",
                    result.exception,
                )
            }
        }
    }

    private companion object {
        @SuppressLint("DiffUtilEquals")
        private val DIFF = object : DiffUtil.ItemCallback<RetailMediaAd>() {
            override fun areItemsTheSame(a: RetailMediaAd, b: RetailMediaAd) = a === b
            override fun areContentsTheSame(a: RetailMediaAd, b: RetailMediaAd) = a === b
        }
    }
}
